package eu.power_switch.gui.fragment.wizard;

import android.os.Bundle;

import eu.power_switch.R;

/**
 * Created by Markus on 04.11.2016.
 */
public class SetupApartmentPage extends SingleLineTextInputPage {

    private boolean isValid = false;

    public static SetupApartmentPage newInstance() {
        Bundle args = new Bundle();
        SetupApartmentPage fragment = new SetupApartmentPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();

        setTitle(R.string.configure_apartment);
        setHint("Home");
        setDescription(R.string.tutorial__apartment_explanation);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.blue);
    }

    @Override
    public void onInputChanged(CharSequence s, int start, int before, int count) {
        if (s.length() <= 0) {
            isValid = false;
        } else {
            isValid = true;
        }
    }

    @Override
    public boolean isPolicyRespected() {
        return isValid;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        flashBackground(R.color.color_red_a700, 1000);
        showErrorMessage(getString(R.string.please_enter_name));
    }

}
