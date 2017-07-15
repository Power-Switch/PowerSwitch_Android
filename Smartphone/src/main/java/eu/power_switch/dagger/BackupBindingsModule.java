package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.CreateBackupDialog;
import eu.power_switch.gui.dialog.CreateBackupProcessingDialog;
import eu.power_switch.gui.dialog.EditBackupDialog;
import eu.power_switch.gui.dialog.RestoreBackupFromFileActivity;
import eu.power_switch.gui.dialog.RestoreBackupProcessingDialog;
import eu.power_switch.gui.fragment.BackupFragment;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class BackupBindingsModule {

    @ContributesAndroidInjector
    abstract BackupFragment backupFragment();

    @ContributesAndroidInjector
    abstract CreateBackupDialog createBackupDialog();

    @ContributesAndroidInjector
    abstract EditBackupDialog editBackupDialog();

    @ContributesAndroidInjector
    abstract CreateBackupProcessingDialog createBackupProcessingDialog();

    @ContributesAndroidInjector
    abstract RestoreBackupProcessingDialog restoreBackupProcessingDialog();

    @ContributesAndroidInjector
    abstract RestoreBackupFromFileActivity restoreBackupFromFileActivity();

}
