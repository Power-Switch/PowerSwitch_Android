/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.action;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.history.HistoryHelper;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkPackage;
import eu.power_switch.network.UdpNetworkPackage;
import eu.power_switch.notification.NotificationHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.phone.call.CallEvent;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.action.Action;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.PhoneConstants;
import eu.power_switch.shared.constants.SleepAsAndroidConstants;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;
import eu.power_switch.shared.settings.WearablePreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import timber.log.Timber;

/**
 * Created by Markus on 05.12.2015.
 */
public class ActionHandler {

	/**
	 * Execute Receiver Action
	 *
	 * @param context  any suitable context
	 * @param receiver receiver to execute on
	 * @param button   button to activate
	 */
	public static void execute(@NonNull Context context, @NonNull Receiver receiver, @NonNull Button button) {
		try {
			executeReceiverAction(context, receiver, button);

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.receiver_action_history_text,
									receiver.getName(),
									button.getName())));
		} catch (ActionNotSupportedException e) {
            Timber.e("Action not supported by Receiver!", e);
            StatusMessageHandler.showInfoMessage(context,
					context.getString(R.string.action_not_supported_by_receiver),
					5000);
		} catch (GatewayNotSupportedException e) {
            Timber.e("Gateway not supported by Receiver!", e);
            StatusMessageHandler.showInfoMessage(context,
					context.getString(R.string.gateway_not_supported_by_receiver),
					5000);
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	private static void executeReceiverAction(@NonNull Context context, @NonNull Receiver receiver, @NonNull Button button) throws Exception {
		NetworkHandler.init(context);

		Apartment apartment = DatabaseHandler.getContainingApartment(receiver);
		Room room = apartment.getRoom(receiver.getRoomId());

		ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
		List<Gateway> gateways;
		if (!receiver.getAssociatedGateways().isEmpty()) {
			gateways = receiver.getAssociatedGateways();
		} else {
			if (room.getAssociatedGateways().isEmpty()) {
				gateways = apartment.getAssociatedGateways();
			} else {
				gateways = room.getAssociatedGateways();
			}
		}

		if (gateways.isEmpty() && apartment.getAssociatedGateways().isEmpty()) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.apartment_has_no_associated_gateways,
					Snackbar.LENGTH_LONG);
			return;
		}

		boolean hasActiveGateway = false;
		for (Gateway gateway : gateways) {
			if (gateway.isActive()) {
				hasActiveGateway = true;
				break;
			}
		}

		if (!hasActiveGateway) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.no_active_gateway,
					Snackbar.LENGTH_LONG);
			return;
		}

		for (Gateway gateway : gateways) {
			if (gateway.isActive()) {
				NetworkPackage networkPackage = getNetworkPackage(apartment,
						gateway,
						receiver,
						button);

				for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
					networkPackages.add(networkPackage);
				}
			}
		}

		NetworkHandler.send(networkPackages);

		// set on object, as well as in database
		receiver.setLastActivatedButtonId(button.getId());
		DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());

		if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			ReceiverWidgetProvider.forceWidgetUpdate(context);
		}
		if (WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			UtilityService.forceWearDataUpdate(context);
		}
	}

	/**
	 * Execute Room Action
	 *
	 * @param context    any suitable context
	 * @param room       room to execute on
	 * @param buttonName button name to execute on each receiver
	 */
	public static void execute(@NonNull Context context, @NonNull Room room, @NonNull String buttonName) {
		try {
			executeRoomAction(context, room, buttonName);

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.room_action_history_text,
									room.getName(),
									buttonName)));
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	/**
	 * Execute Room Action
	 *
	 * @param context  any suitable context
	 * @param room     room to execute on
	 * @param buttonId button ID to execute on each receiver
	 */
	public static void execute(@NonNull Context context, @NonNull Room room, long buttonId) {
		try {
			executeRoomAction(context, room, buttonId);

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.room_action_history_text,
									room.getName(),
									Button.getName(context, buttonId))));
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	private static void executeRoomAction(@NonNull Context context, @NonNull Room room, @NonNull String buttonName) throws Exception {
		NetworkHandler.init(context);

		Apartment apartment = DatabaseHandler.getContainingApartment(room);

		List<Gateway> gateways;
		if (!room.getAssociatedGateways().isEmpty()) {
			gateways = room.getAssociatedGateways();
		} else {
			gateways = apartment.getAssociatedGateways();
		}

		if (gateways.isEmpty() && apartment.getAssociatedGateways().isEmpty()) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.apartment_has_no_associated_gateways,
					Snackbar.LENGTH_LONG);
			return;
		}

		boolean hasActiveGateway = false;
		for (Gateway gateway : apartment.getAssociatedGateways()) {
			if (gateway.isActive()) {
				hasActiveGateway = true;
				break;
			}
		}

		if (!hasActiveGateway) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.no_active_gateway,
					Snackbar.LENGTH_LONG);
			return;
		}

		ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
		for (Receiver receiver : room.getReceivers()) {
			try {
				Button button = receiver.getButtonCaseInsensitive(buttonName);

				List<Gateway> associatedGateways;
				if (!receiver.getAssociatedGateways().isEmpty()) {
					associatedGateways = receiver.getAssociatedGateways();
				} else {
					associatedGateways = gateways;
				}

				for (Gateway gateway : associatedGateways) {
					if (gateway.isActive()) {
						try {
							NetworkPackage networkPackage = getNetworkPackage(apartment,
									gateway,
									receiver,
									button);

							for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
								networkPackages.add(networkPackage);
							}

							// set on object, as well as in database
							receiver.setLastActivatedButtonId(button.getId());
							DatabaseHandler.setLastActivatedButtonId(receiver.getId(),
									button.getId());
						} catch (ActionNotSupportedException e) {
                            Timber.e("Action not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
									context.getString(R.string.action_not_supported_by_receiver),
									5000);
						} catch (GatewayNotSupportedException e) {
                            Timber.e("Gateway not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
									context.getString(R.string.gateway_not_supported_by_receiver),
									5000);
						}
					}
				}
			} catch (NoSuchElementException e) {
				// ignore if Receiver doesnt support this action
			}
		}

		if (networkPackages.size() <= 0) {
            Timber.d(context.getString(R.string.no_receiver_supports_this_action));
            StatusMessageHandler.showInfoMessage(context,
					context.getString(R.string.no_receiver_supports_this_action),
					Snackbar.LENGTH_LONG);
		} else {
			NetworkHandler.send(networkPackages);
		}

		if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			ReceiverWidgetProvider.forceWidgetUpdate(context);
		}
		if (WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			UtilityService.forceWearDataUpdate(context);
		}
	}

	private static void executeRoomAction(@NonNull Context context, @NonNull Room room, long buttonId) throws Exception {
		NetworkHandler.init(context);

		Apartment apartment = DatabaseHandler.getContainingApartment(room);

		List<Gateway> gateways;
		if (!room.getAssociatedGateways().isEmpty()) {
			gateways = room.getAssociatedGateways();
		} else {
			gateways = apartment.getAssociatedGateways();
		}

		if (gateways.isEmpty() && apartment.getAssociatedGateways().isEmpty()) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.apartment_has_no_associated_gateways,
					Snackbar.LENGTH_LONG);
			return;
		}

		boolean hasActiveGateway = false;
		for (Gateway gateway : apartment.getAssociatedGateways()) {
			if (gateway.isActive()) {
				hasActiveGateway = true;
				break;
			}
		}

		if (!hasActiveGateway) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.no_active_gateway,
					Snackbar.LENGTH_LONG);
			return;
		}

		ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
		for (Receiver receiver : room.getReceivers()) {
			try {
				Button button = receiver.getButton(buttonId);

				List<Gateway> associatedGateways;
				if (!receiver.getAssociatedGateways().isEmpty()) {
					associatedGateways = receiver.getAssociatedGateways();
				} else {
					associatedGateways = gateways;
				}

				for (Gateway gateway : associatedGateways) {
					if (gateway.isActive()) {
						try {
							NetworkPackage networkPackage = getNetworkPackage(apartment,
									gateway,
									receiver,
									button);
							for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
								networkPackages.add(networkPackage);
							}
						} catch (ActionNotSupportedException e) {
                            Timber.e("Action not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
									context.getString(R.string.action_not_supported_by_receiver),
									5000);
						} catch (GatewayNotSupportedException e) {
                            Timber.e("Gateway not supported by Receiver!", e);
                            StatusMessageHandler.showInfoMessage(context,
									context.getString(R.string.gateway_not_supported_by_receiver),
									5000);
						}
					}
				}

				// set on object, as well as in database
				receiver.setLastActivatedButtonId(button.getId());
				DatabaseHandler.setLastActivatedButtonId(receiver.getId(), button.getId());
			} catch (NoSuchElementException e) {
				// ignore if Receiver doesnt support this action
			}
		}

		if (networkPackages.size() <= 0) {
            Timber.d(context.getString(R.string.no_receiver_supports_this_action));
            StatusMessageHandler.showInfoMessage(context,
					context.getString(R.string.no_receiver_supports_this_action),
					Snackbar.LENGTH_LONG);
		} else {
			NetworkHandler.send(networkPackages);
		}

		if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			ReceiverWidgetProvider.forceWidgetUpdate(context);
		}
		if (WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			UtilityService.forceWearDataUpdate(context);
		}
	}

	/**
	 * Execute Scene Action
	 *
	 * @param context any suitable context
	 * @param scene   scene to execute
	 */
	public static void execute(@NonNull Context context, @NonNull Scene scene) {
		try {
			executeScene(context, scene);

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.scene_action_history_text,
									scene.getName())));
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	private static void executeScene(@NonNull Context context, @NonNull Scene scene) throws Exception {
		NetworkHandler.init(context);


		Apartment apartment = DatabaseHandler.getContainingApartment(scene);

		if (apartment.getAssociatedGateways().isEmpty()) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.apartment_has_no_associated_gateways,
					Snackbar.LENGTH_LONG);
			return;
		}

		boolean hasActiveGateway = false;
		for (Gateway gateway : apartment.getAssociatedGateways()) {
			if (gateway.isActive()) {
				hasActiveGateway = true;
				break;
			}
		}

		if (!hasActiveGateway) {
			StatusMessageHandler.showInfoMessage(context,
					R.string.no_active_gateway,
					Snackbar.LENGTH_LONG);
			return;
		}

		ArrayList<NetworkPackage> networkPackages = new ArrayList<>();
		for (SceneItem sceneItem : scene.getSceneItems()) {
			Room room = DatabaseHandler.getRoom(sceneItem.getReceiver().getRoomId());
			Receiver receiver = sceneItem.getReceiver();

			List<Gateway> gateways;
			if (!receiver.getAssociatedGateways().isEmpty()) {
				gateways = receiver.getAssociatedGateways();
			} else {
				if (!room.getAssociatedGateways().isEmpty()) {
					gateways = room.getAssociatedGateways();
				} else {
					gateways = apartment.getAssociatedGateways();
				}
			}

			for (Gateway gateway : gateways) {
				if (gateway.isActive()) {
					NetworkPackage networkPackage = getNetworkPackage(apartment,
							gateway,
							sceneItem.getReceiver(),
							sceneItem.getActiveButton());

					for (int i = 0; i < receiver.getRepetitionAmount(); i++) {
						networkPackages.add(networkPackage);
					}

					// set on object, as well as in database
					sceneItem.getReceiver()
							.setLastActivatedButtonId(sceneItem.getActiveButton().getId());
					DatabaseHandler.setLastActivatedButtonId(sceneItem.getReceiver().getId(),
							sceneItem.getActiveButton().getId());
				}
			}
		}

		NetworkHandler.send(networkPackages);

		if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			ReceiverWidgetProvider.forceWidgetUpdate(context);
		}
		if (WearablePreferencesHandler.<Boolean>get(WearablePreferencesHandler.KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON)) {
			UtilityService.forceWearDataUpdate(context);
		}
	}

	/**
	 * Execute Timer actions
	 *
	 * @param context any suitable context
	 * @param timer   timer to execute
	 */
	public static void execute(@NonNull Context context, @NonNull Timer timer) {
		try {
			executeActions(context, timer.getActions());

			if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOW_TIMER_NOTIFICATIONS)) {
				NotificationHandler.createNotification(context,
						"Timer",
						"Timer \"" + timer.getName() + "\" executed");
			}

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.timer_action_history_text,
									timer.getName())));
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	/**
	 * Execute Sleep As Android actions
	 *
	 * @param context any suitable context
	 * @param event   event type
	 */
	public static void execute(@NonNull Context context, @NonNull SleepAsAndroidConstants.Event event) {
		try {
			List<Action> actions = DatabaseHandler.getAlarmActions(event);
			executeActions(context, actions);

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.sleep_as_android_action_history_text,
									event.toString())));
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	/**
	 * Execute Alarm Clock actions
	 *
	 * @param context any suitable context
	 * @param event   alarm event type
	 */
	public static void execute(@NonNull Context context, @NonNull AlarmClockConstants.Event event) {
		try {
			List<Action> actions = DatabaseHandler.getAlarmActions(event);
			executeActions(context, actions);

			HistoryHelper.add(context,
					new HistoryItem((long) -1,
							Calendar.getInstance(),
							context.getString(R.string.alarm_clock_action_history_text,
									event.toString())));
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	/**
	 * Execute Geofence actions
	 *
	 * @param context   any suitable context
	 * @param geofence  geofence
	 * @param eventType event type
	 */
	public static void execute(@NonNull Context context, @NonNull Geofence geofence, @NonNull Geofence.EventType eventType) {
		try {
			executeActions(context, geofence.getActions(eventType));

			HistoryItem historyItem;
			String notificationMessage;
			if (Geofence.EventType.ENTER.equals(eventType)) {
				notificationMessage = "Geofence \"" + geofence.getName() + "\" entered";
				historyItem = new HistoryItem((long) -1,
						Calendar.getInstance(),
						context.getString(R.string.geofence_enter_action_history_text,
								geofence.getName()));
			} else if (Geofence.EventType.EXIT.equals(eventType)) {
				notificationMessage = "Geofence \"" + geofence.getName() + "\" exited";
				historyItem = new HistoryItem((long) -1,
						Calendar.getInstance(),
						context.getString(R.string.geofence_exit_action_history_text,
								geofence.getName()));
			} else {
				notificationMessage = "Geofence \"" + geofence.getName() + "\" Event: " + eventType.toString() + " activated";
				historyItem = new HistoryItem((long) -1,
						Calendar.getInstance(),
						context.getString(R.string.geofence_event_type_action_history_text,
								geofence.getName(),
								eventType.toString()));
			}

			if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOW_GEOFENCE_NOTIFICATIONS)) {
				NotificationHandler.createNotification(context,
						context.getString(R.string.geofence),
						notificationMessage);
			}
			HistoryHelper.add(context, historyItem);
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	/**
	 * Execute CallEvent actions
	 *
	 * @param context
	 * @param callEvent
	 * @param callType
	 */
	public static void execute(Context context, CallEvent callEvent, @NonNull PhoneConstants.CallType callType) {
		try {
			executeActions(context, callEvent.getActions(callType));

			HistoryItem historyItem = new HistoryItem((long) -1,
					Calendar.getInstance(),
					context.getString(R.string.geofence_enter_action_history_text,
							callEvent.getName()));
			HistoryHelper.add(context, historyItem);
		} catch (Exception e) {
			StatusMessageHandler.showErrorMessage(context, e);
			try {
				HistoryHelper.add(context, e);
			} catch (Exception e1) {
                Timber.e(e1);
            }
		}
	}

	private static void executeActions(@NonNull Context context, @NonNull List<Action> actions) throws Exception {
		for (Action action : actions) {
			switch (action.getActionType()) {
				case Action.ACTION_TYPE_RECEIVER:
					ReceiverAction receiverAction = (ReceiverAction) action;
					executeReceiverAction(context,
							receiverAction.getReceiver(),
							receiverAction.getButton());
					break;
				case Action.ACTION_TYPE_ROOM:
					RoomAction roomAction = (RoomAction) action;
					executeRoomAction(context, roomAction.getRoom(), roomAction.getButtonName());
					break;
				case Action.ACTION_TYPE_SCENE:
					SceneAction sceneAction = (SceneAction) action;
					executeScene(context, sceneAction.getScene());
					break;
			}
		}
	}

	private static NetworkPackage getNetworkPackage(Apartment apartment, Gateway gateway, Receiver receiver, Button button) throws Exception {
		String signal = receiver.getSignal(gateway, button.getName());

		if (gateway.hasValidLocalAddress() && !gateway.hasValidWanAddress()) {
			// only valid local address
            Timber.d("Using local address");
            return getLocalNetworkPackage(gateway, signal);
		} else if (!gateway.hasValidLocalAddress() && gateway.hasValidWanAddress()) {
			// only valid WAN address
            Timber.d("Using WAN address");
            return getWanNetworkPackage(gateway, signal);
		} else if (gateway.hasValidLocalAddress() && gateway.hasValidWanAddress()) {
			// decide if local or WAN address should be used
			if (NetworkHandler.isWifiConnected() || NetworkHandler.isEthernetConnected()) {
				if (NetworkHandler.isInternetConnected()) {
					if (!gateway.getSsids().isEmpty()) {
						if (gateway.getSsids().contains(NetworkHandler.getConnectedWifiSSID())) {
                            Timber.d("Using local address, connected to SSID specified in Gateway");
                            return getLocalNetworkPackage(gateway, signal);
						} else {
                            Timber.d("Using WAN address, connected to unspecified SSID");
                            return getWanNetworkPackage(gateway, signal);
						}
					} else {
						if (apartment.getGeofence() != null &&
								apartment.getGeofence().isActive() &&
								Geofence.STATE_INSIDE.equals(apartment.getGeofence().getState())) {
                            Timber.d("Using local address, inside geofence");
                            return getLocalNetworkPackage(gateway, signal);
						} else {
                            Timber.d("Using WAN address, outside or missing geofence data");
                            return getWanNetworkPackage(gateway, signal);
						}
					}
				} else {
                    Timber.d("Using local address, no WAN (Internet connection) available");
                    return getLocalNetworkPackage(gateway, signal);
				}
			} else {
                Timber.d("Using WAN address, no WiFi or LAN available");
                return getWanNetworkPackage(gateway, signal);
			}
		} else {
			throw new Exception("Invalid Gateway configuration!");
		}
	}

	private static NetworkPackage getLocalNetworkPackage(Gateway gateway, String signal) {
		return new UdpNetworkPackage(gateway.getLocalHost(),
				gateway.getLocalPort(),
				signal,
				gateway.getTimeout());
	}

	private static NetworkPackage getWanNetworkPackage(Gateway gateway, String signal) {
		return new UdpNetworkPackage(gateway.getWanHost(),
				gateway.getWanPort(),
				signal,
				gateway.getTimeout());
	}
}