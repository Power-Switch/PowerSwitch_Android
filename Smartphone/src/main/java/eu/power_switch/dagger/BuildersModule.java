package eu.power_switch.dagger;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.UnknownErrorDialog;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class BuildersModule {

    @Binds
    @IntoMap
    @ActivityKey(MainActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> bindMainActivityInjectorFactory(MainActivitySubComponent.Builder builder);

    @Binds
    @IntoMap
    @ActivityKey(UnknownErrorDialog.class)
    abstract AndroidInjector.Factory<? extends Activity> bindUnknownErrorDialogInjectorFactory(UnknownErrorSubComponent.Builder builder);

    // Add more bindings here for other sub components

}
