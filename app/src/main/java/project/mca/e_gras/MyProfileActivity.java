package project.mca.e_gras;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import project.mca.e_gras.util.MyUtil;

public class MyProfileActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    ViewGroup phoneLayout;
    ViewGroup emailLayout;
    ViewGroup passwordLayout;
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
            confPassword.setErrorEnabled(true);
            confPassword.setError(getString(R.string.password_error));
            return;
        }

        MyUtil.showSpotDialog(MyProfileActivity.this);

        // update password
        user.updatePassword(pwd)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            MyUtil.closeSpotDialog();

                            confPassword.setErrorEnabled(false);

                            informUser(getString(R.string.label_password_changed));
                        } else {
                            //inspect Firebase Auth Exception and alert user accordingly with localised message

                            MyUtil.showBottomDialog(MyProfileActivity.this, task.getException().getMessage());
                        }
                    }
                });

    }


    private void informUser(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public void updateUserEmail(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = this.email.getEditText().getText().toString();

        MyUtil.showSpotDialog(this);

        // update email
        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            MyUtil.closeSpotDialog();
                            informUser(getString(R.string.label_email_changed));
                        } else {
                            MyUtil.showBottomDialog(MyProfileActivity.this, task.getException().getMessage());
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

        MyUtil.showSpotDialog(this);


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
                                            MyUtil.closeSpotDialog();
                                            informUser(getString(R.string.label_ph_changed));
                                        }
                                        else {
                                            // handle FirebaseAuth Exception
                                            MyUtil.showBottomDialog(MyProfileActivity.this, task.getException().getMessage());
                                        }
                                    }
                                });

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // handle FirebaseAuth Exception
                        MyUtil.showBottomDialog(MyProfileActivity.this, e.getMessage());
                    }
                });
    }


    public void updateUserName(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = displayName.getEditText().getText().toString();

        if (name.isEmpty()) {
            displayName.setErrorEnabled(true);
            displayName.setError(getString(R.string.error_user_name));
            return;
        }

        MyUtil.showSpotDialog(this);

        // update display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            MyUtil.closeSpotDialog();
                            displayName.setErrorEnabled(false);
                            informUser(getString(R.string.label_name_changed));
                        } else {
                            // handle FirebaseAuth Exception
                            MyUtil.showBottomDialog(MyProfileActivity.this, task.getException().getMessage());
                        }
                    }
                });
    }
}
