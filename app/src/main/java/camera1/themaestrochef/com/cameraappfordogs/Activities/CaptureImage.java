package camera1.themaestrochef.com.cameraappfordogs.Activities;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import camera1.themaestrochef.com.cameraappfordogs.Adapters.SoundsAdapter;
import camera1.themaestrochef.com.cameraappfordogs.Billing.IabBroadcastReceiver;
import camera1.themaestrochef.com.cameraappfordogs.Billing.IabHelper;
import camera1.themaestrochef.com.cameraappfordogs.Billing.IabResult;
import camera1.themaestrochef.com.cameraappfordogs.Billing.Inventory;
import camera1.themaestrochef.com.cameraappfordogs.Billing.SkuDetails;
import camera1.themaestrochef.com.cameraappfordogs.Models.AnimalsModel;
import camera1.themaestrochef.com.cameraappfordogs.R;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.AdsUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.CapturePhotoUtils;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.ImageHelper;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.PermissionUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.SharedPreferencesUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.UiUtilise;

public class CaptureImage extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    InAppPurchases inAppPurchases;
    static final String TAG = "CaptureImage123";
    private static final String CAMERA_FACING_MODE = "camera_facing_mode";
    private static final String CAMERA_MODE_FRONT = "FRONT";
    private AudioManager mAudioManager;
    public boolean noWatermarkPurchased;
    @BindView(R.id.animals_icon)
    RecyclerView animalsList;
    @Nullable
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.camera)
    CameraView mCameraView;
    SkuDetails  mSkuDetails;
    @BindView(R.id.switch_flash)
    ImageView flashIcon;
    List<String> skuDetails;


    @BindView(R.id.last_captured_image)
    ImageView lastImage;
    SoundsAdapter adapter;

    boolean isPunchable =true;

    private static final Flash[] FLASH_OPTIONS = {
            Flash.OFF,
            Flash.ON,
            Flash.AUTO
    };



    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
            R.drawable.ic_flash_auto
    };

    private int mCurrentFlash;


    @Override
    public void onDestroy() {

        mCameraView.destroy();
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



    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelperSetup();
        SharedPreferences inAppBillingPref = getSharedPreferences("billingPref", 0);
        boolean noAdsBoolean = inAppBillingPref.getBoolean("adsboolean", false);
        noWatermarkPurchased = inAppBillingPref.getBoolean("watermarkBoolean", false);

        Log.v("check1noadsbooleanCap", Boolean.toString(noAdsBoolean) );
        Log.v("check1watermarboolCap", Boolean.toString(noAdsBoolean) );


        if (noAdsBoolean) {
                setContentView(R.layout.content_main_no_ad);
            } if (!noAdsBoolean){
                setContentView(R.layout.content_main);
            }

        ButterKnife.bind(this);
        //Hide notificationBar
        UiUtilise.hideSystemBar(this);
        UiUtilise.hideToolBar(this);
        initIcons();

        if (mCameraView != null) {
            mCameraView.addCameraListener(new CameraListener() {
                @Override
                public void onPictureTaken(final byte[] jpeg) {
                    super.onPictureTaken(jpeg);
                    saveImg(jpeg);
                }
            });
        }
        if (savedInstanceState != null) {
            String mode = savedInstanceState.getString(CAMERA_FACING_MODE);
            if (mode != null)
                if (mode.equals(CAMERA_MODE_FRONT))
                    mCameraView.setFacing(Facing.FRONT);

        } if (!noAdsBoolean)
        AdsUtilities.initAds(mAdView);

    }


    private void mHelperSetup(){
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlmY9+P8DiB6CSamC2LDr4veGfy/VACf8s/0LIKuPckimBEo2DXSgJngDu02cbCp1GdepN2ShTZW6GMEwlmYpb0gdrF1th4ib+zLxAO55p9Ky3u+TH+2aE35O6fPvOBZwBjJpfMYGpI4d0LEYAyb3q4nsPVvFTo4/yxqFGJPzqMsyGBSlyeOaX7FPX9G3tyPH1UFXLlMo7Ta7RNs5RHPZJLhCJYVSvnUHgdQLs/DVItuXZr2ejIjCQ9nvK7g4yNqejLnHkAT9Z8IAb1qTF4/Z4UOslqy/SgGyJHTHBV0SNIOIDknKgLlAFHFCGtl3EDD8p9JmOFEJXFqq5lMebOyNJQIDAQAB";

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
                mBroadcastReceiver = new IabBroadcastReceiver(CaptureImage.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {

                    mHelper.queryInventoryAsync(true, skuDetails, mGotInventoryListener);
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

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            SkuDetails skuDetails = inventory.getSkuDetails(InAppPurchases.DISABLEDADSID);
            if(skuDetails!=null){

                String price = skuDetails.getPrice();
                Log.v(TAG, price);

            }
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            if (inventory.hasPurchase(InAppPurchases.DISABLEDADSID)){
                takeOffAds();
                Log.v(TAG, "onIabPurchase finished complete Disabled ads");
            }
             if (inventory.hasPurchase(InAppPurchases.WATERMARKDISABLED)){
                takeOffWaterMark();
                Log.v(TAG, "onIabPurchase finished complete watermark");

            }
             if(inventory.hasPurchase(InAppPurchases.PROVERSIONENABLED)){

                goProboth();
                 Log.v(TAG, "onIabPurchase finished complete goPro");


             }
            //Your purchase details will be in the purchased object.
            //You can also do the developer payload verification.


        }
    };

    private void initIcons() {
        mCurrentFlash = SharedPreferencesUtilities.getFlashIndex(this);
        flashIcon.setImageResource(FLASH_ICONS[mCurrentFlash]);
        mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        isPunchable = SharedPreferencesUtilities.getPinchValue(this);
        mCameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        boolean isRotated = false;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isRotated = true;
        }
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, isRotated ? 1 : 0, false);
        animalsList.setLayoutManager(layoutManager);
        ArrayList<AnimalsModel> list = new ArrayList<>();
        list.add(new AnimalsModel(R.drawable.bell_button, R.raw.bell_sound));
        list.add(new AnimalsModel(R.drawable.baby_button, R.raw.baby_sound));
        list.add(new AnimalsModel(R.drawable.squeaky_toy, R.raw.squeakytoy1));
        list.add(new AnimalsModel(R.drawable.puppy_button   , R.raw.puppy_sound));
        list.add(new AnimalsModel(R.drawable.dog_button, R.raw.dog_sound));
        //adapter here
        adapter = new SoundsAdapter(list, this);
        animalsList.setAdapter(adapter);
    }
    private void saveImg(final byte[] jpeg) {

        CameraUtils.decodeBitmap(jpeg, new CameraUtils.BitmapCallback() {
            @Override
            public void onBitmapReady(final Bitmap bitmap) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!noWatermarkPurchased) {
                            if (PermissionUtilities.checkAndRequestPermissions(CaptureImage.this)) {

                                Bitmap waterMarkedImage = ImageHelper.addWatermark
                                        (getResources(),
                                                mCameraView.getFacing() == Facing.FRONT
                                                        ? ImageHelper.flipImage(bitmap)
                                                        : bitmap,getApplicationContext());

                                final String imgPath = CapturePhotoUtils.insertImage
                                        (getContentResolver(),
                                                waterMarkedImage,
                                                "Captured Image", "Image Description");
                                if (imgPath != null)
                                    lastImage.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Glide.with(CaptureImage.this).load(imgPath).into(lastImage);
                                        }
                                    });
                            }
                        } if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || noWatermarkPurchased){
                            final String imgPath = CapturePhotoUtils.insertImage(getContentResolver(), bitmap, "Captured Image", "Image Description");
                            if (imgPath != null)
                                lastImage.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(CaptureImage.this).load(imgPath).into(lastImage);
                                    }
                                });
                        }
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mCameraView.start();
        if (PermissionUtilities.checkAndRequestPermissions(this)) {
            Bitmap bitmap = ImageHelper.getLastTakenImage(CaptureImage.this);
            if (bitmap != null)
                lastImage.setImageBitmap(bitmap);
            else
                lastImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtilities.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @OnClick(R.id.take_picture)
    public void capturePic() {
        if (mCameraView != null)
            mCameraView.capturePicture();
    }

    @OnClick(R.id.switch_flash)
    public void switchFlash() {
        if (mCameraView != null) {
            mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
            flashIcon.setImageResource(FLASH_ICONS[mCurrentFlash]);
            mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
            SharedPreferencesUtilities.setFlash(this, mCurrentFlash);
        }
    }

    @OnClick(R.id.switch_camera)
    public void switchCamera() {
        mCameraView.toggleFacing();
    }



    @OnClick(R.id.last_captured_image)
    public void showImages() {
        Intent intent = new Intent(this, ShowAppImages.class);
        startActivity(intent);
    }

    @OnClick(R.id.imageView)
    public void openVideo() {
        Intent intent = new Intent(this, CaptureVideo.class);
        startActivity(intent);
        finish();
    }

    //take this off to enable the next page to open to buy upgrades.
    int i=0;

    @OnClick(R.id.settings_wheel)
    public void openInAppPurchasesActivity(){
      //  bp.purchase(CaptureImage.this, "android.test.purchased");

            Intent intent = new Intent(this, InAppPurchases.class);
            finish();
            startActivity(intent);

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCameraView.getFacing() == Facing.FRONT) {
            outState.putString(CAMERA_FACING_MODE, CAMERA_MODE_FRONT);
        }
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

    @Override
    public void receivedBroadcast() {

    }
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


}
