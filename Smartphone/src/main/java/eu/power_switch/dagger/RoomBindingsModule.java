package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureRoomDialog;
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomDialogPage1;
import eu.power_switch.gui.fragment.configure_room.ConfigureRoomDialogPage2Gateways;
import eu.power_switch.gui.fragment.main.RoomsFragment;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class RoomBindingsModule {

    @ContributesAndroidInjector
    abstract RoomsFragment roomsFragment();

    @ContributesAndroidInjector
    abstract ConfigureRoomDialog configureRoomDialog();

    @ContributesAndroidInjector
    abstract ConfigureRoomDialogPage1 configureRoomDialogPage1();

    @ContributesAndroidInjector
    abstract ConfigureRoomDialogPage2Gateways configureRoomDialogPage2Gateways();

}
