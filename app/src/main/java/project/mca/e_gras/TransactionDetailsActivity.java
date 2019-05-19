package project.mca.e_gras;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import project.mca.e_gras.databinding.ActivityTransactionDetailsBinding;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    public static final String BASE_URL = "http://192.168.43.211";
    private static final String TAG_REPEAT_PAYMENT = "repeat_payment";
    private static final String TAG_VERIFY_PAYMENT = "verify_payment";
    private static final String TAG_GET_GRN = "get_grn";

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
                getJWTToken(TAG_VERIFY_PAYMENT);
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
                                        verifyPayment(idToken);
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


    private void verifyPayment(String idToken) {
        if (!AndroidNetworking.isRequestRunning(TAG_VERIFY_PAYMENT)) {

            MyUtil.showSpotDialog(this);

            // check for server reachability
            MyUtil.checkServerReachable(TransactionDetailsActivity.this, TAG_VERIFY_PAYMENT);

            AndroidNetworking.get(BASE_URL + "/verify-payment/{id}")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .addPathParameter("id", String.valueOf(model.getId()))
                    .setTag(TAG_VERIFY_PAYMENT)
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
                                    String data = response.getString("data");          // a json object for get parameters

                                    getGRN(url, data);
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


    private void getGRN(String url, String data) {
//        Intent intent = new Intent(this, PaymentGatewayActivity.class);
//        intent.putExtra("url", url);
//        intent.putExtra("bundle", data);
//        intent.putExtra("type_verify_payment", true);
//
//        startActivity(intent);
//        finish();

        Log.d(TAG, "getGRN: " + url + "\n" + data);

        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();

        Map<String, String> param = new Gson().fromJson(data, type);

        Log.d(TAG, "param: " + param.toString());

        AndroidNetworking.post(url)
                .addBodyParameter(param)
                .setTag(TAG_GET_GRN)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError: " + anError.getErrorCode());
                    }
                });
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
            // received error from server
            String jsonString = anError.getErrorBody();

            try {
                JSONObject obj = new JSONObject(jsonString);
                MyUtil.showBottomDialog(TransactionDetailsActivity.this, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }
}
