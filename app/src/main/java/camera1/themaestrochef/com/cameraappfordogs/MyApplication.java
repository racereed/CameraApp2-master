package camera1.themaestrochef.com.cameraappfordogs;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
       MobileAds.initialize(this, "ca-app-pub-3924158907329616~9536912933");
    }
}
