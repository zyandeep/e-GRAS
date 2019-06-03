package project.mca.e_gras.util;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.marcoscg.dialogsheet.DialogSheet;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import dmax.dialog.SpotsDialog;
import project.mca.e_gras.PaymentGatewayActivity;
import project.mca.e_gras.R;

import static project.mca.e_gras.MyApplication.BASE_URL;
import static project.mca.e_gras.MyApplication.HOST_NAME;

public class MyUtil {

    private static final String TAG = "MY-APP";
    private static AlertDialog spotDialog;
    private static androidx.appcompat.app.AlertDialog fileDialog;


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
                    SocketAddress sockaddr = new InetSocketAddress(HOST_NAME, 80);
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

    public static void showFileDialog(Context context) {
        if (fileDialog != null && fileDialog.isShowing()) {
            return;
        }

        // Build the custome alert dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(R.layout.custome_dialog_layout);

        fileDialog = builder.create();

        fileDialog.show();
    }

    public static void closeFileDialog() {
        if (fileDialog != null && fileDialog.isShowing()) {
            fileDialog.dismiss();
        }
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


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build a notification channel for Android O > devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel =
                    new NotificationChannel("default_channel", "Challan Download",
                            NotificationManager.IMPORTANCE_HIGH);

            defaultChannel.setShowBadge(true);

            notificationManager.createNotificationChannel(defaultChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default_channel")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_noti_file)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification))
                .setContentTitle(context.getString(R.string.label_file_downloaded))
                .setContentText(context.getString(R.string.label_notification))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(23, builder.build());

    }


    public static void downloadChallan(final Context context, final String token, final String tag, final int id) {

        // Check for WRITE_EXTERNAL_STORAGE permission
        Dexter.withActivity((AppCompatActivity) context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        downloadFile(context, token, tag, id);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Log.d(TAG, "Permission Denied");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private static void downloadFile(final Context context, final String token, final String tag, final int id) {
        // log data in backend database

        if (!AndroidNetworking.isRequestRunning(tag)) {

            MyUtil.showSpotDialog(context);

            // check for server reachability
            MyUtil.checkServerReachable(context, tag);

            AndroidNetworking.get(BASE_URL + "/download-challan")
                    .addHeaders("Authorization", "Bearer " + token)
                    .addQueryParameter("id", String.valueOf(id))
                    .setTag(tag)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // if status code is OK:200 then only
                            MyUtil.closeSpotDialog();

                            try {
                                if (response.getBoolean("success")) {

                                    Log.d(TAG, "response: " + response);

                                    String url = response.getString("url");
                                    String data = response.getString("data");

                                    // row id; primary key
                                    int rowId = response.getInt("id");

                                    new DownloadFileTask(context, token, rowId).execute("https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf", data);
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            displayErrorMessage(context, anError);
                        }
                    });
        }

    }


    public static void displayErrorMessage(Context context, ANError anError) {
        MyUtil.closeSpotDialog();
        MyUtil.closeFileDialog();

        if (anError.getErrorCode() != 0) {
            // received error from server
            String jsonString = anError.getErrorBody();

            try {
                JSONObject obj = new JSONObject(jsonString);
                MyUtil.showBottomDialog(context, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }

    private static void updateLog(final Context context, final Uri uri, final String idToken, int id) {

        AndroidNetworking.post(BASE_URL + "/update-challan")
                .addHeaders("Authorization", "Bearer " + idToken)
                .addBodyParameter("id", String.valueOf(id))             // the row id
                .addBodyParameter("data", uri.toString())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, "update: " + response);

                        try {
                            if (response.getBoolean("success")) {
                                MyUtil.closeFileDialog();

                                // show a notification
                                if (uri != null) {
                                    MyUtil.showNotification(context, uri);
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        MyUtil.displayErrorMessage(context, error);
                    }
                });
    }

    public static void verifyPayment(final Context context, final String token, final String tag, final int id) {
        if (!AndroidNetworking.isRequestRunning(tag)) {

            MyUtil.showSpotDialog(context);

            // check for server reachability
            MyUtil.checkServerReachable(context, tag);

            AndroidNetworking.get(BASE_URL + "/verify-payment/{id}")
                    .addHeaders("Authorization", "Bearer " + token)
                    .addPathParameter("id", String.valueOf(id))
                    .setTag(tag)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // if status code is OK:200 then only
                            MyUtil.closeSpotDialog();

                            try {
                                if (response.getBoolean("success")) {
                                    // get the url and data and open it in the webView
                                    String url = response.getString("url");
                                    String data = response.getString("data");

                                    Log.d(TAG, "verify payment: " + url + "\n" + data);

                                    Intent intent = new Intent(context, PaymentGatewayActivity.class);
                                    intent.putExtra("url", url);
                                    intent.putExtra("bundle", data);
                                    context.startActivity(intent);

                                    ((AppCompatActivity) context).finish();
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            MyUtil.displayErrorMessage(context, anError);
                        }
                    });
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /// ASYNCTASK TO DOWNLOAD THE FILE
    private static class DownloadFileTask extends AsyncTask<String, Void, Uri> {

        private String idToken;
        private int id;
        private Context context;

        public DownloadFileTask(Context context, String idToken, int id) {
            this.idToken = idToken;
            this.id = id;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MyUtil.showFileDialog(context);
        }

        @Override
        protected Uri doInBackground(String... strings) {
            InputStream is = null;
            OutputStream os = null;
            HttpURLConnection conn = null;

            try {
                URL url = new URL(strings[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);                                             // in milliseconds
                conn.setConnectTimeout(15000);
                conn.setDoInput(true);

               /* conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                conn.setDoOutput(true);                                               // to POST

                os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(strings[1]);
                writer.flush();
                writer.close();*/

                conn.connect();

                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    // input stream to read file
                    is = conn.getInputStream();

                    return MyUtil.createFile(context, is);
                }
            } catch (Exception e) {
                MyUtil.showBottomDialog(context, e.getMessage());
            } finally {
                conn.disconnect();

                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        is.close();
                    }
                } catch (Exception ex) {
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(Uri uri) {
            // update log data in the database
            updateLog(context, uri, idToken, id);
        }
    }

}