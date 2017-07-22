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

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.shared.event.ActivityResultEvent;
import timber.log.Timber;

/**
 * Dialog to select a Play Store Donation
 * <p/>
 * Created by Markus on 01.10.2015.
 */
public class DonationDialog extends EventBusSupportDialogFragment implements BillingClientStateListener, PurchasesUpdatedListener {


    private static final String SKU_DONATE_10 = "donate_10";
    private static final String SKU_DONATE_5  = "donate_5";
    private static final String SKU_DONATE_2  = "donate_2";
    private static final String SKU_DONATE_1  = "donate_1";

    private static final String SKU_TEST_PURCHASED        = "android.test.purchased";
    private static final String SKU_TEST_CANCELED         = "android.test.canceled";
    private static final String SKU_TEST_REFUNDED         = "android.test.refunded";
    private static final String SKU_TEST_ITEM_UNAVAILABLE = "android.test.item_unavailable";

    private static final int REQUEST_CODE = 123;

    private static final List<String> IAP_IDS_LIST = Arrays.asList(SKU_DONATE_10, SKU_DONATE_5, SKU_DONATE_2, SKU_DONATE_1);

    @BindView(R.id.button_donate_10)
    Button       donate10;
    @BindView(R.id.button_donate_5)
    Button       donate5;
    @BindView(R.id.button_donate_2)
    Button       donate2;
    @BindView(R.id.button_donate_1)
    Button       donate1;
    @BindView(R.id.layout_donate_buttons)
    LinearLayout layoutDonationButtons;
    @BindView(R.id.layoutLoading)
    LinearLayout layoutLoading;

