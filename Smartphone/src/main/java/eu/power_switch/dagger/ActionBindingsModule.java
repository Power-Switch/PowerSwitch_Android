package eu.power_switch.dagger;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.action.ActionHandlerImpl;
import eu.power_switch.gui.dialog.AddActionDialog;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class ActionBindingsModule {

    @Binds
    @Singleton
    public abstract ActionHandler provideActionHandler(ActionHandlerImpl actionHandler);

    @ContributesAndroidInjector
    abstract AddActionDialog addActionDialog();

}
