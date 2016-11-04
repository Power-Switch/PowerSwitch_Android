package eu.power_switch.gui.fragment.wizard;

import android.os.Bundle;

import eu.power_switch.R;

/**
 * Created by Markus on 04.11.2016.
 */
public class SetupRoomPage extends SingleLineTextInputPage {

    public static SetupRoomPage newInstance() {
        Bundle args = new Bundle();
        SetupRoomPage fragment = new SetupRoomPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();

        setTitle(R.string.configure_room);
        setHint("Living room");
        setDescription(R.string.tutorial__room_explanation);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.blue);
    }
}
