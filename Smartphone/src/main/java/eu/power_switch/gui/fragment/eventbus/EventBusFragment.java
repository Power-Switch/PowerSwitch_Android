package eu.power_switch.gui.fragment.eventbus;

import eu.power_switch.gui.EventBusHelper;
import eu.power_switch.gui.fragment.butterknife.ButterKnifeFragment;

/**
 * Base class for an EventBus backed Fragment
 * <p>
 * Created by Markus on 02.07.2017.
 */
public abstract class EventBusFragment extends ButterKnifeFragment {

    @Override
    public void onStart() {
        super.onStart();

        EventBusHelper.tryRegister(this);
    }

    @Override
    public void onStop() {
        EventBusHelper.tryUnregister(this);

        super.onStop();
    }
}
