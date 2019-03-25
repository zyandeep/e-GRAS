package project.mca.e_gras;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MY-APP";

    private TextView displayName;
    private TextView emailOrPhone;

    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to ensure that default setting values are set properly
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // reading the setting values from the default shared pref
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String langPrefValue = sharedPref.getString(getString(R.string.lang_pref_key), getString(R.string.lang_pref_default_value));
        String themePrefValue = sharedPref.getString(getString(R.string.theme_pref_key), getString(R.string.theme_pref_default_value));

        // now apply the settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


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
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.make_payment) {
            Intent intent = new Intent(getApplicationContext(), MakePaymentActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.transaction_history) {

        }
        else if (id == R.id.search_challan) {

        }
        else if (id == R.id.settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.logout) {
            signOutUser();
        }
        else if (id == R.id.about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
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
}