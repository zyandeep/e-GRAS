package project.mca.e_gras.util;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.androidnetworking.AndroidNetworking;
import com.marcoscg.dialogsheet.DialogSheet;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.goncalves.pugnotification.notification.PugNotification;
import dmax.dialog.SpotsDialog;
import project.mca.e_gras.R;

public class MyUtil {

    private static final String TAG = "MY-APP";
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


    public static void changeLocal(Context context, String language) {          // here the context is Activity context
        Locale newLocal = new Locale(language);
        Locale.setDefault(newLocal);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(newLocal);
        res.updateConfiguration(conf, dm);

        //
        new WebView(context).destroy();
    }


    // check if external storage is available for saving PDFs

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    public static Uri createFile(Context context, InputStream is) {
        // DO FILE I/O IN A BACKGROUND THREAD

        if (isExternalStorageWritable()) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);

            // Make sure the directory exist
            path.mkdirs();

            String fileName = "challan_" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".pdf";
            File file = new File(path, fileName);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                IOUtils.copyLarge(is, fos);
                fos.close();

                // returns a content:// uri
                return FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".fileprovider",
                        file);

            } catch (Exception ex) {
                showBottomDialog(context, ex.getMessage());
            }
        } else {
            showBottomDialog(context, context.getString(R.string.label_write_file_error));
        }

        return null;
    }


    public static void showNotification(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PugNotification.with(context)
                .load()
                .title(context.getString(R.string.label_file_downloaded))
                .message(context.getString(R.string.label_notification))
                .smallIcon(R.drawable.ic_noti_file)
                .largeIcon(R.drawable.notification)
                .flags(Notification.DEFAULT_ALL)
                .click(pendingIntent)
                .autoCancel(true)
                .simple()
                .build();
    }
}