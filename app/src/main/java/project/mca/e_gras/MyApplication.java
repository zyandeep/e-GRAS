package project.mca.e_gras;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApplication extends Application implements LifecycleObserver {

    private static final String TAG = "MY-APP";

    private MyNetworkReceiver myReceiver;


    @Override
    public void onCreate() {
        super.onCreate();

        myReceiver = new MyNetworkReceiver();

        // add lifecycle observer for the whole application process
        ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .addObserver(this);

        // Fast AN initialization
        AndroidNetworking.initialize(getApplicationContext());
    }


    ////////////////////////////////////////////////////////////////////////
    // Lifecycle Observer methods

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground() {

        // register the receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        // register the receiver dynamically
        getApplicationContext().registerReceiver(myReceiver, filter);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appInBackground() {

        // unregister the receiver
        getApplicationContext().unregisterReceiver(myReceiver);
    }

    private void checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, "No network connection available. You are now offline", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "You are back online", Toast.LENGTH_SHORT).show();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Receiver to receive CONNECTIVITY_CHANGED broadcast intent
    private class MyNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {

                checkNetworkConnectivity();
            }
        }
    }
}
