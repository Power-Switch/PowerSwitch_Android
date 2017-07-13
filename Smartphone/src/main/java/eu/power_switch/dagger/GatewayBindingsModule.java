package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.AddSsidDialog;
import eu.power_switch.gui.dialog.GatewayDetectedDialog;
import eu.power_switch.gui.dialog.configuration.ConfigureGatewayDialog;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage1;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage2;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage3;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage4Summary;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class GatewayBindingsModule {

    @ContributesAndroidInjector
    abstract GatewayDetectedDialog gatewayDetectedDialog();

    @ContributesAndroidInjector
    abstract ConfigureGatewayDialog configureGatewayDialog();

    @ContributesAndroidInjector
    abstract ConfigureGatewayDialogPage1 configureGatewayDialogPage1();

    @ContributesAndroidInjector
    abstract ConfigureGatewayDialogPage2 configureGatewayDialogPage2();

    @ContributesAndroidInjector
    abstract ConfigureGatewayDialogPage3 configureGatewayDialogPage3();

    @ContributesAndroidInjector
    abstract AddSsidDialog addSsidDialog();

    @ContributesAndroidInjector
    abstract ConfigureGatewayDialogPage4Summary configureGatewayDialogPage4Summary();

}
