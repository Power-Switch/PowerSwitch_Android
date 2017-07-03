package eu.power_switch.gui.dialog.eventbus;

import eu.power_switch.gui.EventBusHelper;
import eu.power_switch.gui.dialog.butterknife.ButterKnifeSupportDialogFragment;

/**
 * Base class for an EventBus backed SupportDialogFragment
 * <p>
 * Created by Markus on 02.07.2017.
 */
public abstract class EventBusSupportDialogFragment extends ButterKnifeSupportDialogFragment {

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
