package project.mca.e_gras;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import project.mca.e_gras.databinding.ActivityTransactionDetailsBinding;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    public static final String BASE_URL = "http://192.168.43.211/api";
    private static final String TAG_VERIFY_PAYMENT = "verify_payment";
    private static final String TAG_DOWNLOAD_CHALLAN = "download_challan";

    Button actionButton;

    // all the details of a transaction
    TransactionModel model;

    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ANDROID DATA BINDING WITH JETPACK LIBRARY
        // receive the bundle
        String json = getIntent().getStringExtra("data");
        model = new Gson().fromJson(json, TransactionModel.class);

        ActivityTransactionDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_details);
        binding.setModel(model);

        setTitle(R.string.label_tran_details);

        actionButton = findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getStatus().equalsIgnoreCase("Y")) {
                    // download challan
                    getJWTToken(TAG_DOWNLOAD_CHALLAN);
                } else {
                    // verify payment
                    getJWTToken(TAG_VERIFY_PAYMENT);
                }
            }
        });


        // Build the custome alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.custome_dialog_layout);

        dialog = builder.create();
    }


    private void getJWTToken(final String tag) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                switch (tag) {
                                    case TAG_DOWNLOAD_CHALLAN:
                                        downloadChallan(idToken);
                                        break;

                                    case TAG_VERIFY_PAYMENT:
                                        verifyPayment(idToken);
                                        break;
                                }

                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();
                                MyUtil.showBottomDialog(TransactionDetailsActivity.this, ex.getMessage());
                            }
                        }
                    });
        }
    }


    private void downloadChallan(final String idToken) {

        // Check for WRITE_EXTERNAL_STORAGE permission
        Dexter.withActivity(TransactionDetailsActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        downloadFile(idToken);
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


    private void downloadFile(String idToken) {
        // log data in backend database

        if (!AndroidNetworking.isRequestRunning(TAG_DOWNLOAD_CHALLAN)) {

            MyUtil.showSpotDialog(this);

            // check for server reachability
            MyUtil.checkServerReachable(TransactionDetailsActivity.this, TAG_DOWNLOAD_CHALLAN);

            AndroidNetworking.get(BASE_URL + "/download-challan")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .addQueryParameter("id", String.valueOf(model.getId()))
                    .setTag(TAG_DOWNLOAD_CHALLAN)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // if status code is OK:200 then only
                            MyUtil.closeSpotDialog();

                            try {
                                if (response.getBoolean("success")) {
                                    String url = response.getString("url");

                                    Map<String, String> params = new HashMap<>();
                                    Type type = new TypeToken<HashMap<String, String>>() {
                                    }.getType();
                                    params = new Gson().fromJson(String.valueOf(response.getJSONObject("data")), type);

                                    new DownloadFileTask(params).execute("http://www.axmag.com/download/pdfurl-guide.pdf");
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            displayErrorMessage(anError);
                        }
                    });
        }

    }


    private void verifyPayment(String idToken) {
        if (!AndroidNetworking.isRequestRunning(TAG_VERIFY_PAYMENT)) {

            MyUtil.showSpotDialog(this);

            // check for server reachability
            MyUtil.checkServerReachable(TransactionDetailsActivity.this, TAG_VERIFY_PAYMENT);

            AndroidNetworking.get(BASE_URL + "/verify-payment/{id}")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .addPathParameter("id", String.valueOf(model.getId()))
                    .setTag(TAG_VERIFY_PAYMENT)
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

                                    Intent intent = new Intent(TransactionDetailsActivity.this, PaymentGatewayActivity.class);
                                    intent.putExtra("url", url);
                                    intent.putExtra("bundle", data);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            displayErrorMessage(anError);
                        }
                    });
        }
    }


    private void displayErrorMessage(ANError anError) {
        MyUtil.closeSpotDialog();

        if (anError.getErrorCode() != 0) {
            // received error from server
            String jsonString = anError.getErrorBody();

            try {
                JSONObject obj = new JSONObject(jsonString);
                MyUtil.showBottomDialog(TransactionDetailsActivity.this, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /// ASYNCTASK TO DOWNLOAD THE FILE
    private class DownloadFileTask extends AsyncTask<String, Void, Uri> {

        // data to POST
        private Map<String, String> params;


        public DownloadFileTask(Map<String, String> params) {
            this.params = params;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
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
                conn.setRequestMethod("GET");
                //conn.setDoOutput(true);                                               // to POST
                conn.connect();


                int response = conn.getResponseCode();

                if (response == 200) {
                    // input stream to read file
                    is = conn.getInputStream();

                    return MyUtil.createFile(TransactionDetailsActivity.this, is);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                conn.disconnect();

                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception ex) {
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(Uri uri) {
            dialog.dismiss();

            // show a notification
            if (uri != null) {
                MyUtil.showNotification(TransactionDetailsActivity.this, uri);
            }
        }
    }
}
