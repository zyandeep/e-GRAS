package project.mca.e_gras;

import android.app.Application;
import android.webkit.WebView;

import com.androidnetworking.AndroidNetworking;

public class MyApplication extends Application {

    //public static final String BASE_URL = "http://10.177.15.95/api";
    //public static final String HOST_NAME = "10.177.15.95";

    // Home server
    public static final String BASE_URL = "http://192.168.43.211/api";
    public static final String HOST_NAME = "192.168.43.211";

    @Override
    public void onCreate() {
        super.onCreate();

        // Fast AN initialization
        AndroidNetworking.initialize(getApplicationContext());

        //
        new WebView(this).destroy();
    }
}