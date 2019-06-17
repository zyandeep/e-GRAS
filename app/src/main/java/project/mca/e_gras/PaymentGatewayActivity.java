package project.mca.e_gras;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import project.mca.e_gras.util.MyUtil;

import static project.mca.e_gras.MyApplication.BASE_URL;

public class PaymentGatewayActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";
    private static final int DOWNLOAD = 1;
    private static final int VERIFY = 2;

    private WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the app's default Local, manually
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String langValue = sharedPref.getString(getString(R.string.lang_pref_key), getString(R.string.lang_pref_default_value));
        MyUtil.changeLocal(this, langValue);

        setContentView(R.layout.activity_payment_gateway);
        setTitle(R.string.title_payment_gateway);

        String url = getIntent().getStringExtra("url");
        String data = getIntent().getStringExtra("bundle");

        webView = findViewById(R.id.my_web_view);

        // configure settings
        webView.setWebChromeClient(new WebChromeClient());          // So that any pop-ups/alerts get displayed

        webView.setWebViewClient(new WebViewClient() {              // to show and hide progress dialogSheet
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // Display the progress dialogSheet
                MyUtil.showSpotDialog(PaymentGatewayActivity.this);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                MyUtil.closeSpotDialog();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.contains("http://verify_transaction/?")) {
                    // extract the POST params and then
                    // reload web view to verify transaction

                    String params = url.split("\\?")[1];
                    getJWTToken(params, VERIFY);

                    return true;

                } else if (url.contains("http://download_challan/?")) {
                    // extract the POST params and then
                    // download the respective challan

                    String params = url.split("\\?")[1];
                    getJWTToken(params, DOWNLOAD);

                    return true;
                }


                // let my WebView load the page
                return false;
            }
        });


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Enable responsive layout
        webView.getSettings().setUseWideViewPort(true);
        // Zoom out if the content width is greater than the width of the viewport
        webView.getSettings().setLoadWithOverviewMode(true);


        // zoom control settings
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true); // allow pinch to zooom
        webView.getSettings().setDisplayZoomControls(false); // disable the default zoom controls on the page

        // post data to the URL
        try {
            webView.postUrl(url, data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }


    private void getJWTToken(final String query, final int tag) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            MyUtil.showSpotDialog(this);

            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                if (tag == DOWNLOAD) {
                                    MyUtil.closeSpotDialog();
                                    MyUtil.downloadChallan(PaymentGatewayActivity.this, idToken, query);
                                } else {
                                    insertLog(query, idToken);
                                }
                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();

                                if (ex instanceof FirebaseNetworkException) {
                                    MyUtil.showBottomDialog(PaymentGatewayActivity.this, getString(R.string.label_network_error));
                                }
                            }
                        }
                    });
        }
    }


    // for verifying payments from the webView
    private void insertLog(final String reqParams, String idToken) {

        // first write a log and then go for verifying the transaction

        String[] data = reqParams.split("&");
        String dept_id = data[0].split("=")[1];             // dept_id

        Map<String, String> params = new HashMap<>();

        for (String item : data) {
            String[] temp = item.split("=");

            params.put(temp[0], temp[1]);
        }

        JSONObject obj = new JSONObject(params);                // requestparameters

        Log.d(TAG, obj.toString());

        AndroidNetworking.post(BASE_URL + "/verify-payment")
                .addHeaders("Authorization", "Bearer " + idToken)
                .addBodyParameter("dept_id", dept_id)
                .addBodyParameter("req_param", obj.toString())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getBoolean("success")) {
                                MyUtil.closeFileDialog();

                                // reload the webview
                                // post data to the URL
                                try {
                                    webView.postUrl("http://103.8.248.139/challan/models/frmgetgrn.php",
                                            reqParams.getBytes(StandardCharsets.UTF_8));
                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }

                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        MyUtil.displayErrorMessage(PaymentGatewayActivity.this, error);
                    }
                });
    }
}