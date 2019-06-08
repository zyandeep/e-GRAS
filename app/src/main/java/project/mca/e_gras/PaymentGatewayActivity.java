package project.mca.e_gras;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.nio.charset.StandardCharsets;

import project.mca.e_gras.util.MyUtil;

public class PaymentGatewayActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";

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


        WebView webView = findViewById(R.id.my_web_view);

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

                if (url.equals("http://transaction_activity/")) {

                    // finish this activity and go to Transaction list activity
                    Intent intent = new Intent(PaymentGatewayActivity.this, TransactionListActivity.class);
                    startActivity(intent);
                    finish();

                    return true;
                } else if (url.contains("http://download_challan/?")) {
                    // extract the POST params and then
                    // download the respective challan

                    Log.d(TAG, "shouldOverrideUrlLoading: " + true);

                    String params = url.split("\\?")[1];
                    getJWTToken(params);

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


    private void getJWTToken(final String query) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            MyUtil.showSpotDialog(this);

            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                MyUtil.closeSpotDialog();

                                MyUtil.downloadChallan(PaymentGatewayActivity.this, idToken, query);
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
}