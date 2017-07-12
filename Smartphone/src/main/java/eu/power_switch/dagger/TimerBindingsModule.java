package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.configuration.ConfigureTimerDialog;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage1Time;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage2Days;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage3Action;
import eu.power_switch.gui.fragment.configure_timer.ConfigureTimerDialogPage4TabbedSummary;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class TimerBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureTimerDialog configureTimerDialog();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage1Time configureTimerDialogPage1Time();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage2Days configureTimerDialogPage2Days();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage3Action configureTimerDialogPage3Action();

    @ContributesAndroidInjector
    abstract ConfigureTimerDialogPage4TabbedSummary configureTimerDialogPage4TabbedSummary();

}
