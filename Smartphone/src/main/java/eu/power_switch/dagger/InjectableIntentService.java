package eu.power_switch.dagger;

import android.app.IntentService;

import dagger.android.AndroidInjection;

/**
 * Created by Markus on 11.07.2017.
 */
public abstract class InjectableIntentService extends IntentService {

    public InjectableIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }
}
