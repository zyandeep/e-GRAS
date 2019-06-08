package project.mca.e_gras.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import project.mca.e_gras.R;
import project.mca.e_gras.model.LogModel;


public class LogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "MY-APP";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    List<LogModel> modelList;
    Context context;
    LayoutInflater inflater;
    RecyclerView recyclerView;


    public LogAdapter(List<LogModel> modelList, Context context, RecyclerView recyclerView) {
        this.modelList = modelList;
        this.context = context;
        this.recyclerView = recyclerView;
        inflater = LayoutInflater.from(this.context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = this.inflater.inflate(R.layout.log_item_view, parent, false);
            return new LogAdapter.ItemViewHolder(view);
        } else {
            View view = this.inflater.inflate(R.layout.loading_item_view, parent, false);
            return new LogAdapter.LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LogAdapter.ItemViewHolder) {
            LogModel model = this.modelList.get(position);

            ((ItemViewHolder) holder).activityTextView.setText(model.getActivity());
            ((LogAdapter.ItemViewHolder) holder).dateTextView.setText(model.getDateTime());
            ((LogAdapter.ItemViewHolder) holder).grnTextView.setText(model.getGrnNo());
            ((ItemViewHolder) holder).officeTextView.setText(model.getName());
        } else if (holder instanceof LogAdapter.LoadingViewHolder) {
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
    public void addNewItems(List<LogModel> newList) {
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

        TextView activityTextView;
        TextView dateTextView;
        TextView grnTextView;
        TextView officeTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            activityTextView = itemView.findViewById(R.id.activity_item_textview);
            dateTextView = itemView.findViewById(R.id.date_time_item_textView);
            grnTextView = itemView.findViewById(R.id.grn_item_tv);
            officeTextView = itemView.findViewById(R.id.office_item_textView);
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
