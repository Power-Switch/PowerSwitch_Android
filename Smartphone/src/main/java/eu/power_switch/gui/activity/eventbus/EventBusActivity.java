package eu.power_switch.gui.activity.eventbus;

import eu.power_switch.gui.EventBusHelper;
import eu.power_switch.gui.activity.butterknife.ButterKnifeActivity;

/**
 * Base class for an EventBus backed Activity
 * <p>
 * Created by Markus on 02.07.2017.
 */
public abstract class EventBusActivity extends ButterKnifeActivity {

    @Override
    protected void onStart() {
        super.onStart();

        EventBusHelper.tryRegister(this);
    }

    @Override
    protected void onStop() {
        EventBusHelper.tryUnregister(this);

        super.onStop();
    }
}
