/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.activity.ButterKnifeDialogActivity;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.nfc.NfcHandler;
import eu.power_switch.shared.log.Log;

/**
 * Dialog used while waiting for an NFC tag to be written
 * <p>
 * Created by mre on 08.04.2016.
 */
public class WriteNfcTagDialog extends ButterKnifeDialogActivity {

    public static final String KEY_CONTENT = "content";

    @BindView(R.id.txt_nfc_status)
    TextView         textViewStatus;
    @BindView(R.id.layoutLoading)
    LinearLayout     layoutLoading;
    @BindView(R.id.imageView_success)
    IconicsImageView successImage;
    @BindView(R.id.imageView_error)
    IconicsImageView errorImage;

    private String     content;
    private NfcAdapter nfcAdapter;

    public static Intent getNewInstanceIntent(String content) {
        Intent intent = new Intent();
        intent.setAction("eu.power_switch.write_nfc_tag_activity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CONTENT, content);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window
        setTitle(R.string.write_nfc_tag);

        Intent intent = getIntent();
        if (intent.hasExtra(KEY_CONTENT)) {
            content = intent.getStringExtra(KEY_CONTENT);
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        textViewStatus.setText(R.string.waiting_for_tag);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_write_nfc_tag;
    }

    private void enableTagWriteMode() {
        IntentFilter   intentFilter     = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[]{intentFilter};
        PendingIntent nfcPendingIntent = PendingIntent.getActivity(this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, mWriteTagFilters, null);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        // Tag writing mode
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            textViewStatus.setText(R.string.tag_discovered_writing_data);
            layoutLoading.setVisibility(View.VISIBLE);
            successImage.setVisibility(View.GONE);
            errorImage.setVisibility(View.GONE);

            new AsyncTask<Void, Void, AsyncTaskResult<Void>>() {
                @Override
                protected AsyncTaskResult<Void> doInBackground(Void... params) {
                    try {
                        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                        NfcHandler.writeTag(NfcHandler.getAsNdef(content), detectedTag);
//                        NfcHandler.soundNotify(this);

                        return new AsyncTaskResult<>();
                    } catch (Exception e) {
                        return new AsyncTaskResult<>(e);
                    }
                }

                @Override
                protected void onPostExecute(AsyncTaskResult asyncTaskResult) {
                    if (asyncTaskResult.isSuccess()) {
                        textViewStatus.setText(R.string.tag_written_successfully);
                        layoutLoading.setVisibility(View.GONE);
                        successImage.setVisibility(View.VISIBLE);
                        errorImage.setVisibility(View.GONE);
                    } else {
                        Log.e(asyncTaskResult.getException());
                        textViewStatus.setText(R.string.error_writing_tag_please_try_again);
                        layoutLoading.setVisibility(View.GONE);
                        successImage.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                    }
                }
            }.execute();
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
