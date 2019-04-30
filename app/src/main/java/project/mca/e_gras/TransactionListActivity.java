package project.mca.e_gras;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class TransactionListActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";

    // The main two layouts
    SwipeRefreshLayout refreshLayout;
    CardView emptyState;

    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        refreshLayout = findViewById(R.id.transaction_list_container);
        emptyState = findViewById(R.id.empty_state_cardView);

        recyclerView = findViewById(R.id.tran_list_recycler_view);
    }
}
