package camera1.themaestrochef.com.cameraappfordogs.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import camera1.themaestrochef.com.cameraappfordogs.Billing.IabBroadcastReceiver;
import camera1.themaestrochef.com.cameraappfordogs.Billing.IabHelper;
import camera1.themaestrochef.com.cameraappfordogs.Billing.IabResult;
import camera1.themaestrochef.com.cameraappfordogs.Billing.Inventory;
import camera1.themaestrochef.com.cameraappfordogs.Billing.QuerySKUAsync;
import camera1.themaestrochef.com.cameraappfordogs.Billing.SkuDetails;
import camera1.themaestrochef.com.cameraappfordogs.R;

public class InAppPurchases extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {
    ArrayList<String> skuList;
    ProgressDialog progressDialog;
    Purchase purchase;
    @BindView(R.id.no_ads_button)
    Button noAdsButton;
    @BindView(R.id.purchase_watermark)
    Button noWatermarkButoon;
    @BindView(R.id.go_pro)
    Button goProButton;
    public Activity activity;
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    String payload = "";
    public static final String DISABLEDADSID = "ads_disabled";
    public static final String WATERMARKDISABLED = "no_watermark";
    public static final String PROVERSIONENABLED = "pro_version";
    Boolean mAdsDisabled;
    Boolean mWatermarkDisabled;
    Boolean mProversionEnabled;
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    SkuDetails details;
    static final String TAG = "InAppPurchase123";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        mHelperSetup();
        setContentView(R.layout.activity_in_app_purchases);
        activity = this;
        ButterKnife.bind(this);

    };

    public void mHelperSetup(){

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlmY9+P8DiB6CSamC2LDr4veGfy/VACf8s/0LIKuPckimBEo2DXSgJngDu02cbCp1GdepN2ShTZW6GMEwlmYpb0gdrF1th4ib+zLxAO55p9Ky3u+TH+2aE35O6fPvOBZwBjJpfMYGpI4d0LEYAyb3q4nsPVvFTo4/yxqFGJPzqMsyGBSlyeOaX7FPX9G3tyPH1UFXLlMo7Ta7RNs5RHPZJLhCJYVSvnUHgdQLs/DVItuXZr2ejIjCQ9nvK7g4yNqejLnHkAT9Z8IAb1qTF4/Z4UOslqy/SgGyJHTHBV0SNIOIDknKgLlAFHFCGtl3EDD8p9JmOFEJXFqq5lMebOyNJQIDAQAB";
        skuList = new ArrayList<String>();
        skuList.add(DISABLEDADSID);
        skuList.add(WATERMARKDISABLED);
        skuList.add(PROVERSIONENABLED);

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(InAppPurchases.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);


                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(true, skuList, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            Log.d(TAG, "Query inventory finished.");
            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
            String mPackagePrice;
            progressDialog.cancel();
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }




            mAdsDisabled = inventory.hasPurchase(DISABLEDADSID);
            mWatermarkDisabled = inventory.hasPurchase(WATERMARKDISABLED);
            mProversionEnabled = inventory.hasPurchase(PROVERSIONENABLED);

            inventory.getPurchase(DISABLEDADSID);
            details = inventory.getSkuDetails(DISABLEDADSID);
            Log.d(TAG, "Query inventory was successful.");
            adsDisabledText();
            progressDialog.hide();
            progressDialog.cancel();
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            camera1.themaestrochef.com.cameraappfordogs.Billing.Purchase purchased = inventory.getPurchase(DISABLEDADSID);
            if (purchased != null) {
                //Your purchase details will be in the purchased object.
                //You can also do the developer payload verification.
            }

        }
    };


    @OnClick(R.id.no_ads_button)
    public void purchaseNoAds() {

        try {
            
            mHelper.launchPurchaseFlow(InAppPurchases.this,DISABLEDADSID, RC_REQUEST,mPurchaseFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
    }
    @OnClick(R.id.purchase_watermark)
    public void purchaseNoWatermark() {
        try {
            mHelper.launchPurchaseFlow(InAppPurchases.this,WATERMARKDISABLED, RC_REQUEST, mPurchaseFinishedListener,payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
    }
    @OnClick(R.id.go_pro)
    public void purchaseProVersion() {
        try {
            mHelper.launchPurchaseFlow(InAppPurchases.this,PROVERSIONENABLED, RC_REQUEST, mPurchaseFinishedListener,payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Tried to make purchase", Toast.LENGTH_SHORT ).show();
    }
    IabResult mIabResult;
//    @OnClick(R.id.no_ads_button_cancel)
//    public void purchaseNoAdsCancel() {
//        try {
//            mHelper.queryInventoryAsync(true, skuList, skuList, mReceivedInventoryListener);
//        } catch (IabHelper.IabAsyncInProgressException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(InAppPurchases.this, CaptureImage.class);
        InAppPurchases.this.startActivity(myIntent);
    }

    @Override
    public void receivedBroadcast() {

    }



    void complain(String message) {
        Log.e(TAG, "**** Loook Here Error: " + message);
        alert("Error: " + message);
    }
    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
    String Tag1 = "onIabcheck";
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, camera1.themaestrochef.com.cameraappfordogs.Billing.Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            Log.d(Tag1, "Purchase successful.");

            if (purchase.getSku().contentEquals(DISABLEDADSID)){
            takeOffAds();
            Log.v(Tag1, "onIabPurchase finished complete Disabled ads");
            }
            else if (purchase.getSku().contentEquals(WATERMARKDISABLED)){
                takeOffWaterMark();
                Log.v(Tag1, "onIabPurchase finished complete watermark");

            }
            else if(purchase.getSku().contentEquals(PROVERSIONENABLED)){
                Log.v(Tag1, "onIabPurchase finished complete goPro");

                goProboth();

            }
            adsDisabledText();
        }

    };




    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {

            if (mHelper == null) return;
            if (result.isFailure())
            {
                Log.v(TAG, "failed again");
                // Handle failure
            }
            else
            {
                camera1.themaestrochef.com.cameraappfordogs.Billing.Purchase adsPurchase = inventory.getPurchase(DISABLEDADSID);
                if (adsPurchase!=null){
                    adsDisabledText();
                }
                else {
                    Log.v("doesnoadstake", "adsPurchase is null");
                }
                try {if (inventory.hasPurchase(DISABLEDADSID)){

                     mHelper.consumeAsync(inventory.getPurchase(DISABLEDADSID), mConsumeFinishedListener);}
                     if (inventory.hasPurchase(WATERMARKDISABLED)){
                    mHelper.consumeAsync(inventory.getPurchase(WATERMARKDISABLED), mConsumeFinishedListener);}
                    if (inventory.hasPurchase(PROVERSIONENABLED)){
                    mHelper.consumeAsync(inventory.getPurchase(PROVERSIONENABLED), mConsumeFinishedListener);}
                    else{
                        Toast.makeText(InAppPurchases.this, "Nothing to consume", Toast.LENGTH_LONG).show();

                    }


                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }

            }

        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        @Override
        public void onConsumeFinished(camera1.themaestrochef.com.cameraappfordogs.Billing.Purchase purchase, IabResult result) {
            if (mHelper == null) return;
            if (result.isSuccess())
            {
                Toast.makeText(InAppPurchases.this, "Thank you for your donation!!", Toast.LENGTH_LONG).show();
            }
            else
            {
                // handle error

                Toast.makeText(InAppPurchases.this, "Your failed!!", Toast.LENGTH_LONG).show();

            }
        }

    };
    public void takeOffAds(){
        SharedPreferences inAppBillingPref = getSharedPreferences("billingPref", 0);
        SharedPreferences.Editor et = inAppBillingPref.edit();
        et.putBoolean("adsboolean", true);
        et.apply();
        boolean test = inAppBillingPref.getBoolean("adsboolean", false);
        Log.v("Check1EditAdsTrue", Boolean.toString(test) );
    }
    public void takeOffWaterMark(){
        SharedPreferences inAppBillingPref = getSharedPreferences("billingPref", 0);
        SharedPreferences.Editor et = inAppBillingPref.edit();
        et.putBoolean("watermarkBoolean", true);
        et.apply();
        boolean test = inAppBillingPref.getBoolean("watermarkBoolean", false);
        Log.v("check1WatrmarkBoolInapp", Boolean.toString(test) );
    }

    public void goProboth(){
        takeOffWaterMark();
        takeOffAds();
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
    String packageName = "camera1.themaestrochef.com.cameraappfordogs";
    public void adsDisabledText(){

        try {

            if (!mAdsDisabled){
                String noAdsText = mHelper.getDescription(packageName, DISABLEDADSID)+" "+ mHelper.getPricesDev(packageName,DISABLEDADSID );
                noAdsButton.setText(noAdsText);


            }
            if (!mWatermarkDisabled) {
                String noWatermarkText = mHelper.getDescription(packageName, WATERMARKDISABLED) + " " + mHelper.getPricesDev(packageName, WATERMARKDISABLED);
                noWatermarkButoon.setText(noWatermarkText);
            }
            if (!mProversionEnabled)     {
                String goProText = mHelper.getDescription(packageName, PROVERSIONENABLED) + " " + mHelper.getPricesDev(packageName, PROVERSIONENABLED);
                goProButton.setText(goProText);
            }
            if (mProversionEnabled){
                noAdsButton.setEnabled(false);
                noWatermarkButoon.setEnabled(false);
                goProButton.setText(R.string.go_pro_string);
                noAdsButton.setPaintFlags(noAdsButton.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                noWatermarkButoon.setPaintFlags(noWatermarkButoon.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

            }
            if(mAdsDisabled){
                noAdsButton.setText(R.string.ads_disabled_string);
                noAdsButton.setEnabled(false);
                goProButton.setPaintFlags(goProButton.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                goProButton.setEnabled(false);

            }
            if (mWatermarkDisabled){
                noWatermarkButoon.setText(R.string.watermark_removed_string);
                noWatermarkButoon.setEnabled(false);
                goProButton.setEnabled(false);
                goProButton.setPaintFlags(goProButton.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);


            }


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}