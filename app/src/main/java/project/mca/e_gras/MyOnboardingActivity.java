package project.mca.e_gras;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.Arrays;
import java.util.List;


public class MyOnboardingActivity extends AppIntro {

    private static final int RC_SIGN_IN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //create slides and add them
        addSlide(AppIntro2Fragment.newInstance("Welcome to e-GRAS Mobile",
                "Add some description here...",
                R.drawable.logo,
                Color.parseColor("#9B59B6")));

        addSlide(AppIntro2Fragment.newInstance("Generate e-Challans",
                "Add some description here...",
                R.drawable.invoice,
                Color.DKGRAY));

        addSlide(AppIntro2Fragment.newInstance("Search for Challans",
                "Add some description here...",
                R.drawable.search,
                Color.parseColor("#16A085")));

        addSlide(AppIntro2Fragment.newInstance("Review Transaction History",
                "Add some description here...",
                R.drawable.payment_history,
                Color.parseColor("#CD5C5C")));


        // divider color
        setSeparatorColor(Color.WHITE);


        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);


        // Set silde animations
        setDepthAnimation();
    }


    private void routeToAppropriateScreen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is already signed in
            // verify token at the backend

            signInUser();
        } else {
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


    private void showError(String message) {
        // show the bottomSheet dialogSheet

        new DialogSheet(this)
                .setTitle(R.string.error_label_bottom_dialog)
                .setMessage(message)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close the dialogSheet
                    }
                })
                .setRoundedCorners(true)
                .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackground))
                .show();
    }


    private void signInUser() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.

        // let the user sign-up or log-in
        routeToAppropriateScreen();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.

        // let the user sign-up or log-in
        routeToAppropriateScreen();
    }
}

