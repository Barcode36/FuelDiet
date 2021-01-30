package com.fueldiet.fueldiet.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.fueldiet.fueldiet.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DeletingDialog extends AppCompatDialogFragment {
    private final Activity activity;
    private AlertDialog dialog;

    public DeletingDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_deleting, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setSuccessful() {
        ImageView successImage = dialog.findViewById(R.id.delete_dialog_success);
        successImage.setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.delete_dialog_text);
        textView.setText(activity.getString(R.string.success));
        TextView textUnit = dialog.findViewById(R.id.delete_dialog_unit);
        textUnit.setVisibility(View.INVISIBLE);
    }

    public boolean isDisplayed() {
        return dialog != null && dialog.isShowing();
    }
}
