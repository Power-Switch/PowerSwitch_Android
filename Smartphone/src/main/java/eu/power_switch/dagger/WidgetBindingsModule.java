package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;
import eu.power_switch.widget.activity.ConfigureRoomWidgetActivity;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class WidgetBindingsModule {

    @ContributesAndroidInjector
    abstract ConfigureReceiverWidgetActivity configureReceiverWidgetActivity();

    @ContributesAndroidInjector
    abstract ConfigureRoomWidgetActivity configureRoomWidgetActivity();

    @ContributesAndroidInjector
    abstract ConfigureSceneWidgetActivity configureSceneWidgetActivity();

    @ContributesAndroidInjector
    abstract WidgetIntentReceiver widgetIntentReceiver();

}
