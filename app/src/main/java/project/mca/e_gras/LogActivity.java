package project.mca.e_gras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
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

import project.mca.e_gras.adapter.LogAdapter;
import project.mca.e_gras.model.LogModel;
import project.mca.e_gras.util.MyUtil;

import static project.mca.e_gras.MyApplication.BASE_URL;

public class LogActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    private static final String TAG_LOAD_MORE = "load_more";
    private static final int ITEMS_PER_PAGE = 5;
    private static final String TAG_ACTIVITY_LOG = "activity_log";

    // The layouts
    SwipeRefreshLayout refreshLayout;
    ViewGroup emptyState;
    ShimmerFrameLayout shimmerFrameLayout;          // facebook's shimmer layout

    RecyclerView recyclerView;
    LogAdapter adapter;

    Gson gson;
    private BroadcastReceiver myReceiver;

    // to load data page-wise
    private int pageNo = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        setTitle(R.string.label_activity_log);

        // instantiate the broadcast myReceiver
        myReceiver = new LogActivity.MyNetworkReceiver();

        // register the myReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myReceiver, filter);


        shimmerFrameLayout = findViewById(R.id.shimmer_container);

        refreshLayout = findViewById(R.id.transaction_container);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);         // Configure the refreshing colors
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    if (!AndroidNetworking.isRequestRunning(TAG_ACTIVITY_LOG)) {
                        // start loading data form the beginning
                        pageNo = 0;
                        getJWTToken(TAG_ACTIVITY_LOG);
                    } else {
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

        emptyState = findViewById(R.id.empty_state_view);

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();


        // get the 1st load of logs
        getJWTToken(TAG_ACTIVITY_LOG);

        recyclerView = findViewById(R.id.tran_recycler_view);

        // initialise the adapter with an empty list
        adapter = new LogAdapter(new ArrayList<LogModel>(), this, recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


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
                        // at the end of the recycler view, can't scroll down any further
                        // show the loading progress dialog

                        // load new items
                        getJWTToken(TAG_LOAD_MORE);
                    }
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
                                    case TAG_ACTIVITY_LOG:
                                        getLogs(idToken);
                                        break;

                                    case TAG_LOAD_MORE:
                                        loadMore(idToken);
                                        break;
                                }

                            } else {
                                closeShimmer();

                                // Handle error -> task.getException();
                                Exception ex = task.getException();

                                if (ex instanceof FirebaseNetworkException) {
                                    MyUtil.showBottomDialog(LogActivity.this, getString(R.string.label_network_error));
                                }
                            }
                        }
                    });
        }
    }


    private void getLogs(String idToken) {
        refreshLayout.setRefreshing(false);

        if (!AndroidNetworking.isRequestRunning(TAG_ACTIVITY_LOG)) {
            startShimmer();

            // check for server reachability
            MyUtil.checkServerReachable(LogActivity.this, TAG_ACTIVITY_LOG);

            AndroidNetworking.get(BASE_URL + "/logs")
                    .addQueryParameter("page", String.valueOf(pageNo))
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setPriority(Priority.MEDIUM)
                    .setTag(TAG_ACTIVITY_LOG)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            closeShimmer();

                            try {
                                if (response.getBoolean("success")) {
                                    // if success is true
                                    // get the logs

                                    // converting jsonArray of into ArrayList
                                    Type type = new TypeToken<ArrayList<LogModel>>() {
                                    }.getType();
                                    List<LogModel> dataSet = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                    // if date set is empty then, show the empty state
                                    // else show the recyclerView
                                    if (dataSet.isEmpty() && adapter.getItemCount() == 0) {
                                        refreshLayout.setVisibility(View.GONE);
                                        emptyState.setVisibility(View.VISIBLE);
                                    } else {
                                        // clear the data source first
                                        adapter.clearItems();

                                        if (dataSet.size() >= ITEMS_PER_PAGE) {
                                            dataSet.add(null);
                                        }

                                        adapter.addNewItems(dataSet);
                                        pageNo++;
                                        refreshLayout.setVisibility(View.VISIBLE);
                                        emptyState.setVisibility(View.GONE);
                                    }
                                } else {
                                    Log.d(TAG, response.toString());
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


    private void loadMore(String idToken) {
        if (!AndroidNetworking.isRequestRunning(TAG_LOAD_MORE)) {

            // check for server reach-ability
            MyUtil.checkServerReachable(LogActivity.this, TAG_LOAD_MORE);

            AndroidNetworking.get(BASE_URL + "/logs")
                    .addQueryParameter("page", String.valueOf(pageNo))
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setPriority(Priority.HIGH)
                    .setTag(TAG_LOAD_MORE)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            closeShimmer();

                            try {
                                if (response.getBoolean("success")) {
                                    // if success is true
                                    // get the dept list

                                    // converting jsonArray of into ArrayList
                                    Type type = new TypeToken<ArrayList<LogModel>>() {
                                    }.getType();
                                    List<LogModel> dataSet = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                    if (!dataSet.isEmpty()) {
                                        // in case, list is empty, then
                                        refreshLayout.setVisibility(View.VISIBLE);
                                        emptyState.setVisibility(View.GONE);

                                        if (dataSet.size() >= ITEMS_PER_PAGE) {
                                            dataSet.add(null);
                                        }
                                        adapter.addNewItems(dataSet);
                                        pageNo++;
                                    } else if (adapter.getItemCount() == 0) {
                                        // no data set in the adapter
                                        refreshLayout.setVisibility(View.GONE);
                                        emptyState.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(LogActivity.this, getString(R.string.label_no_log),
                                                Toast.LENGTH_LONG).show();

                                        adapter.removeNull();
                                    }
                                } else {
                                    Log.d(TAG, response.toString());
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
        closeShimmer();

        if (adapter.getItemCount() == 0) {
            // no items in data source
            emptyState.setVisibility(View.VISIBLE);
        }

        if (anError.getErrorCode() != 0) {
            String jsonString = anError.getErrorBody();

            try {
                JSONObject obj = new JSONObject(jsonString);
                MyUtil.showBottomDialog(LogActivity.this, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }


    private void startShimmer() {
        refreshLayout.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
    }

    private void closeShimmer() {
        if (shimmerFrameLayout.isShimmerStarted()) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myReceiver);
    }


    // Reload data again.. (From an empty view)
    public void reloadData(View view) {
        // load a fresh set of logs
        pageNo = 0;
        getJWTToken(TAG_ACTIVITY_LOG);
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    // Receiver to receive CONNECTIVITY_CHANGED broadcast intent
    private class MyNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {

                // if network is available then
                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(LogActivity.this, getString(R.string.message_online), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LogActivity.this, getString(R.string.message_offline), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
