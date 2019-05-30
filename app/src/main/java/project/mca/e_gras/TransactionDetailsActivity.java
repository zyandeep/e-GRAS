package project.mca.e_gras;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

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

    // downloaded file's ID
    long downloadID;

    BroadcastReceiver myReceiver;


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

        myReceiver = new FileDownloadReceiver();

        // register the myReceiver
        registerReceiver(myReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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


    private void downloadChallan(String idToken) {
        // log data in backend database

        // Check for WRITE_EXTERNAL_STORAGE permission
        Dexter.withActivity(TransactionDetailsActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        downloadFile();
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


    private void downloadFile() {
        Log.d(TAG, "downloadChallan: " + MyUtil.isExternalStorageWritable());

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);

        // Make sure the directory exist
        path.mkdirs();

        String url = "https://www.govst.edu/uploadedFiles/Academics/Colleges_and_Programs/CAS/Trigonometry_Short_Course_Tutorial_Lauren_Johnson.pdf";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("Sample Challan")                 // Title of the Download Notification
                .setDescription("Downloading...")           // Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)      // Visibility of the download Notification
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "test_challan.pdf")
                .setAllowedOverMetered(true)            // Set if download is allowed on Mobile network
                .setVisibleInDownloadsUi(true)
                .setAllowedOverRoaming(true);

        request.allowScanningByMediaScanner();

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myReceiver);
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    // Receiver to receive CONNECTIVITY_CHANGED broadcast intent
    private class FileDownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

                //Fetching the download id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                //Checking if the received broadcast is for our enqueued download by matching download id
                if (downloadID == id) {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
