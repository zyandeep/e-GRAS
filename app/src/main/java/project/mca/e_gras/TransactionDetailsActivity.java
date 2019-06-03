package project.mca.e_gras;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import project.mca.e_gras.databinding.ActivityTransactionDetailsBinding;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    private static final String TAG_DOWNLOAD_CHALLAN = "download_challan";
    private static final String TAG_VERIFY_PAYMENT = "verify_payment";

    Button actionButton;

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

        actionButton = findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getStatus().equalsIgnoreCase("Y")) {
                    // download challan
                    getJWTToken(TAG_DOWNLOAD_CHALLAN);
                } else {
                    // verify payment
                    getJWTToken(TAG_VERIFY_PAYMENT);
                }
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
                                    case TAG_DOWNLOAD_CHALLAN:
                                        MyUtil.downloadChallan(TransactionDetailsActivity.this,
                                                idToken,
                                                TAG_DOWNLOAD_CHALLAN,
                                                model.getId());

                                        break;

                                    case TAG_VERIFY_PAYMENT:
                                        MyUtil.verifyPayment(TransactionDetailsActivity.this,
                                                idToken,
                                                TAG_VERIFY_PAYMENT,
                                                model.getId());
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
}
