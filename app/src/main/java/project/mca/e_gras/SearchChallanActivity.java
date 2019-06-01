package project.mca.e_gras;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;


public class SearchChallanActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";
    private static final String NETWORK_REQUEST = "network_request";
    SearchView searchView;
    SearchView.SearchAutoComplete autoComplete;
    CardView tranCard;
    TextView searchTextView, dateTextView, grnTextView, officeTextView, amountTextView, mopTextView, statusTextView;
    Button actionButton;


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
                Log.d(TAG, "submit: " + query);
                // hide the soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                // make network request search the challan

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 5) {
                    Log.d(TAG, "typed: " + newText);
                }

                return true;
            }
        });


        // cutomise the auto complete dropdown
        autoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoComplete.setDropDownBackgroundResource(R.color.colorBackground);
        autoComplete.setDropDownAnchor(R.id.my_searchView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{
                "1ddpn", "1ddpn344", "1ddpnassd", "1ddpn45", "1ddpn23", "1ddpnvbnm", "1ddpnyh56", "1ddpnbgg56"
        });

        autoComplete.setAdapter(adapter);

        // to handle suggestion items clicks
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString = (String) adapterView.getItemAtPosition(itemIndex);
                autoComplete.setText(queryString);
                // make network request search the challan
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
    }


    // to get the query after a voice search
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            autoComplete.setText(query);
        }
    }
}