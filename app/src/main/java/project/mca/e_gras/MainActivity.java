package project.mca.e_gras;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.firebase.ui.auth.AuthUI;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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

import project.mca.e_gras.adapter.RecentTransactionAdapter;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MY-APP";
    public static final String TAG_RECENT_TRANS = "recent_trans";
    public static final String BASE_URL = "http://192.168.43.211";
    private static final int REQUEST_CODE = 12;
    RecyclerView recyclerView;
    RecentTransactionAdapter adapter;
    ViewGroup emptyLayout;
    ViewGroup transLayout;
    //GSon reference
    Gson gson;
    private TextView displayName;
    private TextView emailOrPhone;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to ensure that default setting values are set properly
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // set the theme and language before setting the contentView

        // reading the setting values from the default shared pref
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String themePrefValue = sharedPref.getString(getString(R.string.theme_pref_key), getString(R.string.theme_pref_default_value));
        String langValue = sharedPref.getString(getString(R.string.lang_pref_key), getString(R.string.lang_pref_default_value));

        applyTheme(themePrefValue);
        setLanguage(langValue);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MakePaymentActivity.class);
                startActivity(intent);
            }
        });

        // Every time the app opens, show FAB TapTarget View
        TapTargetView.showFor(this,
                TapTarget.forView(fab, getString(R.string.fab_tap_target_title), getString(R.string.fab_tap_target_desc))
                        .textColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .textTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.open_sans))
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .icon(getDrawable(R.drawable.ic_payment))       // Specify a custom drawable to draw as the target
        );


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // get the textViews inside navigationDrawer header
        View headerView = navigationView.getHeaderView(0);
        displayName = headerView.findViewById(R.id.username_textView);
        emailOrPhone = headerView.findViewById(R.id.user_email_phone_textView);


        emptyLayout = findViewById(R.id.empty_linearLayout);
        transLayout = findViewById(R.id.tran_linearLayout);

        // initialize the recyclerView
        recyclerView = findViewById(R.id.recent_tran_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // create an empty transaction model list
        List<TransactionModel> modelList = new ArrayList<>();
        adapter = new RecentTransactionAdapter(modelList, this);
        recyclerView.setAdapter(adapter);

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }


    private void setLanguage(String langValue) {
        MyUtil.changeLocal(this, langValue);
    }


    private void applyTheme(String themePrefValue) {
        switch (themePrefValue) {
            case "lt":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "dk":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Get the currently signed-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String phone = user.getPhoneNumber();

            if (name == null || name.isEmpty()) {
                displayName.setText(getString(R.string.display_name));
            } else {
                displayName.setText(name);
            }

            if (email == null || email.isEmpty()) {
                emailOrPhone.setText(phone);
            } else {
                emailOrPhone.setText(email);
            }
        } else {
            // No user is signed in
            finish();
        }


        // get 5 recent transactions
        getJWTToken();
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.make_payment) {
            Intent intent = new Intent(getApplicationContext(), MakePaymentActivity.class);
            startActivity(intent);
        } else if (id == R.id.transaction_history) {
            Intent intent = new Intent(getApplicationContext(), TransactionListActivity.class);
            startActivity(intent);
        } else if (id == R.id.search_challan) {

        } else if (id == R.id.settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            //startActivity(intent);

            startActivityForResult(intent, REQUEST_CODE);
        } else if (id == R.id.logout) {
            signOutUser();
        } else if (id == R.id.about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public void goToEditProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
        startActivity(intent);

        // close the drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    public void showOnBoarding(View view) {
        Intent intent = new Intent(this, MyOnboardingActivity.class);
        intent.putExtra("from_main", true);
        startActivity(intent);
    }

    public void loadAllTransaction(View view) {
        Intent intent = new Intent(getApplicationContext(), TransactionListActivity.class);
        startActivity(intent);
    }


    private void getJWTToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                getRecentTransaction(idToken);
                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();
                                MyUtil.showBottomDialog(MainActivity.this, ex.getMessage());
                            }
                        }
                    });
        }
    }


    private void getRecentTransaction(String idToken) {

        if (!AndroidNetworking.isRequestRunning(TAG_RECENT_TRANS)) {

            AndroidNetworking.get(BASE_URL + "/recent-transactions")
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setTag(TAG_RECENT_TRANS)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // if status code is OK:200 then only

                            try {
                                if (response.getBoolean("success")) {

                                    // converting jsonArray of payments into ArrayList
                                    Type type = new TypeToken<ArrayList<TransactionModel>>() {
                                    }.getType();

                                    List<TransactionModel> list = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                    if (list.isEmpty()) {
                                        // show the empty view
                                        emptyLayout.setVisibility(View.VISIBLE);
                                        transLayout.setVisibility(View.GONE);
                                    } else {
                                        emptyLayout.setVisibility(View.GONE);
                                        transLayout.setVisibility(View.VISIBLE);
                                        adapter.addNewItems(list);
                                    }
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            //displayErrorMessage(anError);

                            Log.d(TAG, anError.getMessage());
                        }
                    });
        }
    }
}