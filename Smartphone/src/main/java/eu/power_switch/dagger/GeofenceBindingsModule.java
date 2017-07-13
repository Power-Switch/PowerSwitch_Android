package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.google_play_services.geofence.GeofenceIntentReceiver;
import eu.power_switch.google_play_services.geofence.GeofenceIntentService;
import eu.power_switch.gui.dialog.SelectApartmentForGeofenceDialog;
import eu.power_switch.gui.dialog.configuration.ConfigureApartmentGeofenceDialog;
import eu.power_switch.gui.dialog.configuration.ConfigureGeofenceDialog;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage1Location;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage2EnterActions;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage3ExitActions;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage4Summary;
import eu.power_switch.gui.fragment.geofences.ApartmentGeofencesFragment;
import eu.power_switch.gui.fragment.geofences.CustomGeofencesFragment;
import eu.power_switch.gui.fragment.geofences.GeofencesTabFragment;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class GeofenceBindingsModule {

    @ContributesAndroidInjector
    abstract GeofencesTabFragment geofencesTabFragment();

    @ContributesAndroidInjector
    abstract ApartmentGeofencesFragment apartmentGeofencesFragment();

    @ContributesAndroidInjector
    abstract SelectApartmentForGeofenceDialog selectApartmentForGeofenceDialog();

    @ContributesAndroidInjector
    abstract CustomGeofencesFragment customGeofencesFragment();

    @ContributesAndroidInjector
    abstract ConfigureApartmentGeofenceDialog configureApartmentGeofenceDialog();

    @ContributesAndroidInjector
    abstract ConfigureGeofenceDialog configureGeofenceDialog();

    @ContributesAndroidInjector
    abstract ConfigureGeofenceDialogPage1Location configureGeofenceDialogPage1Location();

    @ContributesAndroidInjector
    abstract ConfigureGeofenceDialogPage2EnterActions configureGeofenceDialogPage2EnterActions();

    @ContributesAndroidInjector
    abstract ConfigureGeofenceDialogPage3ExitActions configureGeofenceDialogPage3ExitActions();

    @ContributesAndroidInjector
    abstract ConfigureGeofenceDialogPage4Summary configureGeofenceDialogPage4Summary();

    @ContributesAndroidInjector
    abstract GeofenceIntentReceiver geofenceIntentReceiver();

    @ContributesAndroidInjector
    abstract GeofenceIntentService geofenceIntentService();

}
