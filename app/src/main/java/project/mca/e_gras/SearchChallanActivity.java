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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;


public class SearchChallanActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";
    SearchView searchView;
    SearchView.SearchAutoComplete autoComplete;
    TextView searchTextView;

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

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "typed: " + newText);
                return true;
            }
        });


        // cutomise the auto complete dropdown
        autoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoComplete.setDropDownBackgroundResource(R.color.colorBackground);
        autoComplete.setDropDownAnchor(R.id.my_searchView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{
                "Bob Marley", "John Doe", "Ram Ravan", "Keshav Stupid", "Kabir Sarmah", "Anup", "Microsoft", "John Snow"
        });

        autoComplete.setAdapter(adapter);

        // to handle suggestion items clicks
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString = (String) adapterView.getItemAtPosition(itemIndex);
                autoComplete.setText(queryString);
            }
        });
    }


    // to get the query after a voice search
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            searchTextView.setText(query);
        }
    }
}