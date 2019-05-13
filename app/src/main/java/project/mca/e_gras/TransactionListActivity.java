package project.mca.e_gras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.marcoscg.dialogsheet.DialogSheet;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.mca.e_gras.adapter.TransactionAdapter;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionListActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    public static final String BASE_URL = "http://192.168.43.211";
    private static final String TAG_TRANSACTION_LIST = "transaction_list";
    private static final String TAG_LOAD_MORE = "load_more";
    private static final int ITEMS_PER_PAGE = 10;

    // The layouts
    SwipeRefreshLayout refreshLayout;
    CardView emptyState;
    ShimmerFrameLayout shimmerFrameLayout;          // facebook's shimmer layout


    RecyclerView recyclerView;
    TransactionAdapter adapter;

    Gson gson;
    DialogSheet dialogSheet;
    private BroadcastReceiver myReceiver;
    // Number pickers
    NumberPicker yearPicker, monthPicker1, monthPicker2;
    // to load bundle page-wise
    private int pageNo = 0, year = 0, month1 = 0, month2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        setTitle(R.string.label_tran_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // instantiate the broadcast receiver
        myReceiver = new MyNetworkReceiver();

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);

        refreshLayout = findViewById(R.id.transaction_list_container);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);         // Configure the refreshing colors
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    if (!AndroidNetworking.isRequestRunning(TAG_TRANSACTION_LIST)) {
                        // clear filters and re-initialise the search
                        pageNo = year = month1 = month2 = 0;
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


        // the initial load of transaction list
        getJWTToken(TAG_TRANSACTION_LIST);

        recyclerView = findViewById(R.id.tran_list_recycler_view);

        // initialise the adapter with an empty list
        adapter = new TransactionAdapter(new ArrayList<TransactionModel>(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

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

        // Instantiate filter dialogSheet
        // first, inflate the view
        View view = View.inflate(this, R.layout.filter_dialog_box, null);

        Button doneButton = view.findViewById(R.id.filter_serach_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogSheet != null) {
                    // re-initialise the filtered search
                    pageNo = 0;
                    year = yearPicker.getValue();
                    month1 = monthPicker1.getValue();
                    month2 = monthPicker2.getValue();

                    // clear recyclerView adapter data set
                    adapter.clearItems();

                    // get filtered transaction list
                    getJWTToken(TAG_LOAD_MORE);

                    startShimmer();

                    dialogSheet.dismiss();
                }
            }
        });
        Button dismissButton = view.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogSheet != null) {
                    dialogSheet.dismiss();
                }
            }
        });

        dialogSheet = new DialogSheet(this)
                .setView(view)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setRoundedCorners(false)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));

        // NUMBER PICKERS
        yearPicker = view.findViewById(R.id.year_picker);
        yearPicker.setValue(Calendar.getInstance().get(Calendar.YEAR));

        String[] months = getResources().getStringArray(R.array.months);

        monthPicker1 = view.findViewById(R.id.month_picker_1);
        monthPicker1.setMinValue(1);
        monthPicker1.setMaxValue(months.length);
        monthPicker1.setDisplayedValues(months);
        monthPicker1.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1);

        monthPicker2 = view.findViewById(R.id.month_picker_2);
        monthPicker2.setMinValue(1);
        monthPicker2.setMaxValue(months.length);
        monthPicker2.setDisplayedValues(months);
        monthPicker2.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_menu_item:

                // display the search filter dialog
                if (dialogSheet != null) {
                    dialogSheet.show();
                }

                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    private void loadMore(String idToken) {
        if (!AndroidNetworking.isRequestRunning(TAG_LOAD_MORE)) {

            // check for server reach-ability
            MyUtil.checkServerReachable(TransactionListActivity.this, TAG_LOAD_MORE);

            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(pageNo));
            params.put("year", String.valueOf(year));
            params.put("month1", String.valueOf(month1));
            params.put("month2", String.valueOf(month2));

            AndroidNetworking.get(BASE_URL + "/transactions")
                    .addQueryParameter(params)
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setPriority(Priority.MEDIUM)
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
                                    Type type = new TypeToken<ArrayList<TransactionModel>>() {
                                    }.getType();
                                    List<TransactionModel> dataSet = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

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
                                        Toast.makeText(TransactionListActivity.this, getString(R.string.label_no_transaction),
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
                                closeShimmer();

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
            startShimmer();

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
                            closeShimmer();

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
                MyUtil.showBottomDialog(TransactionListActivity.this, obj.getString("msg"));
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


    // Reload data again.. (From an empty view)
    public void reloadData(View view) {
        // clear filters and re-initialise the search

        pageNo = year = month1 = month2 = 0;
        getJWTToken(TAG_TRANSACTION_LIST);
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