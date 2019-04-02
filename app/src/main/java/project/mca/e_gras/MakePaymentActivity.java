package project.mca.e_gras;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.kofigyan.stateprogressbar.components.StateItem;
import com.kofigyan.stateprogressbar.listeners.OnStateItemClickListener;

import java.util.ArrayList;

import fr.ganfra.materialspinner.MaterialSpinner;


public class MakePaymentActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";

    StateProgressBar stateProgressBar;
    MaterialSpinner deptSpinner, periodSpinner, superSpinner;

    TextSwitcher formDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        stateProgressBar = findViewById(R.id.state_progress_bar);
        stateProgressBar.setOnStateItemClickListener(new OnStateItemClickListener() {
            @Override
            public void onStateItemClick(StateProgressBar stateProgressBar, StateItem stateItem,
                                         int stateNumber, boolean isCurrentState) {

                if (!isCurrentState) {
                    //showFrom(stateProgressBar.getCurrentStateNumber(), stateNumber);
                }
            }
        });

        deptSpinner = findViewById(R.id.dept_spinner);
        // hookup the adapter
        deptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        superSpinner = findViewById(R.id.super_spinner);


        periodSpinner = findViewById(R.id.period_spinner);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String data = parent.getItemAtPosition(position).toString();

                switch (data) {
                    case "Half Yearly":
                        setUpSupperSpinner(getResources().getStringArray(R.array.spinner_data_half_yearly));
                        break;

                    case "Quarterly":
                        setUpSupperSpinner(getResources().getStringArray(R.array.spinner_data_quaterly));
                        break;

                    case "Monthly":
                        setUpSupperSpinner(getResources().getStringArray(R.array.spinner_data_monthly));
                        break;

                    case "Select Period *":
                    case "Annual":
                    case "One Time/Adhoc":
                        // hide the supper spinner
                        superSpinner.setVisibility(View.GONE);
                        break;

                    case "Specific Period":
                        // show date pickers
                        superSpinner.setVisibility(View.GONE);

                        showDatePicker("theme");

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        formDate = findViewById(R.id.from_date_text_switcher);
        formDate.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getApplicationContext());
                t.setGravity(Gravity.TOP);
                t.setTextSize(18);
                t.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                return t;
            }
        });

        // Declare in and out animations
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        // set the animation type to TextSwitcher
        formDate.setInAnimation(in);
        formDate.setOutAnimation(out);
        formDate.setCurrentText("Select From Date *");


        // add the bubble showcase
        new BubbleShowCaseBuilder(this)
                .title("Attention!")
                .description("All fields with '*' are mandatory")
                .backgroundColorResourceId(R.color.colorPrimary)
                .textColorResourceId(R.color.white)
                .imageResourceId(R.drawable.ic_about)
                .targetView(deptSpinner)                                        //View to point out
                .show();                                                        //Display the ShowCase
    }


    private void showDatePicker(String theme) {
    }


    private void setUpSupperSpinner(String[] data) {
        // refresh spinner data

        ArrayAdapter<String> s_adapter = new ArrayAdapter<>(
                getApplicationContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<String>()
        );
        s_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        superSpinner.setAdapter(s_adapter);

        s_adapter.clear();
        s_adapter.addAll(data);
        s_adapter.notifyDataSetChanged();
        superSpinner.setVisibility(View.VISIBLE);
    }


    public void showNextForm(View view) {
        formDate.setText("A new text");
    }
}
