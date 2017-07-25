/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.dagger.module;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import eu.power_switch.application.PowerSwitchWear;
import eu.power_switch.shared.application.RunConfig;

/**
 * Created by Markus on 25.07.2017.
 */
@Module
public abstract class AppModule {

    @Binds
    abstract Application application(PowerSwitchWear application);

    @Provides
    @Singleton
    static Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    static RunConfig provideRunConfig() {
        Boolean enabled = false;
//        Boolean enabled = developerPreferencesHandler.getValue(DeveloperPreferencesHandler.PLAY_STORE_MODE);

        if (enabled) {
            return new RunConfig(RunConfig.Mode.DEMO);
        } else {
            return new RunConfig(RunConfig.Mode.NORMAL);
        }
    }

}
