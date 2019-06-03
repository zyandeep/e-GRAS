package project.mca.e_gras.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;

import project.mca.e_gras.R;

public class SuggestionAdapter extends CursorAdapter {

    private static final String TAG = "MY-APP";
    private Context context;
    private SearchView searchView;

    public SuggestionAdapter(Context context, Cursor cursor, SearchView searchView) {
        super(context, cursor, false);

        this.context = context;
        this.searchView = searchView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_item_view, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String grn = cursor.getString(cursor.getColumnIndex("grn_no"));

        TextView textView = view.findViewById(R.id.grn_item_textView);
        textView.setText(grn);

        // when the view is clicked submit the query

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = v.findViewById(R.id.grn_item_textView);

                searchView.setQuery(tv.getText().toString(), true);

                // also, hide the suggestion dropdown
                searchView.setSuggestionsAdapter(null);
            }
        });
    }
}
