package project.mca.e_gras;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class SchemeAdapter
        extends RecyclerView.Adapter<SchemeAdapter.MyViewHolder> {

    Context mContext;
    List<Scheme> schemeList;


    public SchemeAdapter(Context mContext, List<Scheme> schemeList) {
        this.mContext = mContext;
        this.schemeList = schemeList;
    }


    @NonNull
    @Override
    public SchemeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View itemView = inflater.inflate(R.layout.item_view, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeAdapter.MyViewHolder myViewHolder, int position) {
        Scheme scheme = schemeList.get(position);

        myViewHolder.scheme_name.setText(scheme.getName());
        myViewHolder.ac_no.setText(scheme.getAcNo());
    }

    @Override
    public int getItemCount() {
        return schemeList.size();
    }



    // Add new items to the list
    public void addNewItems(List<Scheme> newList) {
        schemeList.clear();
        schemeList.addAll(newList);

        notifyDataSetChanged();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView ac_no;
        TextView scheme_name;
        EditText amount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ac_no = itemView.findViewById(R.id.acc_no_textView);
            scheme_name = itemView.findViewById(R.id.scheme_textView);
            amount = itemView.findViewById(R.id.scheme_amount_edit_text);
        }
    }
}
