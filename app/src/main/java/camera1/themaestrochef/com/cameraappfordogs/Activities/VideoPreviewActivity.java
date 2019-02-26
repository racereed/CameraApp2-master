package camera1.themaestrochef.com.cameraappfordogs.Activities;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import camera1.themaestrochef.com.cameraappfordogs.R;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.AdsUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.UiUtilise;


public class VideoPreviewActivity extends AppCompatActivity {

    private VideoView videoView;
    @Nullable
    @BindView(R.id.adView)
    AdView mAdView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences inAppBillingPref = getSharedPreferences("billingPref", 0);
        boolean noAdsBoolean = inAppBillingPref.getBoolean("adsboolean", false);
        if (noAdsBoolean){
        setContentView(R.layout.activity_video_preview_no_ads);
        }
        if(!noAdsBoolean) {
            setContentView(R.layout.activity_video_preview);
        }
        UiUtilise.hideSystemBar(this);
        UiUtilise.hideToolBar(this);

        ButterKnife.bind(this);

        videoView = findViewById(R.id.video);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
            }
        });

        Uri videoUri = Uri.parse(getIntent().getStringExtra("video"));
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float viewWidth = videoView.getWidth();
                lp.height = (int) (viewWidth * (videoHeight / videoWidth));
                videoView.setLayoutParams(lp);
                playVideo();
            }
        });
        if(!noAdsBoolean) {

            AdsUtilities.initAds(mAdView);
        }
    }

    void playVideo() {
        if (videoView.isPlaying()) return;
        videoView.start();
    }

}