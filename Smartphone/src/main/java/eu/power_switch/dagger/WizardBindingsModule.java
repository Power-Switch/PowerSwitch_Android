package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.wizard.gui.AdvancedFeaturesPage;
import eu.power_switch.wizard.gui.ApartmentsPage;
import eu.power_switch.wizard.gui.BasicPage;
import eu.power_switch.wizard.gui.FinishPage;
import eu.power_switch.wizard.gui.RoomsScenesPage;
import eu.power_switch.wizard.gui.SetupApartmentPage;
import eu.power_switch.wizard.gui.SetupGatewayPage;
import eu.power_switch.wizard.gui.SetupRoomPage;
import eu.power_switch.wizard.gui.SingleLineTextInputPage;
import eu.power_switch.wizard.gui.TimerAlarmClockPage;
import eu.power_switch.wizard.gui.WelcomePage;
import eu.power_switch.wizard.gui.WizardActivity;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class WizardBindingsModule {

    @ContributesAndroidInjector
    abstract WizardActivity wizardActivity();

    @ContributesAndroidInjector
    abstract BasicPage basicPage();

    @ContributesAndroidInjector
    abstract SingleLineTextInputPage singleLineTextInputPage();

    @ContributesAndroidInjector
    abstract WelcomePage welcomePage();

    @ContributesAndroidInjector
    abstract ApartmentsPage apartmentsPage();

    @ContributesAndroidInjector
    abstract RoomsScenesPage roomsScenesPage();

    @ContributesAndroidInjector
    abstract TimerAlarmClockPage timerAlarmClockPage();

    @ContributesAndroidInjector
    abstract AdvancedFeaturesPage advancedFeaturesPage();

    @ContributesAndroidInjector
    abstract SetupApartmentPage setupApartmentPage();

    @ContributesAndroidInjector
    abstract SetupRoomPage setupRoomPage();

    @ContributesAndroidInjector
    abstract SetupGatewayPage setupGatewayPage();

    @ContributesAndroidInjector
    abstract FinishPage finishPage();

}
