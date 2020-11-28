package com.fueldiet.fueldiet.dialog;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.fueldiet.fueldiet.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoadingDialog extends AppCompatDialogFragment {

    private Activity activity;
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
}
