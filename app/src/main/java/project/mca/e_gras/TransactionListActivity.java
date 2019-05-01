package project.mca.e_gras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import project.mca.e_gras.adapter.TransactionAdapter;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionListActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    public static final String BASE_URL = "http://192.168.43.211";
    private static final String TAG_TRANSACTION_LIST = "transaction_list";
    private static final String TAG_LOAD_MORE = "load_more";
    // The main two layouts
    SwipeRefreshLayout refreshLayout;
    CardView emptyState;
    RecyclerView recyclerView;
    TransactionAdapter adapter;
    FloatingActionButton fab;

    //GSon reference
    Gson gson;
    // to load bundle page-wise
    private int pageNo = 0;
    private BroadcastReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        // instantiate the broadcast receiver
        myReceiver = new MyNetworkReceiver();

        refreshLayout = findViewById(R.id.transaction_list_container);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);         // Configure the refreshing colors
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    if (!AndroidNetworking.isRequestRunning(TAG_TRANSACTION_LIST)) {
                        // load page = 0 of the transaction list, again
                        pageNo = 0;
                        getJWTToken(TAG_TRANSACTION_LIST);
                    } else {
                        // refresh completed
                        // stop the animation
                        refreshLayout.setRefreshing(false);
                    }
                } else {
                    // No network
                    // stop the animation
                    refreshLayout.setRefreshing(false);
                }
            }
        });

        emptyState = findViewById(R.id.empty_state_cardView);

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        getJWTToken(TAG_TRANSACTION_LIST);

        recyclerView = findViewById(R.id.tran_list_recycler_view);

        // initialise the adapter with an empty list
        adapter = new TransactionAdapter(new ArrayList<TransactionModel>(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        // add scroll listener to recyclerView
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);     // starts scrolling
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);         // scrolling ends

                if (dy > 0) {
                    // Recycle view scrolling down...

                    if (!recyclerView.canScrollVertically(View.FOCUS_DOWN)) {
                        Log.d(TAG, "at the end");

                        // load new items
                        getJWTToken(TAG_LOAD_MORE);
                    }
                }
            }
        });

        fab = findViewById(R.id.reload_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load the first page
                if (pageNo == 0) {
                    getJWTToken(TAG_TRANSACTION_LIST);
                }
            }
        });
    }


    private void loadMore(String idToken) {
        // if bundle not already loading
        if (!AndroidNetworking.isRequestRunning(TAG_LOAD_MORE)) {

            Log.d(TAG, "loading bundle...");

            // check for server reachability
            MyUtil.checkServerReachable(TransactionListActivity.this, TAG_LOAD_MORE);

            AndroidNetworking.get(BASE_URL + "/transactions")
                    .addQueryParameter("page", String.valueOf(pageNo))
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setPriority(Priority.MEDIUM)
                    .setTag(TAG_LOAD_MORE)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            MyUtil.closeSpotDialog();

                            try {
                                if (response.getBoolean("success")) {
                                    // if success is true
                                    // get the dept list

                                    // converting jsonArray of into ArrayList
                                    Type type = new TypeToken<ArrayList<TransactionModel>>() {
                                    }.getType();
                                    List<TransactionModel> dataSet = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                    if (!dataSet.isEmpty()) {
                                        adapter.addNewItems(dataSet);
                                        pageNo++;
                                    } else {
                                        // hide the loading spinner
                                        //recyclerView.smoothScrollToPosition(adapter.getItemCount() - 5);

                                        Toast.makeText(TransactionListActivity.this, "No new transactions found",
                                                Toast.LENGTH_LONG).show();
                                    }
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


    private void getJWTToken(final String tag) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                switch (tag) {
                                    case TAG_TRANSACTION_LIST:
                                        getTransactions(idToken);
                                        break;

                                    case TAG_LOAD_MORE:
                                        loadMore(idToken);
                                        break;
                                }

                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();
                                MyUtil.showBottomDialog(TransactionListActivity.this, ex.getMessage());
                            }
                        }
                    });
        }
    }


    private void getTransactions(String idToken) {
        refreshLayout.setRefreshing(false);

        if (!AndroidNetworking.isRequestRunning(TAG_TRANSACTION_LIST)) {
            MyUtil.showSpotDialog(this);                    // making network connection here...

            // check for server reachability
            MyUtil.checkServerReachable(TransactionListActivity.this, TAG_TRANSACTION_LIST);

            AndroidNetworking.get(BASE_URL + "/transactions")
                    .addQueryParameter("page", String.valueOf(pageNo))
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setPriority(Priority.MEDIUM)
                    .setTag(TAG_TRANSACTION_LIST)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            MyUtil.closeSpotDialog();

                            try {
                                if (response.getBoolean("success")) {
                                    // if success is true
                                    // get the dept list

                                    // converting jsonArray of into ArrayList
                                    Type type = new TypeToken<ArrayList<TransactionModel>>() {
                                    }.getType();
                                    List<TransactionModel> dataSet = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                    // if bundle set is empty then, show the empty state
                                    // else show the recyclerView
                                    if (dataSet.isEmpty() && adapter.getItemCount() == 0) {
                                        refreshLayout.setVisibility(View.GONE);
                                        emptyState.setVisibility(View.VISIBLE);
                                    } else {
                                        if (pageNo == 0) {
                                            // clear the bundle source first
                                            adapter.clearItems();
                                        }

                                        adapter.addNewItems(dataSet);
                                        pageNo++;
                                        refreshLayout.setVisibility(View.VISIBLE);
                                        emptyState.setVisibility(View.GONE);
                                    }
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
                MyUtil.showBottomDialog(TransactionListActivity.this, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // register the receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        // register the receiver dynamically
        this.registerReceiver(myReceiver, filter);
    }


    @Override
    protected void onStop() {
        super.onStop();

        this.unregisterReceiver(myReceiver);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Receiver to receive CONNECTIVITY_CHANGED broadcast intent
    private class MyNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {

                // if network is available then
                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(TransactionListActivity.this, getString(R.string.message_online), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}