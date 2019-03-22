package project.mca.e_gras;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import dmax.dialog.SpotsDialog;

public class MyProfileActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";

    private TextInputLayout displayName;
    private TextInputLayout phone;
    private TextInputLayout email;
    private TextInputLayout password;
    private TextInputLayout confPassword;

    ViewGroup phoneLayout;
    ViewGroup emailLayout;
    ViewGroup passwordLayout;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

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
            }
            else {
                displayName.getEditText().setText(name);
            }

            if (e == null || e.isEmpty()) {
                phone.getEditText().setText(p);
                phoneLayout.setVisibility(View.VISIBLE);
            }
            else {
                email.getEditText().setText(e);
                emailLayout.setVisibility(View.VISIBLE);
                passwordLayout.setVisibility(View.VISIBLE);
            }
        }
        else {
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
                        }
                        else {
                            displayErrorMessage(task.getException().getMessage());
                        }
                    }
                });


    }



    private void displayErrorMessage(String msg) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        // show the bottomSheet dialog
        new BottomDialog.Builder(this)
                .setTitle("Error!")
                .setContent(msg)
                .setCancelable(false)
                .setPositiveText("CLOSE")
                .setPositiveBackgroundColorResource(R.color.colorAccent)
                .setPositiveTextColorResource(android.R.color.background_light)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(BottomDialog dialog) {
                        // dialog closes automatically
                    }
                }).show();
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
                        }
                        else {
                            displayErrorMessage(task.getException().getMessage());
                        }
                    }
                });
    }


    public void updateUserPhone(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String phone = this.phone.getEditText().getText().toString();
        phone += "+91";

           /* user.updatePhoneNumber()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                            }
                            else {
                                Log.d(TAG, task.getException().toString());
                            }
                        }
                    });*/

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
                        }
                        else {
                            displayErrorMessage(task.getException().getMessage());
                        }
                    }
                });
    }
}
