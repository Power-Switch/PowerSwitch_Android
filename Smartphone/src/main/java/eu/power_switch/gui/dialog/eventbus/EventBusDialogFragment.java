package eu.power_switch.gui.dialog.eventbus;

import eu.power_switch.gui.EventBusHelper;
import eu.power_switch.gui.dialog.butterknife.ButterKnifeDialogFragment;

/**
 * Base class for an EventBus backed DialogFragment
 * <p>
 * Created by Markus on 02.07.2017.
 */
public abstract class EventBusDialogFragment extends ButterKnifeDialogFragment {

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
