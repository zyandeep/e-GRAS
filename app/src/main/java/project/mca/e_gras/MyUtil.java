package project.mca.e_gras;

import android.content.Context;
import android.view.View;

import com.marcoscg.dialogsheet.DialogSheet;

import androidx.core.content.ContextCompat;

class MyUtil {

    static void showBottomDialog(Context context, String msg) {

        // show the bottomSheet dialog
        // context : Activity context not Application's

        new DialogSheet(context)
                .setTitle(R.string.error_label_bottom_dialog)
                .setMessage(msg)
                .setColoredNavigationBar(true)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close the dialog
                    }
                })
                .setRoundedCorners(true)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
                .show();
    }
}
