package eu.power_switch.wizard.gui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.github.paolorotolo.appintro.AppIntro;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;
import eu.power_switch.settings.DeveloperPreferencesHandler;

/**
 * Base class for a Wizard Activity backed by ButterKnife
 * <p>
 * Created by Markus on 30.06.2017.
 */
public abstract class WizardActivityBase extends AppIntro implements HasFragmentInjector, HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment>             supportFragmentInjector;
    @Inject
    DispatchingAndroidInjector<android.app.Fragment> frameworkFragmentInjector;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply forced locale (if set in developer options)
        applyLocale();

        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
    }

    private void applyLocale() {
        if (DeveloperPreferencesHandler.getForceLanguage()) {
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics                    dm   = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = DeveloperPreferencesHandler.getLocale();
            res.updateConfiguration(conf, dm);
        }
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return frameworkFragmentInjector;
    }

}
