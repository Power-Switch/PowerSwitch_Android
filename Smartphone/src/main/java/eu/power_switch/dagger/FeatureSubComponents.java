package eu.power_switch.dagger;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.UnknownErrorDialog;

/**
 * Created by Markus on 12.07.2017.
 */
@Subcomponent
interface MainActivitySubComponent extends AndroidInjector<MainActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MainActivity> {
    }

}

@Subcomponent
interface UnknownErrorSubComponent extends AndroidInjector<UnknownErrorDialog> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<UnknownErrorDialog> {
    }

}