    private BillingClient billingClient;
    private boolean       billingServiceIsConnected;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_donate_10:
                        initiatePurchase(SKU_DONATE_10);
//                        initiatePurchase(SKU_TEST_PURCHASED);
                        break;
                    case R.id.button_donate_5:
                        initiatePurchase(SKU_DONATE_5);
//                        initiatePurchase(SKU_TEST_CANCELED);
                        break;
                    case R.id.button_donate_2:
                        initiatePurchase(SKU_DONATE_2);
//                        initiatePurchase(SKU_TEST_REFUNDED);
                        break;
                    case R.id.button_donate_1:
                        initiatePurchase(SKU_DONATE_1);
//                        initiatePurchase(SKU_TEST_ITEM_UNAVAILABLE);
                        break;
                    default:
//                        initiatePurchase(SKU_TEST_PURCHASED);
                        break;
                }
            }
        };

        donate10.setOnClickListener(onClickListener);
        donate5.setOnClickListener(onClickListener);
        donate2.setOnClickListener(onClickListener);
        donate1.setOnClickListener(onClickListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle(getString(R.string.donate));
        builder.setNeutralButton(R.string.close, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.show();

        return dialog;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_donation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            billingClient = new BillingClient.Builder(getActivity()).setListener(this)
                    .build();

            connectBillingService();

            // ======================== OLD =========================

//            String key                    = smartphonePreferencesHandler.getPublicKeyString();
//            String base64EncodedPublicKey = new String(Base64.decode(key));
//            iapHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

        } catch (Exception e) {
            statusMessageHandler.showErrorMessage(getActivity(), e);
            Timber.e(e);
            dismiss();
        }
    }

    private void connectBillingService() {
        if (!billingServiceIsConnected) {
            billingClient.startConnection(this);
        }
    }

    @Override
    public void onBillingSetupFinished(int billingResponseCode) {
        if (billingResponseCode == BillingResponse.OK) {
            billingServiceIsConnected = true;
            // The billing client is ready. You can query purchases here.

            billingClient.querySkuDetailsAsync(BillingClient.SkuType.INAPP, IAP_IDS_LIST, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(SkuDetails.SkuDetailsResult result) {
                    // Process the result.
                    switch (result.getResponseCode()) {
                        case BillingResponse.OK:
                            List<SkuDetails> skuDetailsList = result.getSkuDetailsList();
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku   = skuDetails.getSku();
                                String price = skuDetails.getPrice();

                                switch (sku) {
                                    case SKU_DONATE_1:
                                        donate1.setText(price);
                                        break;
                                    case SKU_DONATE_2:
                                        donate2.setText(price);
                                        break;
                                    case SKU_DONATE_5:
                                        donate5.setText(price);
                                        break;
                                    case SKU_DONATE_10:
                                        donate10.setText(price);
                                        break;
                                }
                            }

                            layoutLoading.setVisibility(View.GONE);
                            layoutDonationButtons.setVisibility(View.VISIBLE);

                            consumePreviousPurchases();

                            break;
                        case BillingResponse.FEATURE_NOT_SUPPORTED:
                        case BillingResponse.SERVICE_DISCONNECTED:
                        case BillingResponse.USER_CANCELED:
                        case BillingResponse.SERVICE_UNAVAILABLE:
                        case BillingResponse.BILLING_UNAVAILABLE:
                        case BillingResponse.ITEM_UNAVAILABLE:
                        case BillingResponse.DEVELOPER_ERROR:
                        case BillingResponse.ITEM_ALREADY_OWNED:
                        case BillingResponse.ITEM_NOT_OWNED:
                        default:
                            Timber.w("unhandled result response code: " + result.getResponseCode());
                        case BillingResponse.ERROR:
                            statusMessageHandler.showInfoMessage(getContext(), "Error: " + result.getResponseCode(), Snackbar.LENGTH_LONG);
                            dismiss();
                            break;

                    }
                }
            });

        } else {
            statusMessageHandler.showInfoMessage(getContext(), "Error: " + billingResponseCode, Snackbar.LENGTH_LONG);
            Timber.e("Problem setting up In-app Billing: " + billingResponseCode);
            dismiss();
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to the
        // In-app Billing service by calling the startConnection() method.
        billingServiceIsConnected = false;

        Timber.d("In-app billing service disconnected");
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        switch (responseCode) {
            case BillingResponse.OK:

                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        consumePurchase(purchase);
                    }
                }

                getDialog().dismiss();

                break;
            case BillingResponse.USER_CANCELED:
                statusMessageHandler.showInfoMessage(getContext(), "Cancelled by user", Toast.LENGTH_LONG);
                break;
            case BillingResponse.FEATURE_NOT_SUPPORTED:
            case BillingResponse.SERVICE_DISCONNECTED:
            case BillingResponse.SERVICE_UNAVAILABLE:
            case BillingResponse.BILLING_UNAVAILABLE:
            case BillingResponse.ITEM_UNAVAILABLE:
            case BillingResponse.DEVELOPER_ERROR:
            case BillingResponse.ITEM_ALREADY_OWNED:
            case BillingResponse.ITEM_NOT_OWNED:
            default:
                Timber.w("unhandled result response code: " + responseCode);
            case BillingResponse.ERROR:
                statusMessageHandler.showInfoMessage(getContext(), "Error: " + responseCode, Snackbar.LENGTH_LONG);
                dismiss();
                break;

        }
    }

    private void consumePurchase(Purchase purchase) {
        billingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(String outToken, int responseCode) {
                if (responseCode == BillingResponse.OK) {
                    // Handle the success of the consume operation.
                    // For example, increase the number of coins inside the user's basket.
                    dismiss();
                    statusMessageHandler.showInfoMessage(getContext(), R.string.thank_you, Snackbar.LENGTH_LONG);
                } else {
                    statusMessageHandler.showInfoMessage(getContext(), "Error consuming: " + responseCode, Snackbar.LENGTH_LONG);
                }
            }
        });
    }

    private void initiatePurchase(String skuId) {
        BillingFlowParams.Builder builder = new BillingFlowParams.Builder().setSku(skuId)
                .setType(BillingClient.SkuType.INAPP);
        int responseCode = billingClient.launchBillingFlow(getActivity(), builder.build());
    }

    private void consumePreviousPurchases() {
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult.getResponseCode() == BillingResponse.OK) {
            List<Purchase> purchases = purchasesResult.getPurchasesList();
            for (Purchase purchase : purchases) {
                billingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(String outToken, int responseCode) {
                        if (responseCode == BillingResponse.OK) {
                            Timber.i("Previous purchase consumed");
                        } else {
                            Timber.e("Error consuming previous purchase: " + responseCode);
                        }
                    }
                });
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onActivityResult(ActivityResultEvent activityResultEvent) {
        Timber.d("activity result received");

        // TODO: this may be not needed anymore with the play billing library
    }

}
