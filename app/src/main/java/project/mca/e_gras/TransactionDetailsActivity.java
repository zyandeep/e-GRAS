package project.mca.e_gras;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import project.mca.e_gras.databinding.ActivityTransactionDetailsBinding;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    public static final String BASE_URL = "http://192.168.43.211";
    private static final String TAG_REPEAT_PAYMENT = "repeat_payment";
    private static final String TAG_VERIFY_PAYMENT = "verify_payment";

    Button repeatButton, verificationButton;

    // all the details of a transaction
    TransactionModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ANDROID DATA BINDING WITH JETPACK LIBRARY
        // receive the bundle
        String json = getIntent().getStringExtra("data");
        model = new Gson().fromJson(json, TransactionModel.class);

        ActivityTransactionDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_details);
        binding.setModel(model);

        setTitle(R.string.label_tran_details);

        repeatButton = findViewById(R.id.repeat_button);
        if (model.getStatus().equalsIgnoreCase("F")) {
            repeatButton.setVisibility(View.VISIBLE);
            repeatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getJWTToken(TAG_REPEAT_PAYMENT);
                }
            });
        }

        verificationButton = findViewById(R.id.verification_button);
        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
    }

    private void getJWTToken(final String tag) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                switch (tag) {
                                    case TAG_REPEAT_PAYMENT:
                                        repeatPayment(idToken);
                                        break;

                                    case TAG_VERIFY_PAYMENT:
                                        //getPaymentTypes(idToken);
                                        break;
                                }
                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();
                                MyUtil.showBottomDialog(TransactionDetailsActivity.this, ex.getMessage());
                            }
                        }
                    });
        }
    }


    private void repeatPayment(String idToken) {

        if (!AndroidNetworking.isRequestRunning(TAG_REPEAT_PAYMENT)) {

            MyUtil.showSpotDialog(this);

            // check for server reachability
            MyUtil.checkServerReachable(TransactionDetailsActivity.this, TAG_REPEAT_PAYMENT);

            AndroidNetworking.get(BASE_URL + "/repeat-payment/{id}")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .addPathParameter("id", String.valueOf(model.getId()))
                    .setTag(TAG_REPEAT_PAYMENT)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // if status code is OK:200 then only
                            MyUtil.closeSpotDialog();

                            try {
                                if (response.getBoolean("success")) {
                                    // get the url and data and open it in the webView
                                    String url = response.getString("url");
                                    String postData = response.getString("data");

                                    Intent intent = new Intent(TransactionDetailsActivity.this, PaymentGatewayActivity.class);
                                    intent.putExtra("url", url);
                                    intent.putExtra("bundle", postData);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            displayErrorMessage(anError);
                        }
                    });
        }
    }


    private void displayErrorMessage(ANError anError) {
        MyUtil.closeSpotDialog();

        if (anError.getErrorCode() != 0) {
            String jsonString = anError.getErrorBody();

            try {
                JSONObject obj = new JSONObject(jsonString);
                MyUtil.showBottomDialog(TransactionDetailsActivity.this, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }
}
