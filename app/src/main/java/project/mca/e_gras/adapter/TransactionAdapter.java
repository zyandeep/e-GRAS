package project.mca.e_gras.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import project.mca.e_gras.R;
import project.mca.e_gras.TransactionDetailsActivity;
import project.mca.e_gras.model.SchemeModel;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

import static project.mca.e_gras.MyApplication.BASE_URL;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "MY-APP";
    private static final String TAG_SCHEMES = "get_schemes";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    List<TransactionModel> modelList;
    Context context;
    RecyclerView recyclerView;
    LayoutInflater inflater;


    public TransactionAdapter(List<TransactionModel> modelList, Context context, RecyclerView recyclerView) {
        // initialise the list with an empty list(with null)
        this.modelList = modelList;
        this.context = context;
        this.recyclerView = recyclerView;
        inflater = LayoutInflater.from(this.context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = this.inflater.inflate(R.layout.transaction_item_view, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = this.inflater.inflate(R.layout.loading_item_view, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            TransactionModel model = this.modelList.get(position);

            ((ItemViewHolder) holder).dateTextView.setText(model.getChallan_date());
            ((ItemViewHolder) holder).grnTextView.setText(model.getGrn_no());
            ((ItemViewHolder) holder).amountTextView.setText(MyUtil.formatCurrency(model.getAmount()));
        } else if (holder instanceof LoadingViewHolder) {
            // Nothing to do here...
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (this.modelList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return this.modelList.size();
    }


    // Add new items to the list
    public void addNewItems(List<TransactionModel> newList) {
        // remove NULL from the list and add it to the end

        this.modelList.remove(null);
        this.modelList.addAll(newList);
        notifyDataSetChanged();
    }

    public void removeNull() {
        if (this.modelList.contains(null)) {
            this.modelList.remove(null);
            notifyItemRemoved(getItemCount() - 1);          // from position
        }
    }

    public void clearItems() {
        this.modelList.clear();
        notifyDataSetChanged();
    }

    private void getJWTToken(final TransactionModel model) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                getSchemes(idToken, model);
                            } else {
                                // Handle error -> task.getException();
                                Exception ex = task.getException();

                                if (ex instanceof FirebaseNetworkException) {
                                    MyUtil.showBottomDialog(context,
                                            context.getString(R.string.label_network_error));
                                }
                            }
                        }
                    });
        }
    }

    private void getSchemes(String idToken, final TransactionModel model) {

        if (!AndroidNetworking.isRequestRunning(TAG_SCHEMES)) {
            // show spot dialog
            MyUtil.showSpotDialog(context);

            // check for server reachability
            MyUtil.checkServerReachable(context, TAG_SCHEMES);

            AndroidNetworking.get(BASE_URL + "/offices/{office_code}/schemes")
                    .addPathParameter("office_code", model.getOffice_code())
                    .addHeaders("Authorization", "Bearer " + idToken)
                    .setPriority(Priority.HIGH)
                    .setTag(TAG_SCHEMES)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.getBoolean("success")) {
                                    // if success is true

                                    // converting jsonArray of into ArrayList
                                    Type type = new TypeToken<ArrayList<SchemeModel>>() {
                                    }.getType();

                                    List<SchemeModel> list = new Gson().fromJson(String.valueOf(response.getJSONArray("result")), type);

                                    // now filter only those HOAs the office has paid tax for
                                    JSONObject obj = new JSONObject(model.getReq_params());
                                    String hoa;
                                    int amount;

                                    for (int i = 1; i <= 9; i++) {
                                        if (obj.has("HOA" + i)) {
                                            hoa = obj.getString("HOA" + i);
                                            amount = obj.getInt("AMOUNT" + i);

                                            // now look for the hoa in the list
                                            for (SchemeModel m : list) {
                                                if (m.getHoa().equals(hoa)) {
                                                    m.setAmount(amount);
                                                }
                                            }
                                        }
                                    }

                                    /// remove models with amount 0
                                    for (Iterator<SchemeModel> iterator = list.iterator(); iterator.hasNext(); ) {
                                        if (iterator.next().getAmount() == 0) {
                                            iterator.remove();
                                        }
                                    }

                                    // flashing a model to JSON
                                    String jsonModel = new Gson().toJson(model);
                                    String jsonScheme = new Gson().toJson(list);

                                    MyUtil.closeSpotDialog();

                                    // Now, go to transaction details screen
                                    // start Details Activity
                                    Intent intent = new Intent(context, TransactionDetailsActivity.class);
                                    intent.putExtra("data", jsonModel);
                                    intent.putExtra("schemes", jsonScheme);
                                    context.startActivity(intent);
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            // Networking error
                            MyUtil.displayErrorMessage(context, anError);
                        }
                    });
        }
    }

    // View Holder to hold non-empty item view
    private class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView grnTextView;
        TextView amountTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.date_item_textview);
            grnTextView = itemView.findViewById(R.id.grn_item_textView);
            amountTextView = itemView.findViewById(R.id.amount_details_textView);

            // Add click listener to the whole itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();        // get the item position

                    if (position != RecyclerView.NO_POSITION) {
                        TransactionModel model = modelList.get(position);

                        // get the schemes for that particular office
                        getJWTToken(model);
                    }
                }
            });
        }
    }


    // View Holder to hold empty item view
    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar_itemView);
        }
    }
}
