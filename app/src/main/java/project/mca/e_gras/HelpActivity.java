package project.mca.e_gras;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import project.mca.e_gras.util.MyUtil;

public class HelpActivity extends AppCompatActivity {

    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the app's default Local, manually
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String langValue = sharedPref.getString(getString(R.string.lang_pref_key), getString(R.string.lang_pref_default_value));
        MyUtil.changeLocal(this, langValue);

        setContentView(R.layout.activity_help);
        setTitle(R.string.label_help);


        webView = findViewById(R.id.help_web_view);

        // configure settings
        webView.setWebChromeClient(new WebChromeClient());          // So that any pop-ups/alerts get displayed

        webView.setWebViewClient(new WebViewClient() {              // to show and hide progress dialogSheet
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // Display the progress dialogSheet
                MyUtil.showSpotDialog(HelpActivity.this);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                MyUtil.closeSpotDialog();
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

        // load the help page
        webView.loadUrl("https://developer.mozilla.org/en-US/docs/Learn");
    }

    @Override
    public void onBackPressed() {
        // if there's history in the webview to navigate backward
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // exit the activity
            finish();
        }
    }
}