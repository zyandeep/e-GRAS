package project.mca.e_gras.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import project.mca.e_gras.MakePaymentActivity;
import project.mca.e_gras.R;
import project.mca.e_gras.model.SchemeModel;
import project.mca.e_gras.util.MyUtil;

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

        myViewHolder.schemeName.setText(scheme.getName());
        myViewHolder.hoa.setText(scheme.getHoa());
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


    // Remove list items
    public void removeAllItems() {
        this.schemeList.clear();

        notifyDataSetChanged();
    }


    private void displayTotalAmount() {
        // reset totalAmount
        totalAmount = 0.0;

        if (schemeList.size() > 0) {

            // calculate total amount
            for (SchemeModel obj : schemeList) {
                totalAmount += obj.getAmount();
            }

            // display the total
            TextView textView = ((MakePaymentActivity) mContext).totalAmountTextView;
            if (textView != null) {
                textView.setText(MyUtil.formatCurrency(totalAmount));
            }
        }
    }


    public double getTotalAmount() {
        return totalAmount;
    }




    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView hoa;
        TextView schemeName;
        EditText amount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            hoa = itemView.findViewById(R.id.hoa_textView);
            schemeName = itemView.findViewById(R.id.scheme_textView);
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