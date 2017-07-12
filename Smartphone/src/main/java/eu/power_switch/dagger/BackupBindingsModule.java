package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.EditBackupDialog;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class BackupBindingsModule {

    @ContributesAndroidInjector
    abstract EditBackupDialog editBackupDialog();

}
