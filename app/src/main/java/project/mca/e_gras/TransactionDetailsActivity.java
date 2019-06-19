package project.mca.e_gras;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import project.mca.e_gras.adapter.SelectedSchemeAdapter;
import project.mca.e_gras.databinding.ActivityTransactionDetailsBinding;
import project.mca.e_gras.model.SchemeModel;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    private static final String TAG_DOWNLOAD_CHALLAN = "download_challan";
    private static final String TAG_VERIFY_PAYMENT = "verify_payment";
    private static final String TAG_SCHEMES = "get_schemes";

    Button actionButton;

    // all the details of a transaction
    TransactionModel model;

    RecyclerView recyclerView;
    SelectedSchemeAdapter adapter;

    // all schemes for a particular office
    private List<SchemeModel> schemeModelList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // receive the bundle
        // The Model
        String json = getIntent().getStringExtra("data");
        model = new Gson().fromJson(json, TransactionModel.class);

        // The schemes
        Type type = new TypeToken<ArrayList<SchemeModel>>() {
        }.getType();

        schemeModelList = new Gson().fromJson(getIntent().getStringExtra("schemes"), type);

        // ANDROID DATA BINDING WITH JETPACK LIBRARY
        ActivityTransactionDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_details);
        binding.setModel(model);

        setTitle(R.string.label_tran_details);

        // initialize the recyclerView
        recyclerView = findViewById(R.id.schemes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SelectedSchemeAdapter(schemeModelList, this);
        recyclerView.setAdapter(adapter);


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
                                                model.getId(),
                                                model.getGrn_no());

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

                                if (ex instanceof FirebaseNetworkException) {
                                    MyUtil.showBottomDialog(TransactionDetailsActivity.this,
                                            getString(R.string.label_network_error));
                                }
                            }
                        }
                    });
        }
    }
}
