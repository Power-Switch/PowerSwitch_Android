package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureApartmentDialog;
import eu.power_switch.gui.fragment.configure_apartment.ConfigureApartmentDialogPage1Name;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class ApartmentBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureApartmentDialog configureApartmentDialog();

    @ContributesAndroidInjector
    abstract ConfigureApartmentDialogPage1Name configureApartmentDialogPage1Name();

}
