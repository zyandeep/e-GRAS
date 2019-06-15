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
import project.mca.e_gras.model.SchemeModel;
import project.mca.e_gras.util.MyUtil;


public class SelectedSchemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "MY-APP";
    List<SchemeModel> schemeList;
    Context context;
    LayoutInflater inflater;


    public SelectedSchemeAdapter(List<SchemeModel> schemeList, Context context) {
        this.schemeList = schemeList;
        this.context = context;

        inflater = LayoutInflater.from(this.context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.scheme_item_view, parent, false);
        return new SelectedSchemeAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SchemeModel scheme = schemeList.get(position);

        ((MyViewHolder) holder).hoa.setText(scheme.getHoa());
        ((MyViewHolder) holder).name.setText(scheme.getName());
        ((MyViewHolder) holder).amount.setText(MyUtil.formatCurrency(scheme.getAmount()));
    }

    @Override
    public int getItemCount() {
        return schemeList.size();
    }


    // View Holder to hold non-empty item view
    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView hoa;
        TextView amount;
        TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            hoa = itemView.findViewById(R.id.hoa_textView);
            amount = itemView.findViewById(R.id.scheme_amount_textview);
            name = itemView.findViewById(R.id.scheme_name_textView);
        }
    }
}
