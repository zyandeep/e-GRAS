package project.mca.e_gras;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import project.mca.e_gras.adapter.SuggestionAdapter;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

import static project.mca.e_gras.MyApplication.BASE_URL;


public class SearchChallanActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";
    private static final String TAG_FETCH_SUGGESTION = "fetch_suggestion";
    private static final String TAG_FETCH_TRANSACTION = "fetch_transaction";
    private static final String TAG_DOWNLOAD_CHALLAN = "download_challan";
    private static final String TAG_VERIFY_PAYMENT = "verify_payment";


    SearchView searchView;
    CardView tranCard;
    TextView searchTextView, dateTextView, grnTextView, officeTextView, amountTextView, mopTextView, statusTextView;
    Button actionButton;

    // all the details of a transaction
    TransactionModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_challan);
        setTitle(R.string.label_serach_challan);

        searchTextView = findViewById(R.id.search_textView);

        searchView = findViewById(R.id.my_searchView);
        searchView.setIconifiedByDefault(false);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // to handle all events inside the searchView, like, onTyping, OnSubmit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // hide the soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                // fetch transaction
                getJWTToken(query.trim(), TAG_FETCH_TRANSACTION);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty() && newText.length() >= 3) {
                    // fetch suggestions
                    getJWTToken(newText.trim(), TAG_FETCH_SUGGESTION);
                }

                return true;
            }
        });

        ///// Transaction details card
        tranCard = findViewById(R.id.tran_card);
        amountTextView = findViewById(R.id.amount_tv);
        dateTextView = findViewById(R.id.date_tv);
        grnTextView = findViewById(R.id.grn_tv);
        mopTextView = findViewById(R.id.mop_tv);
        officeTextView = findViewById(R.id.office_tv);
        statusTextView = findViewById(R.id.status_tv);

        actionButton = findViewById(R.id.action_button_2);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getStatus().equalsIgnoreCase("Y")) {
                    // download challan
                    getJWTToken("", TAG_DOWNLOAD_CHALLAN);
                } else {
                    // verify payment
                    getJWTToken("", TAG_VERIFY_PAYMENT);
                }
            }
        });
    }


    private void getJWTToken(final String query, final String tag) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                switch (tag) {
                                    case TAG_FETCH_SUGGESTION:
                                        fetchSuggestions(query, idToken);
                                        break;

                                    case TAG_FETCH_TRANSACTION:
                                        fetchTransaction(query, idToken);
                                        break;

                                    case TAG_DOWNLOAD_CHALLAN:
                                        MyUtil.downloadChallan(SearchChallanActivity.this,
                                                idToken,
                                                TAG_DOWNLOAD_CHALLAN,
                                                model.getId(),
                                                model.getGrn_no());
                                        break;

                                    case TAG_VERIFY_PAYMENT:
                                        MyUtil.verifyPayment(SearchChallanActivity.this,
                                                idToken,
                                                TAG_VERIFY_PAYMENT,
                                                model.getId());
                                        break;
                                }
                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();

                                if (ex instanceof FirebaseNetworkException) {
                                    MyUtil.showBottomDialog(SearchChallanActivity.this, getString(R.string.label_network_error));
                                }
                            }
                        }
                    });
        }
    }


    private void fetchTransaction(String grn, String idToken) {
        if (!AndroidNetworking.isRequestRunning(TAG_FETCH_TRANSACTION)) {

            MyUtil.showSpotDialog(SearchChallanActivity.this);

            // check for server reachability
            MyUtil.checkServerReachable(SearchChallanActivity.this, TAG_FETCH_TRANSACTION);

            AndroidNetworking.get(BASE_URL + "/transaction")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .addQueryParameter("grn", grn)
                    .setTag(TAG_FETCH_TRANSACTION)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            MyUtil.closeSpotDialog();

                            try {
                                Log.d(TAG, "transaction: " + response.toString(4));

                                if (response.getBoolean("success")) {
                                    model = new Gson()
                                            .fromJson(response.getJSONObject("result").toString(), TransactionModel.class);

                                    // now display the transaction details
                                    displayTransactionDetails();

                                } else {
                                    // no records found
                                    tranCard.setVisibility(View.GONE);
                                    searchTextView.setVisibility(View.VISIBLE);
                                    searchTextView.setText(R.string.label_zero_transaction);
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            MyUtil.displayErrorMessage(SearchChallanActivity.this, anError);
                        }
                    });
        }
    }


    private void displayTransactionDetails() {
        if (model != null) {
            searchTextView.setVisibility(View.GONE);
            tranCard.setVisibility(View.VISIBLE);

            dateTextView.setText(model.getChallan_date());
            grnTextView.setText(model.getGrn_no());
            officeTextView.setText(model.getName());
            amountTextView.setText(MyUtil.formatCurrency(model.getAmount()));
            mopTextView.setText(model.getMop());

            if (model.getStatus().equals("Y") || model.getStatus().equals("P")) {
                actionButton.setVisibility(View.VISIBLE);

                if (model.getStatus().equals("Y")) {
                    actionButton.setText(R.string.label_download_challan);
                    statusTextView.setText(R.string.status_success);
                } else {
                    actionButton.setText(R.string.label_payment_verification);
                    statusTextView.setText(R.string.status_pending);
                }
            } else {
                statusTextView.setText(R.string.status_fail);
                actionButton.setVisibility(View.GONE);
            }
        } else {
            //
        }
    }

    private void fetchSuggestions(String query, String idToken) {
        if (!AndroidNetworking.isRequestRunning(TAG_FETCH_SUGGESTION)) {

            AndroidNetworking.get(BASE_URL + "/suggestions")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .addQueryParameter("query", query)
                    .setTag(TAG_FETCH_SUGGESTION)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success")) {
                                    JSONArray jsonArray = response.getJSONArray("result");

                                    // now convert jsonArray into cursor
                                    Cursor cursor = getCursor(jsonArray);

                                    searchView.setSuggestionsAdapter(new SuggestionAdapter(SearchChallanActivity.this,
                                            cursor,
                                            searchView));

                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d(TAG, "ANError: " + anError.getErrorCode());
                        }
                    });
        }
    }


    private Cursor getCursor(JSONArray jsonArray) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "grn_no"});

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                cursor.newRow()
                        .add("_id", i + 1)
                        .add("grn_no", jsonArray.getString(i));
            }
        } catch (Exception ex) {
        }

        return cursor;
    }


    // to get the query after a voice search
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            searchView.setQuery(query, false);
        }
    }
}