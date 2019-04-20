package project.mca.e_gras.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import project.mca.e_gras.R;
import project.mca.e_gras.model.OfficeModel;

public class OfficeSpinnerAdapter extends ArrayAdapter<OfficeModel> {

    public OfficeSpinnerAdapter(@NonNull Context context, @NonNull List<OfficeModel> modelList) {
        super(context, 0, modelList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // inflate a new view

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.spinner_item, null);
        }

        // get the model which name to be displayed as the spinner item
        OfficeModel model = getItem(position);

        if (model != null) {
            TextView tv = convertView.findViewById(R.id.spinner_item_textView);
            tv.setText(model.getName());

        }

        return convertView;
    }
}