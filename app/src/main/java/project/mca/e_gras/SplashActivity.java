package project.mca.e_gras;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 10;
    public static final String TAG = "MY-APP";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // do I show on-boarding acivity or the main activity?
        // show the on-boarding acivity during installation


        // go to the on-boarding activity
        Intent intent = new Intent(getApplicationContext(), MyOnboardingActivity.class);
        startActivity(intent);
        finish();
    }
}