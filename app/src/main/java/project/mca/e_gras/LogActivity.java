package project.mca.e_gras;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LogActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    private static final String TAG_LOAD_MORE = "load_more";
    private static final int ITEMS_PER_PAGE = 5;
    private static final String TAG_ACTIVITY_LOG = "activity_log";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
    }
}
