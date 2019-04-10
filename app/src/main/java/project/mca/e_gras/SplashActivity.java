package project.mca.e_gras;

import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    public static final String TAG = "MY-APP";
    public static final String KEY_APP_INTO = "app-intro";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // do I show on-boarding activity or the main activity?
        // show the on-boarding activity during installation


        Intent intent = new Intent(getApplicationContext(), MyOnboardingActivity.class);
        startActivity(intent);
        finish();


       /* SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean appIntroShown = sharedPref.getBoolean(KEY_APP_INTO, false);

        if (!appIntroShown) {
            // show the app intro for once
            // go to the on-boarding activity
            // make the field as true
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(KEY_APP_INTO, true);
            editor.apply();

            Intent intent = new Intent(getApplicationContext(), MyOnboardingActivity.class);
            startActivity(intent);
            finish();

        } else {
            // route to either log-in or mainActivity
            routeToAppropriateScreen();
        }*/
    }


    private void routeToAppropriateScreen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is already signed in
            // verify token at the backend

            signInUser();
        }
        else {
            // No user is signed in
            // Show the Firebase Auth UI

            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setTheme(R.style.AppTheme)
                            .setLogo(R.drawable.image)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                // verify token at the backend

                signInUser();
            } else {
                if (response != null) {
                    showError(response.getError().getMessage());
                }
            }
        }
    }


    private void signInUser() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        // show the bottomSheet dialog

        new DialogSheet(this)
                .setTitle(R.string.error_label_bottom_dialog)
                .setMessage(message)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close the dialog
                    }
                })
                .setRoundedCorners(true)
                .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackground))
                .show();
    }
}