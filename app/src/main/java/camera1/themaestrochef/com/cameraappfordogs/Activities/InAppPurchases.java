package camera1.themaestrochef.com.cameraappfordogs.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import camera1.themaestrochef.com.cameraappfordogs.R;

public class InAppPurchases extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    @BindView(R.id.no_ads_button)
    Button noAdsButton;
    public  Activity activity;
    public static BillingProcessor bp;
    public boolean adsDisabled;
    //public static final String DISABLEDADSID = "ads_disabled";
    public static final String DISABLEDADSID = "ads_disabled";
    public static final String LICENSEKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlmY9+P8DiB6CSamC2LDr4veGfy/VACf8s/0LIKuPckimBEo2DXSgJngDu02cbCp1GdepN2ShTZW6GMEwlmYpb0gdrF1th4ib+zLxAO55p9Ky3u+TH+2aE35O6fPvOBZwBjJpfMYGpI4d0LEYAyb3q4nsPVvFTo4/yxqFGJPzqMsyGBSlyeOaX7FPX9G3tyPH1UFXLlMo7Ta7RNs5RHPZJLhCJYVSvnUHgdQLs/DVItuXZr2ejIjCQ9nvK7g4yNqejLnHkAT9Z8IAb1qTF4/Z4UOslqy/SgGyJHTHBV0SNIOIDknKgLlAFHFCGtl3EDD8p9JmOFEJXFqq5lMebOyNJQIDAQAB";
    public void edtitAdsBooleanTrue(){
        SharedPreferences sp = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putBoolean("isLogin", true);
        et.apply();
        boolean test = sp.getBoolean("isLogin", false);
        Log.v("CheckEditAdsTrue", Boolean.toString(test) );
    }

    public void editadsBooleanFalse(){
        SharedPreferences sp = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putBoolean("isLogin", false);
        et.apply();
        boolean test = sp.getBoolean("isLogin", false);
        Log.v("checkEditadsFalse", Boolean.toString(test) );
    }
    public static void startBillingProcessor(Context context, BillingProcessor.IBillingHandler handler){
        bp = new BillingProcessor(context,LICENSEKEY,handler);
        bp.initialize();
        bp.getPurchaseTransactionDetails(DISABLEDADSID);
        bp.isPurchased(DISABLEDADSID);
        bp.loadOwnedPurchasesFromGoogle();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_purchases);
        activity = this;
        ButterKnife.bind(this);
        onBillingInitialized();
        onPurchaseHistoryRestored();
    }


    @OnClick(R.id.no_ads_button)
    public void purchaseNoAds() {
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
        bp.purchase(this, DISABLEDADSID);

        if (bp.isPurchased(DISABLEDADSID))
            Log.v("123purchased?", "true");
        if (!bp.isPurchased(DISABLEDADSID))
            Log.v("123purchased?", "false");
        else
            Log.v("123purchased?", "fucked up");

    }
    @OnClick(R.id.no_ads_button_cancel)
    public void purchaseNoAdsCancel() {
        Toast.makeText(this, "cancelled purchase", Toast.LENGTH_SHORT ).show();
        bp.consumePurchase(DISABLEDADSID);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(InAppPurchases.this, CaptureImage.class);
        InAppPurchases.this.startActivity(myIntent);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        if (productId.equals(DISABLEDADSID));{
            edtitAdsBooleanTrue();
        }
           }

    @Override
    public void onPurchaseHistoryRestored() {
        if (bp.isPurchased(DISABLEDADSID)){
            edtitAdsBooleanTrue();

        }

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Toast.makeText(this, "cancelled purchase", Toast.LENGTH_SHORT ).show();

    }

    @Override
    public void onBillingInitialized() {
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp.handleActivityResult(requestCode, resultCode,data))
          //  bp.purchase(InAppPurchases.this,DISABLEDADSID);
            onProductPurchased(DISABLEDADSID, null);
       super.onActivityResult(requestCode, resultCode, data);


    }
}
