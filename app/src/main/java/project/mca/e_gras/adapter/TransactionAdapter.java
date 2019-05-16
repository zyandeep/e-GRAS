package project.mca.e_gras.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import project.mca.e_gras.R;
import project.mca.e_gras.TransactionDetailsActivity;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "MY-APP";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    List<TransactionModel> modelList;
    Context context;
    LayoutInflater inflater;

    public TransactionAdapter(List<TransactionModel> modelList, Context context) {
        // initialise the list with an empty list(with null)
        this.modelList = modelList;
        this.context = context;
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


    // View Holder to hold non-empty item view
    private class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView grnTextView;
        TextView amountTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.date_details_textView);
            grnTextView = itemView.findViewById(R.id.grn_details_textView);
            amountTextView = itemView.findViewById(R.id.amount_details_textView);

            // Add click listener to GRN Number
            grnTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();        // gets item position

                    if (position != RecyclerView.NO_POSITION) {
                        String grnNo = modelList.get(position).getGrn_no();

                        Toast.makeText(context, "GRN: " + grnNo, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Add click listener to the whole itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();        // gets item position

                    if (position != RecyclerView.NO_POSITION) {
                        TransactionModel model = modelList.get(position);

                        // flashing a model to JSON
                        String json = new Gson().toJson(model);

                        // start Details Activity
                        Intent intent = new Intent(context, TransactionDetailsActivity.class);
                        intent.putExtra("data", json);
                        context.startActivity(intent);
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
