/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

import eu.power_switch.R;
import eu.power_switch.gui.animation.ActionResponse;
import eu.power_switch.shared.constants.WearableConstants;
import timber.log.Timber;

/**
 * Created by Markus on 03.06.2015.
 */
public class MessageApiHandler {

    private final Context         context;
    private final GoogleApiClient googleApiClient;
    private       Set<Node>       connectedNodes;

    public MessageApiHandler(Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.googleApiClient = googleApiClient;
    }

    /**
     * Send action string to capable node (Smartphone) which will then send the network package to the Gateway
     *
     * @param actionString Action String containing information about what Receiver and Button to act on
     */
    public void sendAction(@NonNull final String actionString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = WearableConstants.RECEIVER_ACTION_TRIGGER_PATH;

                setupReachableReceiverActionTrigger();

                String nodeId = pickBestNode(connectedNodes);
                if (nodeId != null) {
                    Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
                            path, actionString.getBytes()).setResultCallback(
                            new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    if (!sendMessageResult.getStatus().isSuccess()) {
                                        // Failed to send message
                                        Timber.e("Failed to send Message");
                                        ActionResponse.showFailureAnimation(context, R.string.unknown_error);
                                    } else {
                                        Timber.d("", "Message sent");
                                        ActionResponse.showSuccessAnimation(context);
                                    }
                                }
                            }
                    );
                } else {
                    // Unable to retrieve node with transcription capability
                    Timber.d("", "Unable to retrieve node with transcription capability");
                    ActionResponse.showFailureAnimation(context, R.string.smartphone_not_connected);
                }
            }
        }).start();
    }

    public void sendUpdateRequest() {
        String updateRequest = "Update";
        String path = WearableConstants.REQUEST_DATA_UPDATE_PATH;

        String nodeId = pickBestNode(connectedNodes);
        if (nodeId != null) {
            Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
                    path, updateRequest.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                // Failed to send message
                                Timber.e("Failed to send Message");
                                Toast.makeText(context, "Failed to send Message", Toast.LENGTH_LONG).show();
                            } else {
                                Timber.d("", "Message sent");
                            }
                        }
                    }
            );
        } else {
            // Unable to retrieve node with transcription capability
            Timber.d("", "Unable to retrieve node with transcription capability");
            ActionResponse.showFailureAnimation(context, R.string.smartphone_not_connected);
//            Toast.makeText(context.getApplicationContext(), R.string.smartphone_not_connected, Toast.LENGTH_LONG)
//                    .show();
        }
    }

    private void setupReachableReceiverActionTrigger() {
        CapabilityApi.GetCapabilityResult result = Wearable.CapabilityApi.getCapability(
                googleApiClient, context
                        .getResources()
                        .getString(eu.power_switch.shared.R.string.RECEIVER_ACTION_TRIGGER_CAPABILITY_NAME), CapabilityApi.FILTER_REACHABLE)
                .await();
        updateReceiverActionTriggerCapability(result.getCapability());

        CapabilityApi.CapabilityListener capabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateReceiverActionTriggerCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(googleApiClient, capabilityListener,
                context.getResources()
                        .getString(eu.power_switch.shared.R.string.RECEIVER_ACTION_TRIGGER_CAPABILITY_NAME));
    }

    private void updateReceiverActionTriggerCapability(CapabilityInfo capabilityInfo) {
        connectedNodes = capabilityInfo.getNodes();
    }

    @Nullable
    private String pickBestNode(@Nullable Set<Node> nodes) {
        String bestNodeId = null;

        if (nodes == null) {
            return null;
        }

        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }
}
