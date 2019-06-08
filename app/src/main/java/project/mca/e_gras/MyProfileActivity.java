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
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
        String pwd = password.getEditText().getText().toString().trim();
        String c_pwd = confPassword.getEditText().getText().toString().trim();

        if (!validatePassword(pwd)) {
            password.setErrorEnabled(true);
            password.setError(getString(R.string.password_error));
            return;
        }

        // check if both passwords are the same
        if (!pwd.equals(c_pwd)) {
            confPassword.setErrorEnabled(true);
            confPassword.setError(getString(R.string.error_pwds_not_match));
            return;
        }

        MyUtil.showSpotDialog(MyProfileActivity.this);

        // update password
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updatePassword(pwd)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            MyUtil.closeSpotDialog();

                            password.setErrorEnabled(false);
                            confPassword.setErrorEnabled(false);

                            // clear the passwords
                            password.getEditText().setText("");
                            confPassword.getEditText().setText("");

                            informUser(getString(R.string.label_password_changed));
                        } else {
                            displayErrorMessage(task.getException());
                        }
                    }
                });

    }


    private boolean validatePassword(String password) {
        /* ^\p{Alpha}{6,}$
         * ^\d{6,}$
         *  ^\s{6,}$
         *  ^\p{Punct}{6,}$
         * */

        return password.length() >= 6 &&
                !Pattern.matches("^\\p{Alpha}{6,}$", password) &&
                !Pattern.matches("^\\d{6,}$", password) &&
                !Pattern.matches("^\\s{6,}$", password) &&
                !Pattern.matches("^\\p{Punct}{6,}$", password);
    }


    private void displayErrorMessage(Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            MyUtil.showBottomDialog(this, getString(R.string.label_network_error));
        } else {
            MyUtil.showBottomDialog(this, exception.getMessage());
        }
    }


    private void informUser(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public void updateUserEmail(View view) {
        //^[\w.+-]+@\w+\.\w+$
        String mail = this.email.getEditText().getText().toString().trim();

        if (!Pattern.matches("^[\\w.+-]+@\\w+\\.\\w+$", mail)) {
            email.setErrorEnabled(true);
            email.setError(getString(R.string.error_email));
            return;
        }

        MyUtil.showSpotDialog(this);

        // update email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updateEmail(mail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            MyUtil.closeSpotDialog();
                            email.setErrorEnabled(false);
                            informUser(getString(R.string.label_email_changed));
                        } else {
                            displayErrorMessage(task.getException());
                        }
                    }
                });
    }


    public void updateUserPhone(View view) {
        //^(\+91)?\d{10}$

        String phoneNumber = this.phone.getEditText().getText().toString().trim();

        if (!Pattern.matches("^(\\+91)?\\d{10}$", phoneNumber)) {
            phone.setErrorEnabled(true);
            phone.setError(getString(R.string.error_phone));
            return;
        }

        if (!phoneNumber.startsWith("+91")) {
            phoneNumber = "+91" + phoneNumber;
        }

        MyUtil.showSpotDialog(this);

        // authenticate the new phone number
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                                            phone.setErrorEnabled(false);
                                            informUser(getString(R.string.label_ph_changed));
                                        }
                                        else {
                                            displayErrorMessage(task.getException());
                                        }
                                    }
                                });

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        displayErrorMessage(e);
                    }
                });
    }


    public void updateUserName(View view) {
        //^[a-zA-Z]\w*$
        String name = displayName.getEditText().getText().toString().trim();

        if (!Pattern.matches("^[a-zA-Z][\\w ]*$", name)) {
            displayName.setErrorEnabled(true);
            displayName.setError(getString(R.string.error_user_name));
            return;
        }

        MyUtil.showSpotDialog(this);

        // update display name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                            displayErrorMessage(task.getException());
                        }
                    }
                });
    }
}