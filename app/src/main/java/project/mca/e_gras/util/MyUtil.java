package project.mca.e_gras.util;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.androidnetworking.AndroidNetworking;
import com.marcoscg.dialogsheet.DialogSheet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.NumberFormat;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import project.mca.e_gras.R;

public class MyUtil {
    private static AlertDialog spotDialog;


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


    public static void checkServerReachable(final Context context, final String tag) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean exists = false;

                try {
                    SocketAddress sockaddr = new InetSocketAddress("192.168.43.211", 80);
                    // Create an unbound socket
                    Socket sock = new Socket();

                    // This method will block no more than timeoutMs.
                    // If the timeout occurs, SocketTimeoutException is thrown.
                    int timeoutMs = 2000;   // 2 seconds
                    sock.connect(sockaddr, timeoutMs);
                    exists = true;

                    sock.close();
                } catch (IOException e) {
                }

                return exists;
            }

            @Override
            protected void onPostExecute(Boolean isOK) {
                super.onPostExecute(isOK);

                if (!isOK) {
                    // server unreachable
                    // cancel network request
                    AndroidNetworking.cancel(tag);
                    closeSpotDialog();
                    showBottomDialog(context, context.getString(R.string.error_server_down));
                }
            }
        }.execute();
    }


    public static void showSpotDialog(Context context) {
        if (spotDialog != null && spotDialog.isShowing()) {
            return;
        }

        // initialise the spot alert dialog
        spotDialog = new SpotsDialog.Builder()
                .setContext(context)
                .setCancelable(false)
                .setTheme(R.style.mySpotDialogTheme)
                .build();

        spotDialog.show();
    }


    public static void closeSpotDialog() {
        if (spotDialog != null && spotDialog.isShowing()) {
            spotDialog.dismiss();
        }
    }


    public static String formatCurrency(double amount) {
        Locale locale = new Locale("en", "IN");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        return currencyFormat.format(amount);
    }
}