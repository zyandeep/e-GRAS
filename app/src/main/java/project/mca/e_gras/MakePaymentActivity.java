// load spinner bundle like
// dept -> payment -> district -> office

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
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
import java.util.Locale;
import java.util.Map;

import fr.ganfra.materialspinner.MaterialSpinner;
import project.mca.e_gras.adapter.DeptSpinnerAdapter;
import project.mca.e_gras.adapter.DistrictSpinnerAdapter;
import project.mca.e_gras.adapter.OfficeSpinnerAdapter;
import project.mca.e_gras.adapter.PaymentSpinnerAdapter;
import project.mca.e_gras.adapter.SchemeAdapter;
import project.mca.e_gras.model.DeptModel;
import project.mca.e_gras.model.DistrictModel;
import project.mca.e_gras.model.OfficeModel;
import project.mca.e_gras.model.PaymentModel;
import project.mca.e_gras.model.SchemeModel;
import project.mca.e_gras.util.MyUtil;


public class MakePaymentActivity extends AppCompatActivity {

    public static final String TAG = "MY-APP";
    public static final String TAG_DEPT_NAMES = "dept_names";
    public static final String TAG_PAYMENT_TYPES = "payment_types";
    public static final String TAG_OFFICE_NAMES = "office_names";
    public static final String TAG_DISTRICT_NAMES = "district_names";
    public static final String TAG_SCHEME_NAMES = "scheme_names";
    public static final String TAG_GENERATE_CHALLAN = "generate_challan";

    public static final String BASE_URL = "http://192.168.43.211";
    public TextView totalAmountTextView;            // so that it's visible in the package adapter

    SuperSpinnerMode spinnerMode;

    StateProgressBar stateProgressBar;
    MaterialSpinner deptSpinner, periodSpinner, superSpinner, paymentSpinner, districtSpinner, officeSpinner, yearSpinner;

    TextInputLayout deptTextID, payerName, panNo, blockNo, locality, area, pinNo, mobileNo, remarks;

    TextView fromDateTextView, toDateTextView;
    TextView headerTextView;
    ViewGroup datePickerPanel;
    // all the five form layouts
    ViewGroup departmentDetailsForm;
    ViewGroup schemeDetailsForm;
    ViewGroup payerDetailsForm;
    //ViewGroup paymentDetailsForm;
    ViewGroup viewSummaryForm;
    ViewGroup emptyState;
    ViewGroup schemeListData;
    RecyclerView schemeRecyclerView;
    SchemeAdapter schemeAdapter;
    //GSon reference
    Gson gson;
    // Empty Department List
    List<DeptModel> deptModelList = new ArrayList<>();
    // Empty Payment Type List
    List<PaymentModel> paymentModelList = new ArrayList<>();
    // Empty District List
    List<DistrictModel> districtModelList = new ArrayList<>();
    // Empty Office List
    List<OfficeModel> officeModelList = new ArrayList<>();
    // Empty Scheme List
    List<SchemeModel> schemeModelList = new ArrayList<>();
    // SwipToRefresh layout
    SwipeRefreshLayout refreshLayout;
    // This map will contain all input parameters
    // and will get POSTed to PHP backend finally
    private Map<String, Object> parametersMap;

    private BroadcastReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        setTitle(R.string.make_payment_activity);


        myReceiver = new MyNetworkReceiver();

