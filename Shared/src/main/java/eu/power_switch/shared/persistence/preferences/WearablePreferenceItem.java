package eu.power_switch.shared.persistence.preferences;

import android.support.annotation.StringRes;

/**
 * Created by Markus on 17.07.2017.
 */

public class WearablePreferenceItem<T> extends PreferenceItem<T> {

    public WearablePreferenceItem(@StringRes int keyRes, T defaultValue) {
        super(keyRes, defaultValue);
    }

}
