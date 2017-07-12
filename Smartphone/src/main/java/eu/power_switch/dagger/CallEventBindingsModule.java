package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureCallEventDialog;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage1Contacts;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage2Actions;
import eu.power_switch.gui.fragment.configure_call_event.ConfigureCallEventDialogPage3Summary;
import eu.power_switch.phone.call.CallEventReceiver;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class CallEventBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureCallEventDialog configureCallEventDialog();

    @ContributesAndroidInjector
    abstract ConfigureCallEventDialogPage1Contacts configureCallEventDialogPage1Contacts();

    @ContributesAndroidInjector
    abstract ConfigureCallEventDialogPage2Actions configureCallEventDialogPage2Actions();

    @ContributesAndroidInjector
    abstract ConfigureCallEventDialogPage3Summary configureCallEventDialogPage3Summary();

    @ContributesAndroidInjector
    abstract CallEventReceiver callEventReceiver();

}
