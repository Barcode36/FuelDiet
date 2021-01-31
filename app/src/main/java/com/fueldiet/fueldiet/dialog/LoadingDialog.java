package com.fueldiet.fueldiet.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.fueldiet.fueldiet.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoadingDialog extends AppCompatDialogFragment {

    private final Activity activity;
    private AlertDialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog() {
        dialog.dismiss();
    }

    public boolean isDisplayed() {
        return dialog != null && dialog.isShowing();
    }

    public void setSuccessful() {
        dialog.findViewById(R.id.dialog_loading_indicator).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.dialog_loading_success_img).setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.dialog_loading_text);
        textView.setText(activity.getString(R.string.success));
        dialog.findViewById(R.id.dialog_loading_unit).setVisibility(View.INVISIBLE);
    }

    public void setError() {
        dialog.findViewById(R.id.dialog_loading_indicator).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.dialog_loading_failed_img).setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.dialog_loading_text);
        textView.setText(activity.getString(R.string.error));
        dialog.findViewById(R.id.dialog_loading_unit).setVisibility(View.INVISIBLE);
    }
}