        parametersMap = new HashMap<>();

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        refreshLayout = findViewById(R.id.swip_to_refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // check if the network request is not running
                // and dept list ti empty
                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    if (!AndroidNetworking.isRequestRunning(TAG_DEPT_NAMES)) {
                        getJWTToken(TAG_DEPT_NAMES);
                    } else {
                        // refresh completed
                        // stop the animation
                        refreshLayout.setRefreshing(false);
                    }
                } else {
                    // No network
                    // stop the animation
                    refreshLayout.setRefreshing(false);
                }
            }
        });


        deptTextID = findViewById(R.id.dept_text_id_text_input);
        payerName = findViewById(R.id.name_text_input);
        panNo = findViewById(R.id.pan_text_input);
        blockNo = findViewById(R.id.block_text_input);
        locality = findViewById(R.id.locality_text_input);
        area = findViewById(R.id.area_text_input);
        pinNo = findViewById(R.id.pin_text_input);
        mobileNo = findViewById(R.id.mobile_text_input);
        remarks = findViewById(R.id.remarks_text_input);

        totalAmountTextView = findViewById(R.id.total_amount_textView);

        // Set up the recycler view
        schemeRecyclerView = findViewById(R.id.scheme_recycler_view);
        // specify an adapter
        schemeAdapter = new SchemeAdapter(MakePaymentActivity.this, new ArrayList<SchemeModel>());    // an empty list
        schemeRecyclerView.setAdapter(schemeAdapter);
        // use a linear layout manager
        schemeRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        schemeListData = findViewById(R.id.scheme_list);
        emptyState = findViewById(R.id.empty_scheme);


        fromDateTextView = findViewById(R.id.from_date_text_view);
        toDateTextView = findViewById(R.id.to_date_text_view);

        // form header textView
        headerTextView = findViewById(R.id.header_textView);

        // form viewGroups
        departmentDetailsForm = findViewById(R.id.dept_card_view);
        schemeDetailsForm = findViewById(R.id.scheme_card_view);
        payerDetailsForm = findViewById(R.id.payer_card_view);
        //paymentDetailsForm = findViewById(R.id.payment_card_view);
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

        // need to talk to backend to get the dept names. So, for that, grab the jwt token first
        getJWTToken(TAG_DEPT_NAMES);

        // hookup the adapter
        deptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // get the 	selected department
                    DeptModel dept = deptModelList.get(position);

                    // and store it's DEPT_CODE in the Map
                    parametersMap.put("DEPT_CODE", dept.getDeptCode());

                    // load the respective payment types
                    getJWTToken(TAG_PAYMENT_TYPES);
                } else {
                    // remove the key
                    parametersMap.remove("DEPT_CODE");

                    // to refresh a spinner, create its adapter with an empty list

                    setUpPaymentSpinner(true);
                    setUpDistrictSpinner(true);
                    setUpOfficeSpinner(true);
                    clearSchemeList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        paymentSpinner = findViewById(R.id.payment_spinner);
        paymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // get the 	selected payment model
                    PaymentModel model = paymentModelList.get(position);

                    // and store it's PAYMENT_TYPE in the Map
                    parametersMap.put("PAYMENT_TYPE", model.getType());

                    // load the districts corresponding to the department/DEPT_CODE
                    getJWTToken(TAG_DISTRICT_NAMES);
                } else {
                    //remove the key
                    parametersMap.remove("PAYMENT_TYPE");

                    setUpDistrictSpinner(true);
                    setUpOfficeSpinner(true);
                    clearSchemeList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        districtSpinner = findViewById(R.id.district_spinner);
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    DistrictModel model = districtModelList.get(position);
                    parametersMap.put("DISTRICT_CODE", model.getDistrictCode());

                    getJWTToken(TAG_OFFICE_NAMES);

                } else {
                    parametersMap.remove("DISTRICT_CODE");
                    setUpOfficeSpinner(true);
                    clearSchemeList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        officeSpinner = findViewById(R.id.office_spinner);
        officeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    OfficeModel model = officeModelList.get(position);

                    if (!model.getOfficeCode().equals(parametersMap.get("OFFICE_CODE"))) {
                        parametersMap.put("OFFICE_CODE", model.getOfficeCode());
                        parametersMap.put("SRO_CODE", model.getSroCode());

                        // load the schemes
                        getJWTToken(TAG_SCHEME_NAMES);
                    }
                } else {
                    parametersMap.remove("OFFICE_CODE");
                    parametersMap.remove("SRO_CODE");

                    // remove the scheme list too
                    clearSchemeList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        yearSpinner = findViewById(R.id.year_spinner);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    String year = (String) parent.getItemAtPosition(position);

                    parametersMap.put("REC_FIN_YEAR", year);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        superSpinner = findViewById(R.id.super_spinner);
        superSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    String period = "";

                    switch (spinnerMode) {
                        case MODE_HALF:
                            if (position == 0) {
                                period = "H1";
                            } else if (position == 1) {
                                period = "H2";
                            }
                            break;

                        case MODE_MONTH:
                            period = "M";
                            break;

                        case MODE_QUATER:
                            switch (position) {
                                case 0:
                                    period = "Q1";
                                    break;

                                case 1:
                                    period = "Q2";
                                    break;

                                case 2:
                                    period = "Q3";
                                    break;

                                case 3:
                                    period = "Q4";
                                    break;
                            }

                            break;
                    }

                    parametersMap.put("PERIOD", period);
                    parametersMap.put("FROM_DATE", "");         // ?
                    parametersMap.put("TO_DATE", "");           // ?
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        datePickerPanel = findViewById(R.id.date_viewGroup);

        periodSpinner = findViewById(R.id.period_spinner);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String data = parent.getItemAtPosition(position).toString();

                if (data.equals(getString(R.string.half))) {
                    spinnerMode = SuperSpinnerMode.MODE_HALF;
                    setUpSupperSpinner(getResources().getStringArray(R.array.spinner_data_half_yearly));
                } else if (data.equals(getString(R.string.quater))) {
                    spinnerMode = SuperSpinnerMode.MODE_QUATER;
                    setUpSupperSpinner(getResources().getStringArray(R.array.spinner_data_quaterly));
                } else if (data.equals(getString(R.string.month))) {
                    spinnerMode = SuperSpinnerMode.MODE_MONTH;
                    setUpSupperSpinner(getResources().getStringArray(R.array.months));
                } else if (data.equals(getString(R.string.annual))) {
                    superSpinner.setVisibility(View.GONE);
                    datePickerPanel.setVisibility(View.GONE);

                    parametersMap.put("PERIOD", "A");
                    parametersMap.put("FROM_DATE", "01/04/2019");
                    parametersMap.put("TO_DATE", "31/03/2020");
                } else if (data.equals(getString(R.string.one_time))) {
                    superSpinner.setVisibility(View.GONE);
                    datePickerPanel.setVisibility(View.GONE);

                    parametersMap.put("PERIOD", "O");
                    parametersMap.put("FROM_DATE", "01/04/2019");
                    parametersMap.put("TO_DATE", "31/03/2099");
                } else if (data.equals(getString(R.string.specific))) {
                    // show date pickers
                    // clear the dates first
                    superSpinner.setVisibility(View.GONE);
                    datePickerPanel.setVisibility(View.VISIBLE);

                    fromDateTextView.setText(getString(R.string.label_select_from_date));
                    toDateTextView.setText(getString(R.string.label_select_to_date));

                    parametersMap.put("PERIOD", "S");
                } else {
                    // "Select Period *" is chosen
                    superSpinner.setVisibility(View.GONE);
                    datePickerPanel.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void getJWTToken(final String tag) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                Log.d(TAG, idToken);

                                switch (tag) {
                                    case TAG_DEPT_NAMES:
                                        getDeptNames(idToken);
                                        break;

                                    case TAG_PAYMENT_TYPES:
                                        getPaymentTypes(idToken);
                                        break;

                                    case TAG_DISTRICT_NAMES:
                                        getDistricts(idToken);
                                        break;

                                    case TAG_OFFICE_NAMES:
                                        getOfficeNames(idToken);
                                        break;

                                    case TAG_SCHEME_NAMES:
                                        getSchemes(idToken);
                                        break;

                                    case TAG_GENERATE_CHALLAN:
                                        submitData(idToken);
                                        break;
                                }
                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();
                                MyUtil.showBottomDialog(MakePaymentActivity.this, ex.getMessage());
                            }
                        }
                    });
        }
    }

    private void clearSchemeList() {
        if (schemeModelList != null) {
            schemeModelList.clear();
        }

        schemeAdapter.removeAllItems();
        parametersMap.remove("CHALLAN_AMOUNT");
        parametersMap.remove("HOA");
    }

    private void getSchemes(String idToken) {

        MyUtil.showSpotDialog(this);                    // making network connection here...

        // check for server reachability
        MyUtil.checkServerReachable(MakePaymentActivity.this, TAG_SCHEME_NAMES);

        // send query parameters as a Map
        Map<String, String> params = new HashMap<>();
        params.put("office_code", String.valueOf(parametersMap.get("OFFICE_CODE")));

        AndroidNetworking.get(BASE_URL + "/offices/{office_code}/schemes")
                .addPathParameter(params)
                .addHeaders("Authorization", "Bearer " + idToken)
                .setPriority(Priority.MEDIUM)
                .setTag(TAG_SCHEME_NAMES)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MyUtil.closeSpotDialog();

                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of into ArrayList
                                Type type = new TypeToken<ArrayList<SchemeModel>>() {
                                }.getType();
                                schemeModelList = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                // add the scheme names into the scheme adapter
                                schemeAdapter.addNewItems(schemeModelList);
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // Networking error
                        displayErrorMessage(anError);
                    }
                });
    }

    private void getOfficeNames(String idToken) {

        MyUtil.showSpotDialog(this);

        // check for server reachability
        MyUtil.checkServerReachable(MakePaymentActivity.this, TAG_OFFICE_NAMES);

        // send query parameters as a Map
        Map<String, String> params = new HashMap<>();
        params.put("dept_code", String.valueOf(parametersMap.get("DEPT_CODE")));
        params.put("district_code", String.valueOf(parametersMap.get("DISTRICT_CODE")));

        AndroidNetworking.get(BASE_URL + "/departments/{dept_code}/districts/{district_code}/offices")
                .addPathParameter(params)
                .addHeaders("Authorization", "Bearer " + idToken)
                .setPriority(Priority.MEDIUM)
                .setTag(TAG_OFFICE_NAMES)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MyUtil.closeSpotDialog();

                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of into ArrayList
                                Type type = new TypeToken<ArrayList<OfficeModel>>() {
                                }.getType();
                                officeModelList = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                // finally, set up the spinner
                                setUpOfficeSpinner(false);
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // Networking Error
                        displayErrorMessage(anError);
                    }
                });
    }

    private void setUpOfficeSpinner(boolean reset) {
        if (reset) {
            parametersMap.remove("OFFICE_CODE");
            parametersMap.remove("SRO_CODE");

            officeModelList.clear();
        }

        OfficeSpinnerAdapter adapter = new OfficeSpinnerAdapter(this, officeModelList);
        officeSpinner.setAdapter(adapter);
    }

    private void getDistricts(String idToken) {

        MyUtil.showSpotDialog(this);

        // check for server reach-ability
        MyUtil.checkServerReachable(MakePaymentActivity.this, TAG_DISTRICT_NAMES);

        AndroidNetworking.get(BASE_URL + "/departments/{dept_code}/districts")
                .addPathParameter("dept_code", String.valueOf(parametersMap.get("DEPT_CODE")))
                .addHeaders("Authorization", "Bearer " + idToken)
                .setPriority(Priority.MEDIUM)
                .setTag(TAG_DISTRICT_NAMES)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        MyUtil.closeSpotDialog();

                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of into ArrayList
                                Type type = new TypeToken<ArrayList<DistrictModel>>() {
                                }.getType();
                                districtModelList = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                // finally, set up the spinner
                                setUpDistrictSpinner(false);
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayErrorMessage(anError);
                    }
                });
    }

    private void setUpDistrictSpinner(boolean reset) {

        if (reset) {
            parametersMap.remove("DISTRICT_CODE");
            districtModelList.clear();
        }

        DistrictSpinnerAdapter adapter = new DistrictSpinnerAdapter(this, districtModelList);
        districtSpinner.setAdapter(adapter);
    }

    private void getPaymentTypes(String idToken) {

        MyUtil.showSpotDialog(this);

        // check for server reachability
        MyUtil.checkServerReachable(MakePaymentActivity.this, TAG_PAYMENT_TYPES);

        //  parameters in a query string are always in the form of :
        //  url?<K><V>&<K><V>
        /// where K: String, V: String

        AndroidNetworking.get(BASE_URL + "/paymenttypes")
                .addHeaders("Authorization", "Bearer " + idToken)
                .setTag(TAG_PAYMENT_TYPES)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // if status code is OK:200 then only

                        MyUtil.closeSpotDialog();

                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of payments into ArrayList
                                Type type = new TypeToken<ArrayList<PaymentModel>>() {
                                }.getType();
                                paymentModelList = gson.fromJson(String.valueOf(response.getJSONArray("result")), type);

                                // finally, set up the payment spinner
                                setUpPaymentSpinner(false);
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // Networking error
                        displayErrorMessage(anError);
                    }
                });
    }

    private void displayErrorMessage(ANError anError) {
        MyUtil.closeSpotDialog();

        if (anError.getErrorCode() != 0) {
            String jsonString = anError.getErrorBody();

            try {
                JSONObject obj = new JSONObject(jsonString);
                MyUtil.showBottomDialog(MakePaymentActivity.this, obj.getString("msg"));
            } catch (Exception ex) {
            }
        }
    }

    private void setUpPaymentSpinner(boolean reset) {
        // if reset is TRUE then clear the spinner
        if (reset) {
            parametersMap.remove("PAYMENT_TYPE");
            paymentModelList.clear();
        }

        PaymentSpinnerAdapter adapter = new PaymentSpinnerAdapter(this, paymentModelList);
        paymentSpinner.setAdapter(adapter);
    }

    private void getDeptNames(String idToken) {
        // if list is refreshing then
        // refresh completed. Stop the animation
        refreshLayout.setRefreshing(false);

        MyUtil.showSpotDialog(this);

        // check for server reachability
        MyUtil.checkServerReachable(MakePaymentActivity.this, TAG_DEPT_NAMES);


        AndroidNetworking.get(BASE_URL + "/departments")
                .addHeaders("Authorization", "Bearer " + idToken)
                .setPriority(Priority.MEDIUM)
                .setTag(TAG_DEPT_NAMES)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // if response code 200:OK, then only
                        MyUtil.closeSpotDialog();

                        try {
                            if (response.getBoolean("success")) {
                                // if success is true
                                // get the dept list

                                // converting jsonArray of depts into ArrayList<DeptModel>
                                Type deptListType = new TypeToken<ArrayList<DeptModel>>() {
                                }.getType();
                                deptModelList = gson.fromJson(String.valueOf(response.getJSONArray("result")), deptListType);

                                // finally, set up the department spinner
                                setUpDeptSpinner();
                            }
                        } catch (JSONException e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        displayErrorMessage(anError);
                    }
                });
    }

    private void setUpDeptSpinner() {
        if (deptModelList != null) {
            DeptSpinnerAdapter adapter = new DeptSpinnerAdapter(this, deptModelList);
            deptSpinner.setAdapter(adapter);
        }

        // Now, highlight the * mark
        // add the bubble showcase
        new BubbleShowCaseBuilder(this)
                .title(getString(R.string.label_attention))
                .description(getString(R.string.label_field_mandatory))
                .targetView(deptSpinner)
                .backgroundColorResourceId(R.color.colorPrimary)
                .textColorResourceId(R.color.white)
                .imageResourceId(R.drawable.ic_warn)
                .show();
    }

    private void showFrom(int curState, int nextState) {

        // hide the form associates with "curState"
        switch (curState) {
            case 1:
                // validation required
                departmentDetailsForm.setVisibility(View.GONE);

                break;

            case 2:
                schemeDetailsForm.setVisibility(View.GONE);

                // if the user clicks "NEXT", then only
                // store scheme details
                if (schemeModelList != null && !schemeModelList.isEmpty()) {
                    parametersMap.put("HOA", schemeModelList);
                    parametersMap.put("CHALLAN_AMOUNT", schemeAdapter.getTotalAmount());
                }

                break;

            case 3:
                // validation required
                payerDetailsForm.setVisibility(View.GONE);

                // if the user clicks "NEXT", OR when the user wants to go forward, then only
                // validate the inputs and
                // save the values
                savePayerDetails();

                break;

            case 4:
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
                headerTextView.setText(getString(R.string.label_department_details));
                break;

            case 2:
                schemeDetailsForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                headerTextView.setText(getString(R.string.label_scheme_details));

                // show the empty state or the recycler view
                if (schemeAdapter.getItemCount() == 0) {
                    // show the empty state
                    schemeListData.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    // show the scheme list
                    schemeListData.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);

                    // recycler view item animation
                    final LayoutAnimationController controller =
                            AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation);
                    schemeRecyclerView.setLayoutAnimation(controller);
                    schemeRecyclerView.scheduleLayoutAnimation();
                }

                break;

            case 3:
                payerDetailsForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                headerTextView.setText(getString(R.string.label_payer_details));
                break;

            case 4:
                viewSummaryForm.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                headerTextView.setText(getString(R.string.label_verify_data));
                break;
        }
    }

    private void savePayerDetails() {
        ///Validation required

        String text_id = deptTextID.getEditText().getText().toString();
        String party_name = payerName.getEditText().getText().toString();
        String pan_no = panNo.getEditText().getText().toString();
        String address1 = blockNo.getEditText().getText().toString();
        String address2 = locality.getEditText().getText().toString();
        String address3 = area.getEditText().getText().toString();
        String pin_no = pinNo.getEditText().getText().toString();
        String mobile_no = mobileNo.getEditText().getText().toString();
        String remarks = this.remarks.getEditText().getText().toString();

        parametersMap.put("TAX_ID", text_id);
        parametersMap.put("PARTY_NAME", party_name);
        parametersMap.put("PAN_NO", pan_no);
        parametersMap.put("ADDRESS1", address1);
        parametersMap.put("ADDRESS2", address2);
        parametersMap.put("ADDRESS3", address3);
        parametersMap.put("PIN_NO", pin_no);
        parametersMap.put("MOBILE_NO", mobile_no);
        parametersMap.put("REMARKS", remarks);
    }


    public void showDatePicker(View view) {
        // get today's date
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        switch (view.getId()) {
            case R.id.from_date_img_button:

                // show "from date" dialogSheet
                new DatePickerDialog(
                        MakePaymentActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month += 1;

                                String f_date = String.format(new Locale("en", "IN"), "%d/%d/%d",
                                        dayOfMonth, month, year);

                                fromDateTextView.setText(f_date);
                                parametersMap.put("FROM_DATE", f_date);
                            }
                        },
                        year, month, day
                ).show();

                break;

            case R.id.to_date_img_button:

                // show "to date" dialogSheet
                new DatePickerDialog(
                        MakePaymentActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month += 1;

                                String t_date = String.format(new Locale("en", "IN"), "%d/%d/%d",
                                        dayOfMonth, month, year);

                                toDateTextView.setText(t_date);
                                parametersMap.put("TO_DATE", t_date);
                            }
                        },
                        year, month, day
                ).show();

                break;
        }
    }

    private void setUpSupperSpinner(String[] data) {
        // refresh spinner bundle

        ArrayAdapter<String> s_adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                data
        );
        s_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        superSpinner.setAdapter(s_adapter);

        superSpinner.setVisibility(View.VISIBLE);
        // hide date picker
        datePickerPanel.setVisibility(View.GONE);
    }

    public void showNextForm(View view) {
        // get the current state number
        int curState = stateProgressBar.getCurrentStateNumber();

        showFrom(curState, curState + 1);
    }

    public void submitData(String idToken) {
        // after validating all the bundle

        MyUtil.showSpotDialog(this);

        // check for server reachability
        MyUtil.checkServerReachable(MakePaymentActivity.this, TAG_GENERATE_CHALLAN);

        // convert the bundle to be posted as a json
        String json = gson.toJson(parametersMap);
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(json);

            Log.d(TAG, "submitData: " + jsonObject.toString(4));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.post(BASE_URL + "/submit-payment")
                .setTag(TAG_GENERATE_CHALLAN)
                .setPriority(Priority.MEDIUM)
                .addJSONObjectBody(jsonObject)
                .addHeaders("Authorization", "Bearer " + idToken)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MyUtil.closeSpotDialog();

                        try {
                            if (response.getBoolean("success")) {

                                // get the url and bundle and open it in the webView
                                String url = response.getString("url");
                                String postData = response.getString("data");

                                Intent intent = new Intent(MakePaymentActivity.this, PaymentGatewayActivity.class);
                                intent.putExtra("url", url);
                                intent.putExtra("bundle", postData);
                                startActivity(intent);
                                finish();

                            } else {
                                MyUtil.showBottomDialog(MakePaymentActivity.this, response.getString("msg"));
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, ex.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        displayErrorMessage(error);
                    }
                });
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

    // submit form data to backend
    public void doSubmit(View view) {
        getJWTToken(TAG_GENERATE_CHALLAN);
    }

    // to tell the mode of the super spinner
    private enum SuperSpinnerMode {
        MODE_HALF,
        MODE_MONTH,
        MODE_QUATER
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Receiver to receive CONNECTIVITY_CHANGED broadcast intent
    private class MyNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {

                // if network is available then
                if (MyUtil.isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(MakePaymentActivity.this, getString(R.string.message_online), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}