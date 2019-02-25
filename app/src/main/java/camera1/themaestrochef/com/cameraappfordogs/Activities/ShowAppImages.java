package camera1.themaestrochef.com.cameraappfordogs.Activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.google.android.gms.ads.AdView;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import camera1.themaestrochef.com.cameraappfordogs.Adapters.AppImagesAdapter;
import camera1.themaestrochef.com.cameraappfordogs.R;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.AdsUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.PermissionUtilities;
import camera1.themaestrochef.com.cameraappfordogs.Utilities.UiUtilise;

public class ShowAppImages extends AppCompatActivity {

    @BindView(R.id.app_images)
    RecyclerView appImages;

    AppImagesAdapter adapter;
    @Nullable
    @BindView(R.id.adView)
    AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("checkbox", 0);
        boolean cb1 = sp.getBoolean("isLogin", false);
        Log.v("checkbooleanCapture", Boolean.toString(cb1) );
        if (cb1) {
        setContentView(R.layout.activity_show_app_images_no_ads);

    } if (!cb1){
            setContentView(R.layout.activity_show_app_images);
    }
        ButterKnife.bind(this);
        UiUtilise.hideToolBar(this);
        UiUtilise.hideSystemBar(this);
     if (!cb1){

            AdsUtilities.initAds(mAdView);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // call in onResume so that if he deletes image from gallery
        initAppImages();
    }

    private void initAppImages() {
        ArrayList<String> imagesPaths = getAllShownImagesPath();
        adapter = new AppImagesAdapter(this, imagesPaths);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        appImages.setLayoutManager(layoutManager);
        appImages.setAdapter(adapter);
    }

    ArrayList<String> listOfAllImages = new ArrayList<>();

    String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";


    private ArrayList<String> getAllShownImagesPath() {
        if (PermissionUtilities.checkAndRequestPermissions(this))
            loadImages();
        return listOfAllImages;
    }

    private void loadImages() {
        listOfAllImages = new ArrayList<>();
        Cursor externalCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, orderBy);
        int column_index_data = externalCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (externalCursor.moveToNext()) {
            listOfAllImages.add(externalCursor.getString(column_index_data));
        }
    }

}
