package project.mca.e_gras.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import com.marcoscg.dialogsheet.DialogSheet;

import androidx.core.content.ContextCompat;
import project.mca.e_gras.R;

public class MyUtil {

    public static final String TAG = "MY-APP";


    public static void showBottomDialog(Context context, String msg) {
        // show the bottomSheet dialog
        // context : Activity context not Application's
        new DialogSheet(context)
                .setTitle(R.string.error_label_bottom_dialog)
                .setMessage(msg)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close the dialog
                    }
                })
                .setRoundedCorners(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
                .show();
    }


    public static void showBottomDialog(Context context, String msg, final boolean restartApp) {
        new DialogSheet(context)
                .setTitle(R.string.error_label_bottom_dialog)
                .setMessage(msg)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (restartApp) {

                            // restart the application
                            System.exit(0);
                        }
                    }
                })
                .setRoundedCorners(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
                .show();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
