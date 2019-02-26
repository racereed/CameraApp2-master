package camera1.themaestrochef.com.cameraappfordogs.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import camera1.themaestrochef.com.cameraappfordogs.Billing.BillingManager;
import camera1.themaestrochef.com.cameraappfordogs.R;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.AdsUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.CapturePhotoUtils;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.ImageHelper;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.PermissionUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.SharedPreferencesUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.UiUtilise;

public class CaptureImage extends AppCompatActivity {
    private static final String CAMERA_FACING_MODE = "camera_facing_mode";
    private static final String CAMERA_MODE_FRONT = "FRONT";
    private AudioManager mAudioManager;
    public boolean noWatermarkPurchased;

    @Nullable
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.camera)
    CameraView mCameraView;

    @BindView(R.id.switch_flash)
    ImageView flashIcon;


    @BindView(R.id.last_captured_image)
    ImageView lastImage;

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
    }

    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mPlayer = null;

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }
    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // AUDIOFOCUS_LOSS TRANSIENT means we have lost audio focus for a short amount of time
                // and AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK means we have lost audio focus
                // our app still continues to play song at lower volume but in both cases,
                // we want our app to pause playback and start it from beginning.
                mPlayer.pause();

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // it means we have gained focused and start playback
                mPlayer.start();


            releaseMediaPlayer();
            }
        }
    };


    public void checkPurchases(){
//        if(InAppPurchases.bp.loadOwnedPurchasesFromGoogle()&& InAppPurchases.bp.isPurchased(InAppPurchases.DISABLEDADSID)) {
//            SharedPreferences inAppBillingPref = getSharedPreferences("billingPref", MODE_PRIVATE);
//            SharedPreferences.Editor et = inAppBillingPref.edit();
//            et.putBoolean("adsboolean", true);
//            et.apply();
//        }
//        else{
//            Log.v("checkTransactionDetails", "transactiondetailsarenull");
//
//        }
    }
    BillingManager billingManager;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPurchases();
        billingManager = new BillingManager(CaptureImage.this, new MyBillingUpdateListener());
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



    private void initIcons() {
        mCurrentFlash = SharedPreferencesUtilities.getFlashIndex(this);
        flashIcon.setImageResource(FLASH_ICONS[mCurrentFlash]);
        mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);

        isPunchable = SharedPreferencesUtilities.getPinchValue(this);

                mCameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);

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


    public void makeVolumeToast(){

        // Get the AudioManager instance
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        // Get the music current volume level
        if (am != null) {
            int music_volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Get the device music maximum volume level
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (music_volume_level < 2){
            Toast.makeText(getApplicationContext(),"Volume Might Be Too Low",Toast.LENGTH_LONG).show();// Set your own toast  message
        }}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtilities.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    MediaPlayer mPlayer;



    @OnClick(R.id.sound_toy)
    public void playToySound() {

        Log.i("hellow", "toy sound works");
        Log.i("hellow", "toy sound works");
        mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.squeakytoy2);
        mPlayer.start();//Start playing the music
        makeVolumeToast();

    }

    @OnClick(R.id.puppy_button)
    public void playPuppySound () {

        mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.puppy_sound);
        mPlayer.start();//Start playing the music
        makeVolumeToast();
    }
    @OnClick(R.id.dog_button)
    public void playDogSound () {
        mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.dog_sound);
        mPlayer.start();//Start playing the music
        makeVolumeToast();
    }

    @OnClick(R.id.bell_button)
    public void playBellSound () {
        mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.bell_sound);
        mPlayer.start();//Start playing the music
        makeVolumeToast();
    }

    @OnClick(R.id.baby_button)
    public void playBabySound () {
        mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.baby_sound);
        mPlayer.start();//Start playing the music
        makeVolumeToast();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
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
        public void onPurchasesUpdated(List<Purchase > purchases) {

            for (Purchase p : purchases) {

                //update ui

            }

        }

    }


}
