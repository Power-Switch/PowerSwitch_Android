package eu.power_switch.gui.dialog.butterknife;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Base class for a DialogFragment backed by ButterKnife
 * <p>
 * Created by Markus on 30.06.2017.
 */
public abstract class ButterKnifeDialogFragment extends DialogFragment {

    protected View     rootView;
    private   Unbinder unbinder;

    @NonNull
    @Override
    @CallSuper
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(getLayoutRes(), null);

        unbinder = ButterKnife.bind(this, rootView);

        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * @return The layout resource to use for this fragment
     */
    @LayoutRes
    protected abstract int getLayoutRes();

}
