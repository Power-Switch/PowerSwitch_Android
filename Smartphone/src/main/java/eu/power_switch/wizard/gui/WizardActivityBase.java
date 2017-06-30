package eu.power_switch.wizard.gui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.github.paolorotolo.appintro.AppIntro;

import eu.power_switch.settings.DeveloperPreferencesHandler;

/**
 * Base class for a Wizard Activity backed by ButterKnife
 * <p>
 * Created by Markus on 30.06.2017.
 */
public abstract class WizardActivityBase extends AppIntro {

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply forced locale (if set in developer options)
        applyLocale();

        super.onCreate(savedInstanceState);
    }

    public void applyLocale() {
        if (DeveloperPreferencesHandler.getForceLanguage()) {
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics                    dm   = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = DeveloperPreferencesHandler.getLocale();
            res.updateConfiguration(conf, dm);
        }
    }

}
