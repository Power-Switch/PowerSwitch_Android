package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.WriteNfcTagDialog;
import eu.power_switch.gui.fragment.NfcFragment;
import eu.power_switch.nfc.HiddenReceiverActivity;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class NfcBindingsModule {

    @ContributesAndroidInjector
    abstract NfcFragment nfcFragment();

    @ContributesAndroidInjector
    abstract WriteNfcTagDialog writeNfcTagDialog();

    @ContributesAndroidInjector
    abstract HiddenReceiverActivity hiddenReceiverActivity();

}
