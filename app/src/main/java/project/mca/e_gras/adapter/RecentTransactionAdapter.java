package project.mca.e_gras.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import project.mca.e_gras.R;
import project.mca.e_gras.model.TransactionModel;
import project.mca.e_gras.util.MyUtil;

public class RecentTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "MY-APP";
    List<TransactionModel> modelList;
    Context context;
    LayoutInflater inflater;

    public RecentTransactionAdapter(List<TransactionModel> modelList, Context context) {
        // initialise the list with an empty list
        this.modelList = modelList;
        this.context = context;
        inflater = LayoutInflater.from(this.context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.recent_item_view, parent, false);
        return new RecentTransactionAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TransactionModel model = this.modelList.get(position);

        ((RecentTransactionAdapter.MyViewHolder) holder).dateTextView.setText(model.getChallan_date());
        ((RecentTransactionAdapter.MyViewHolder) holder).grnTextView.setText(model.getGrn_no());
        ((RecentTransactionAdapter.MyViewHolder) holder).amountTextView.setText(MyUtil.formatCurrency(model.getAmount()));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    // Add new items to the list
    public void addNewItems(List<TransactionModel> newList) {
        // remove all the items first
        this.modelList.clear();
        this.modelList.addAll(newList);
        notifyDataSetChanged();
    }


    // View Holder to hold non-empty item view
    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView grnTextView;
        TextView amountTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.date_item_textview);
            grnTextView = itemView.findViewById(R.id.office_item_textView);
            amountTextView = itemView.findViewById(R.id.amount_details_textView);
        }
    }
}
