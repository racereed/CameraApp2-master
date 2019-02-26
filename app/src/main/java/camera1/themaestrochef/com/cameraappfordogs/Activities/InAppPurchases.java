package camera1.themaestrochef.com.cameraappfordogs.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import camera1.themaestrochef.com.cameraappfordogs.Billing.BillingManager;
import camera1.themaestrochef.com.cameraappfordogs.R;

public class InAppPurchases extends AppCompatActivity {
    @BindView(R.id.no_ads_button)
    Button noAdsButton;
    public  Activity activity;
    public boolean adsDisabled;
    BillingManager billingManager;
    //public static final String DISABLEDADSID = "ads_disabled";
    public static final String DISABLEDADSID = "ads_disabled";
    public static final String WATERMARKDISABLED = "no_watermark";
    public static final String PROVERSIONENABLED = "pro_version";



    public static final String LICENSEKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlmY9+P8DiB6CSamC2LDr4veGfy/VACf8s/0LIKuPckimBEo2DXSgJngDu02cbCp1GdepN2ShTZW6GMEwlmYpb0gdrF1th4ib+zLxAO55p9Ky3u+TH+2aE35O6fPvOBZwBjJpfMYGpI4d0LEYAyb3q4nsPVvFTo4/yxqFGJPzqMsyGBSlyeOaX7FPX9G3tyPH1UFXLlMo7Ta7RNs5RHPZJLhCJYVSvnUHgdQLs/DVItuXZr2ejIjCQ9nvK7g4yNqejLnHkAT9Z8IAb1qTF4/Z4UOslqy/SgGyJHTHBV0SNIOIDknKgLlAFHFCGtl3EDD8p9JmOFEJXFqq5lMebOyNJQIDAQAB";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_purchases);
        activity = this;
        ButterKnife.bind(this);
        billingManager = new BillingManager(InAppPurchases.this, new MyBillingUpdateListener());

    }

    @OnClick(R.id.no_ads_button)
    public void purchaseNoAds() {
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
        billingManager.initiatePurchaseFlow(DISABLEDADSID, null, BillingClient.SkuType.INAPP);
    }
    @OnClick(R.id.purchase_watermark)
    public void purchaseNoWatermark() {
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
        billingManager.initiatePurchaseFlow(WATERMARKDISABLED, null, BillingClient.SkuType.INAPP);
    }
    @OnClick(R.id.go_pro)
    public void purchaseProVersion() {
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
        billingManager.initiatePurchaseFlow(PROVERSIONENABLED, null, BillingClient.SkuType.INAPP);
    }
    @OnClick(R.id.no_ads_button_cancel)
    public void purchaseNoAdsCancel() {
        Toast.makeText(this, "cancelled purchase", Toast.LENGTH_SHORT ).show();
        billingManager.consumeDisabledAds();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(InAppPurchases.this, CaptureImage.class);
        InAppPurchases.this.startActivity(myIntent);
    }
    class MyBillingUpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {

        billingManager.queryPurchases();
        }

        @Override
        public void onConsumeFinished(String token, int result) {

            if (result == BillingClient.BillingResponse.OK) {
            }
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchases) {

            for (Purchase p : purchases) {

                //update ui

            }
        }
    }


}
