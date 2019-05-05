package project.mca.e_gras;

import android.app.Application;
import android.webkit.WebView;

import com.androidnetworking.AndroidNetworking;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Fast AN initialization
        AndroidNetworking.initialize(getApplicationContext());

        //
        new WebView(this).destroy();
    }
}