package project.mca.e_gras;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TextView;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.kofigyan.stateprogressbar.components.StateItem;
import com.kofigyan.stateprogressbar.listeners.OnStateItemClickListener;


import java.util.ArrayList;
import java.util.Calendar;

import fr.ganfra.materialspinner.MaterialSpinner;


public class MakePaymentActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";

    StateProgressBar stateProgressBar;
    MaterialSpinner deptSpinner, periodSpinner, superSpinner;

    TextView fromDateTextView, toDateTextView;
    TextView headerTextView;

    ViewGroup datePickerPanel;

    // all the five form layouts
    ViewGroup departmentDetailsForm;
    ViewGroup schemeDetailsForm;
    ViewGroup payerDetailsForm;
    ViewGroup paymentDetailsForm;
    ViewGroup viewSummaryForm;

    RecyclerView schemeRecyclerView;
    SchemeAdapter schemeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        schemeRecyclerView = findViewById(R.id.scheme_recycler_view);

        // specify an adapter
        schemeAdapter = new SchemeAdapter(getApplicationContext(), new ArrayList<Scheme>());        // an empty list
        schemeRecyclerView.setAdapter(schemeAdapter);

        // use a linear layout manager
        schemeRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        fromDateTextView = findViewById(R.id.from_date_text_view);
        toDateTextView = findViewById(R.id.to_date_text_view);

        // form header textView
        headerTextView = findViewById(R.id.header_textView);

        departmentDetailsForm = findViewById(R.id.dept_card_view);
        schemeDetailsForm = findViewById(R.id.scheme_card_view);
        payerDetailsForm = findViewById(R.id.payer_card_view);
        paymentDetailsForm = findViewById(R.id.payment_card_view);
        viewSummaryForm = findViewById(R.id.form_summary_card_view);


        stateProgressBar = findViewById(R.id.state_progress_bar);
        stateProgressBar.setOnStateItemClickListener(new OnStateItemClickListener() {
            @Override
            public void onStateItemClick(StateProgressBar stateProgressBar, StateItem stateItem,
                                         int stateNumber, boolean isCurrentState) {

                if (!isCurrentState) {
                    showFrom(stateProgressBar.getCurrentStateNumber(), stateNumber);
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

        datePickerPanel = findViewById(R.id.date_viewGroup);

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

                        // hide date picker panel
                        datePickerPanel.setVisibility(View.GONE);

                        break;

                    case "Specific Period":
                        superSpinner.setVisibility(View.GONE);

                        // show date pickers

                        // clear the dates first
                        fromDateTextView.setText("Select From Date *");
                        toDateTextView.setText("Select To Date *");

                        datePickerPanel.setVisibility(View.VISIBLE);

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        // add the bubble showcase
        new BubbleShowCaseBuilder(this)
                .title("Attention!")
                .description("Fields marked with '*' are mandatory")
                .backgroundColorResourceId(R.color.colorPrimary)
                .textColorResourceId(R.color.white)
                .imageResourceId(R.drawable.ic_about)
                .targetView(deptSpinner)                                        //View to point out
                .show();                                                        //Display the ShowCase
    }


    private void showFrom(int curState, int nextState) {

        Log.d(TAG, "" + curState + " : " + nextState);


        // hide the form associates with "curState"
        switch (curState) {
            case 1:
                // validation required
                departmentDetailsForm.setVisibility(View.GONE);
                break;

            case 2:
                schemeDetailsForm.setVisibility(View.GONE);
                break;

            case 3:
                // validation required
                payerDetailsForm.setVisibility(View.GONE);
                break;

            case 4:
                // validation required
                paymentDetailsForm.setVisibility(View.GONE);
                break;

            case 5:
                viewSummaryForm.setVisibility(View.GONE);
                break;
        }

        // show the from associates with "nextState"
        // Change the heading
        // Change the current state number
        switch (nextState) {
            case 1:
                departmentDetailsForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                headerTextView.setText("Department Details");
                break;

            case 2:

                // get the list of schemes and add it to the adapter
                // if input changes, then only
                schemeAdapter.addNewItems(Scheme.getSchemes());

                schemeDetailsForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                headerTextView.setText("Account and Scheme Details");

                break;

            case 3:
                payerDetailsForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                headerTextView.setText("Payer Details");
                break;

            case 4:
                paymentDetailsForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                headerTextView.setText("Payment Details");
                break;

            case 5:
                viewSummaryForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FIVE);
                headerTextView.setText("View Summary");
                break;
        }
    }


    public void showDatePicker(View view) {
        // get today's date
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        switch (view.getId()) {
            case R.id.from_date_img_button:

                // show "from date" dialog
               new DatePickerDialog(
                        MakePaymentActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month += 1;

                                fromDateTextView.setText(dayOfMonth + "/" + month + "/" + year);
                            }
                        },
                        year, month, day
                ).show();

                break;

            case R.id.to_date_img_button:

                // show "to date" dialog
                new DatePickerDialog(
                        MakePaymentActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month += 1;

                                toDateTextView.setText(dayOfMonth + "/" + month + "/" + year);
                            }
                        },
                        year, month, day
                ).show();

                break;
        }
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

        // hide date picker
        datePickerPanel.setVisibility(View.GONE);
    }


    public void showNextForm(View view) {
        // get the current state number
        int curState = stateProgressBar.getCurrentStateNumber();

        showFrom(curState, curState+1);
    }



    public void submitData(View view) {
    }
}
