package project.mca.e_gras;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.kofigyan.stateprogressbar.components.StateItem;
import com.kofigyan.stateprogressbar.listeners.OnStateItemClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.ganfra.materialspinner.MaterialSpinner;
import project.mca.e_gras.model.DeptModel;
import project.mca.e_gras.model.PaymentModel;
import project.mca.e_gras.model.SchemeModel;
import project.mca.e_gras.util.MyUtil;


public class MakePaymentActivity extends AppCompatActivity {

    private static final String TAG = "MY-APP";
    private static final String TAG_DEPT_NAMES = "dept_names";
    private static final String TAG_PAYMENT_TYPES = "payment_types";

    StateProgressBar stateProgressBar;
    MaterialSpinner deptSpinner, periodSpinner, superSpinner, paymentSpinner;

    TextView fromDateTextView, toDateTextView;
    TextView headerTextView;
    TextView totalAmountTextView;

    ViewGroup datePickerPanel;

    // all the five form layouts
    ViewGroup departmentDetailsForm;
    ViewGroup schemeDetailsForm;
    ViewGroup payerDetailsForm;
    ViewGroup paymentDetailsForm;
    ViewGroup viewSummaryForm;

    RecyclerView schemeRecyclerView;
    SchemeAdapter schemeAdapter;

    //GSon reference
    Gson gson;

    // Department List
    List<DeptModel> deptList;

    // Payment Type List
    List<PaymentModel> paymentList;

    // This map will contain all input parameters
    // and will get POSTed to backend finally
    private Map<String, Object> parametersMap;

    private BroadcastReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        myReceiver = new MyNetworkReceiver();

        // make sure the app is connected to the internet
        if (!MyUtil.isNetworkAvailable(getApplicationContext())) {
            MyUtil.showBottomDialog(MakePaymentActivity.this, getString(R.string.error_no_network));
        }

        parametersMap = new HashMap<String, Object>();

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();


        totalAmountTextView = findViewById(R.id.total_amount_textView);

        schemeRecyclerView = findViewById(R.id.scheme_recycler_view);
        // specify an adapter
        schemeAdapter = new SchemeAdapter(MakePaymentActivity.this, new ArrayList<SchemeModel>());        // an empty list
        schemeRecyclerView.setAdapter(schemeAdapter);
        // use a linear layout manager
        schemeRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        fromDateTextView = findViewById(R.id.from_date_text_view);
        toDateTextView = findViewById(R.id.to_date_text_view);

        // form header textView
        headerTextView = findViewById(R.id.header_textView);

        // form viewGroups
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
        getDeptNames();
        // hookup the adapter
        deptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // get the 	selected department
                    DeptModel dept = deptList.get(position);

                    // and store it's DEPT_CODE in the Map
                    parametersMap.put("DEPT_CODE", dept.getDeptCode());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        paymentSpinner = findViewById(R.id.payment_spinner);
        getPaymentTypes();
        paymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // get the 	selected payment model
                    PaymentModel model = paymentList.get(position);

                    // and store it's PAYMENT_TYPE in the Map
                    parametersMap.put("PAYMENT_TYPE", model.getType());


                    Log.d(TAG, parametersMap.toString());
                }
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


    private void getPaymentTypes() {
        // check network connectivity first
        if (!MyUtil.isNetworkAvailable(getApplicationContext())) {
            return;
        }

        //  parameters in a query string are always in the form of :
        //  url?<K><V>&<K><V>
        /// where K: String, V: String

        String url = "http://192.168.43.211/my-projects/eGRAS/get-payment-data.php";

        // send query parameters as a Map
        Map<String, String> params = new HashMap<>();
        params.put("paymenttypes", "true");

        AndroidNetworking.get(url)
                .addQueryParameter(params)
                .setTag(TAG_PAYMENT_TYPES)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of payments into ArrayList
                                Type type = new TypeToken<ArrayList<PaymentModel>>() {
                                }.getType();
                                paymentList = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                // now create the list of payment types to display
                                List<String> paymentTypes = new ArrayList<>();

                                for (PaymentModel m : paymentList) {
                                    paymentTypes.add(m.getName());
                                }

                                // finally, set up the payment spinner
                                setUpPaymentSpinner(paymentTypes);
                            } else {
                                // server-side error
                                // display error dialog
                                MyUtil.showBottomDialog(MakePaymentActivity.this, response.getString("msg"));
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // client-side error
                        // display error dialog
                        MyUtil.showBottomDialog(MakePaymentActivity.this, anError.getMessage());
                    }
                });
    }


    private void setUpPaymentSpinner(List<String> paymentTypes) {
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_item, paymentTypes
        );

        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentSpinner.setAdapter(paymentAdapter);
    }


    private void getDeptNames() {
        // check network connectivity first
        if (!MyUtil.isNetworkAvailable(getApplicationContext())) {
            return;
        }

        String url = "http://192.168.43.211/my-projects/eGRAS/get-payment-data.php";

        // send query parameters as a Map
        Map<String, String> params = new HashMap<>();
        params.put("deptnames", "true");

        AndroidNetworking.get(url)
                .addQueryParameter(params)
                .setPriority(Priority.MEDIUM)
                .setTag(TAG_DEPT_NAMES)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of depts into ArrayList<DeptModel>
                                Type deptListType = new TypeToken<ArrayList<DeptModel>>() {
                                }.getType();
                                deptList = gson.fromJson(String.valueOf(response.getJSONArray("result")), deptListType);

                                // now create the list of dept names to display
                                List<String> deptNames = new ArrayList<>();

                                for (DeptModel m : deptList) {
                                    deptNames.add(m.getName());
                                }

                                // finally, set up the department spinner
                                setUpDeptSpinner(deptNames);
                            } else {
                                // Server-side error
                                // display error dialog
                                MyUtil.showBottomDialog(MakePaymentActivity.this, response.getString("msg"));
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // client-side network error
                        MyUtil.showBottomDialog(MakePaymentActivity.this, anError.getMessage());
                    }
                });
    }


    private void setUpDeptSpinner(List<String> deptNames) {
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_item, deptNames
        );

        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptSpinner.setAdapter(deptAdapter);
    }


    private void showFrom(int curState, int nextState) {

        //Log.d(TAG, "" + curState + " : " + nextState);


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
                schemeDetailsForm.setVisibility(View.VISIBLE);

                // get the list of schemes and add it to the adapter
                // if input changes, then only
                List<SchemeModel> dataSource = SchemeModel.getSchemes();

                // recycler view item animation
                final LayoutAnimationController controller =
                        AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation);
                schemeRecyclerView.setLayoutAnimation(controller);
                schemeAdapter.addNewItems(dataSource);
                schemeRecyclerView.scheduleLayoutAnimation();


                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                headerTextView.setText("SchemeModel Details");

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

        showFrom(curState, curState + 1);
    }


    public void submitData(View view) {
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


    ///////////////////////////////////////////////////////////////////////////////////////
    // Receiver to receive CONNECTIVITY_CHANGED broadcast intent
    private class MyNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {

                // if network is available then
                if (MyUtil.isNetworkAvailable(getApplicationContext())) {

                    // dept list needs to be empty
                    if (!AndroidNetworking.isRequestRunning(TAG_DEPT_NAMES) && deptList == null) {
                        // get the dept names
                        getDeptNames();
                    }

                    // payment type list needs to be empty
                    if (!AndroidNetworking.isRequestRunning(TAG_PAYMENT_TYPES) && paymentList == null) {
                        // get the payment types
                        getPaymentTypes();
                    }
                }
            }
        }
    }
}