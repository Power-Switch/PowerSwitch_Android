package eu.power_switch.gui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.nfc.NfcHandler;

/**
 * Created by mre on 08.04.2016.
 */
public class WriteNfcTagDialog extends DialogFragment {

    public static final String KEY_CONTENT = "content";

    private Dialog dialog;
    private View contentView;

    private String content;
    private NfcAdapter nfcAdapter;

    public static WriteNfcTagDialog newInstance(String content) {
        Bundle args = new Bundle();
        args.putString(KEY_CONTENT, content);

        WriteNfcTagDialog fragment = new WriteNfcTagDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();
        content = args.getString(KEY_CONTENT);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        // doesnt work with broadcastReceiver, has to be an Intent (?)
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Tag writing mode
                if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                    try {
                        NfcHandler.writeTag(NfcHandler.getAsNdef(content), detectedTag);
                        Toast.makeText(getActivity(), "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
                                .show();
                        NfcHandler.soundNotify(getActivity());
                    } catch (Exception e) {
                        StatusMessageHandler.showErrorMessage(getActivity(), e);
                    }
                }
            }
        };

        LayoutInflater inflater = getActivity().getLayoutInflater();
        contentView = inflater.inflate(R.layout.dialog_write_nfc_tag, null);
        builder.setView(contentView);

        builder.setTitle(R.string.write_nfc_tag);
        builder.setNeutralButton(android.R.string.cancel, null);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();

        enableTagWriteMode();

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        nfcAdapter.disableForegroundDispatch(getActivity());
        super.onPause();
    }

    private void enableTagWriteMode() {
        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[]{intentFilter};
        PendingIntent nfcPendingIntent = PendingIntent.getActivity(getActivity(), 0,
                new Intent(getActivity(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(getActivity(), nfcPendingIntent, mWriteTagFilters, null);
    }

}
