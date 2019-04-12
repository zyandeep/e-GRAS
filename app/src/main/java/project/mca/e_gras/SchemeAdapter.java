package project.mca.e_gras;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import project.mca.e_gras.model.SchemeModel;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class SchemeAdapter
        extends RecyclerView.Adapter<SchemeAdapter.MyViewHolder> {

    public static final String TAG = "MY-APP";

    private double totalAmount = 0.0;


    Context mContext;
    List<SchemeModel> schemeList;


    public SchemeAdapter(Context mContext, List<SchemeModel> schemeList) {
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
    public void onBindViewHolder(@NonNull final SchemeAdapter.MyViewHolder myViewHolder, int position) {

        SchemeModel scheme = schemeList.get(position);

        myViewHolder.scheme_name.setText(scheme.getName());
        myViewHolder.ac_no.setText(scheme.getAcNo());
        myViewHolder.amount.setText(String.valueOf(scheme.getAmount()));
    }


    @Override
    public int getItemCount() {
        return schemeList.size();
    }


    // Add new items to the list
    public void addNewItems(List<SchemeModel> newList) {
        schemeList.clear();
        schemeList.addAll(newList);

        displayTotalAmount();

        notifyDataSetChanged();
    }


    private void displayTotalAmount() {
        if (schemeList.size() > 0) {

            // reset totalAmount
            totalAmount = 0.0;

            // calculate total amount
            for (SchemeModel obj : schemeList) {
                totalAmount += obj.getAmount();
            }

            // display the total
            TextView textView = ((MakePaymentActivity) mContext).totalAmountTextView;
            if (textView != null) {
                textView.setText(String.format("%.2f", totalAmount));
            }
        }
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

            amount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    int value = 0;

                    SchemeModel obj = schemeList.get(position);

                    try {
                        value = Integer.valueOf(s.toString());
                    } catch (Exception ex) {
                        if (s.toString().equals("")) {
                            value = 0;
                        }
                    }

                    obj.setAmount(value);
                    displayTotalAmount();
                }
            });

        }
    }
}