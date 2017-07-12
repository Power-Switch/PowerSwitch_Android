package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureReceiverDialog;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage1Name;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage2Type;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage3Setup;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage4Gateway;
import eu.power_switch.gui.fragment.configure_receiver.ConfigureReceiverDialogPage5Summary;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class ReceiverBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialog configureReceiverDialog();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage1Name configureReceiverDialogPage1Name();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage2Type configureReceiverDialogPage2Type();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage3Setup configureReceiverDialogPage3Setup();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage4Gateway configureReceiverDialogPage4Gateway();

    @ContributesAndroidInjector
    abstract ConfigureReceiverDialogPage5Summary configureReceiverDialogPage5TabbedSummary();

}
