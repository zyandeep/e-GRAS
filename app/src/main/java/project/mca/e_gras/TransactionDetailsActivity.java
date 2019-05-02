package project.mca.e_gras;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;

import project.mca.e_gras.databinding.ActivityTransactionDetailsBinding;
import project.mca.e_gras.model.TransactionModel;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";

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
    }

    public void repeatPayment(View view) {

    }

    public void verifyTransaction(View view) {

    }
}
