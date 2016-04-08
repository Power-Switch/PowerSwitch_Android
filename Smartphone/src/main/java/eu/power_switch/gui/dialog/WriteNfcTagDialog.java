package eu.power_switch.gui.dialog;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.nfc.NfcHandler;
import eu.power_switch.settings.DeveloperPreferencesHandler;

/**
 * Created by mre on 08.04.2016.
 */
public class WriteNfcTagDialog extends AppCompatActivity {

    public static final String KEY_CONTENT = "content";

    private String content;
    private NfcAdapter nfcAdapter;
    private TextView textView;


    public static Intent getNewInstanceIntent(String content) {
        Intent intent = new Intent();
        intent.setAction("eu.power_switch.write_nfc_tag_activity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CONTENT, content);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate();
//        applyTheme(); // not yet ready, missing theme definitions for dialogs
        // apply forced locale (if set in developer options)
        applyLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_write_nfc_tag);
        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window
        setTitle(R.string.write_nfc_tag);

        Intent intent = getIntent();
        if (intent.hasExtra(KEY_CONTENT)) {
            content = intent.getStringExtra(KEY_CONTENT);
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        textView = (TextView) findViewById(R.id.textView);
    }

    private void applyLocale() {
        if (DeveloperPreferencesHandler.getForceLanguage()) {
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = DeveloperPreferencesHandler.getLocale();
            res.updateConfiguration(conf, dm);
        }
    }

    private void enableTagWriteMode() {
        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[]{intentFilter};
        PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, mWriteTagFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Tag writing mode
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            textView.setText("Tag discovered");

            try {
                NfcHandler.writeTag(NfcHandler.getAsNdef(content), detectedTag);
                Toast.makeText(this, "Success: Wrote id to nfc tag", Toast.LENGTH_LONG)
                        .show();
                NfcHandler.soundNotify(this);
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(this, e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        enableTagWriteMode();
    }

    @Override
    public void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }
}
