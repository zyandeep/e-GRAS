package project.mca.e_gras;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
import project.mca.e_gras.util.MyUtil;

public class MyProfileActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    ViewGroup phoneLayout;
    ViewGroup emailLayout;
    ViewGroup passwordLayout;
    AlertDialog dialog;
    private TextInputLayout displayName;
    private TextInputLayout phone;
    private TextInputLayout email;
    private TextInputLayout password;
    private TextInputLayout confPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        setTitle(R.string.my_profile_activity);

        displayName = findViewById(R.id.display_name_text_input_layout);
        phone = findViewById(R.id.phone_text_input_layout);
        email = findViewById(R.id.email_text_input_layout);
        password = findViewById(R.id.password_text_input_layout);
        confPassword = findViewById(R.id.c_password_text_input_layout);

        phoneLayout = findViewById(R.id.phone_form);
        emailLayout = findViewById(R.id.email_form);
        passwordLayout = findViewById(R.id.password_form);

        // set up the initial UI
        updateUI();

        // spot dialog
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setTheme(R.style.mySpotDialogTheme)
                .build();
    }


    // set up the initial ui and update it when required
    private void updateUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            String name = user.getDisplayName();
            String e = user.getEmail();
            String p = user.getPhoneNumber();

            if (name == null || name.isEmpty()) {
                displayName.getEditText().setText(getString(R.string.display_name));
            } else {
                displayName.getEditText().setText(name);
            }

            if (e == null || e.isEmpty()) {
                phone.getEditText().setText(p);
                phoneLayout.setVisibility(View.VISIBLE);
            } else {
                email.getEditText().setText(e);
                emailLayout.setVisibility(View.VISIBLE);
                passwordLayout.setVisibility(View.VISIBLE);
            }
        } else {
            // No user is signed in
            finish();
        }
    }


    public void updatePassword(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String pwd = password.getEditText().getText().toString().trim();
        String c_pwd = confPassword.getEditText().getText().toString().trim();

        if (pwd.isEmpty() || c_pwd.isEmpty() || !pwd.equals(c_pwd)) {
            displayErrorMessage("Enter a valid password");
            return;
        }

        // show the dialog
        dialog.show();


        // update password
        user.updatePassword(pwd)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //updateUI();
                            dialog.dismiss();
                        } else {
                            displayErrorMessage(task.getException().getMessage());
                        }
                    }
                });


    }


    private void displayErrorMessage(String msg) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        MyUtil.showBottomDialog(this, msg);
    }


    public void updateUserEmail(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = this.email.getEditText().getText().toString();

        // show the dialog
        dialog.show();

        // update email
        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //updateUI();
                            dialog.dismiss();
                        } else {
                            displayErrorMessage(task.getException().getMessage());
                        }
                    }
                });
    }


    public void updateUserPhone(View view) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String phoneNumber = this.phone.getEditText().getText().toString().trim();
        if (!phoneNumber.startsWith("+91")) {
            phoneNumber = "+91" + phoneNumber;
        }

        // show the dialog
        dialog.show();


        // authenticate the new phone number
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                MyProfileActivity.this,               // Activity (for callback binding)

                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {       // OnVerificationStateChangedCallbacks
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        Log.d(TAG, "auto verification completed");

                        // now update the phone number
                        user.updatePhoneNumber(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                        }
                                        else {
                                            displayErrorMessage(task.getException().getMessage());
                                        }
                                    }
                                });

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        displayErrorMessage(e.getMessage());
                    }
                });
    }


    public void updateUserName(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = displayName.getEditText().getText().toString();

        // show the dialog
        dialog.show();

        // update display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //updateUI();
                            dialog.dismiss();
                        } else {
                            displayErrorMessage(task.getException().getMessage());
                        }
                    }
                });
    }
}
