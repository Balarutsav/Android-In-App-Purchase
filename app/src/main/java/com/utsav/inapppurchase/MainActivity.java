package com.utsav.inapppurchase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.utsav.inapppurchase.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    BillingClient billingClient;
    SkuDetails skuDetails;
    ActivityMainBinding activityMainBinding;
    PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            Log.e("TAG in App", "billing result response code" + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {
                    handleConsumedPurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.e("Purchase canceled", "by user");
            }
        }
    };

    private void handleConsumedPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                switch (billingResult.getResponseCode()) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.e("handle non consumed purchase", "result found");
                        break;
                    default:
                        Log.e("message for non consume", billingResult.getDebugMessage());
                }
            }
        });
    }

    private void handleNonConsumedPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {

            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        setUpBillingClient();
        init();
    }

    private void init() {

        Activity activity = this;
        activityMainBinding.txtProductBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
                billingClient.launchBillingFlow(activity, billingFlowParams);
            }
        });
    }

    private void setUpBillingClient() {
        billingClient = BillingClient.newBuilder(this).setListener(purchasesUpdatedListener).enablePendingPurchases().build();
        startConnection();

    }

    private void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.e("tag in app", "Setup Billing Done");
                    queryAmiableProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private void queryAmiableProducts() {
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("golden_plan");
        skuList.add("test_product_one");
        skuList.add("test_product_two");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        try {
            billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    Log.e("on skud detials response","result found");
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !list.isEmpty()) {
                        for (SkuDetails skuDetails : list) {
                            Log.v("TAG_INAPP","skuDetailsList :"+skuDetails.getTitle());
                            updateUI(skuDetails);
                        }
                    }else {
                        Log.e("result not found query",billingResult.getDebugMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
        activityMainBinding.txtProductName.setText(skuDetails.getTitle());
        activityMainBinding.txtProductDescription.setText(skuDetails.getDescription());
        showUIElement();

    }

    private void showUIElement() {
        activityMainBinding.txtProductName.setVisibility(View.VISIBLE);
        activityMainBinding.txtProductDescription.setVisibility(View.VISIBLE);
        activityMainBinding.txtProductBuy.setVisibility(View.VISIBLE);
    }

}