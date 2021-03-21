package com.fueldiet.fueldiet.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.CostItemObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class CostItemDialog extends DialogFragment {

    private static final String TAG = "CostItemDialog";

    MaterialButton addButton;
    MaterialButton cancelButton;
    TextInputLayout itemName;
    TextInputLayout itemPrice;
    TextInputLayout itemDescription;

    private CostItemDialog.CostItemDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getActivity()).create();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_cost_item, null);
        alertDialog.setView(view);

        addButton = view.findViewById(R.id.search_prices_search_button);
        cancelButton = view.findViewById(R.id.search_prices_cancel_button);
        itemName = view.findViewById(R.id.dialog_cost_item_title);
        itemPrice = view.findViewById(R.id.dialog_cost_item_price);
        itemDescription = view.findViewById(R.id.dialog_cost_item_desc);

        addButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: search button");
            CostItemObject costItemObject = new CostItemObject();
            if (itemName.getEditText().getText().toString().isEmpty()) {
                itemName.setError("Field is required");
            }
            costItemObject.setName(itemName.getEditText().getText().toString());
            if (itemPrice.getEditText().getText().toString().isEmpty()) {
                itemPrice.setError("Field is required");
            }
            costItemObject.setPrice(Double.parseDouble(itemPrice.getEditText().getText().toString()));
            costItemObject.setDescription(itemDescription.getEditText().getText().toString());
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            alertDialog.dismiss();
            this.listener.addItem(costItemObject);
        });

        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: cancel button");
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            alertDialog.cancel();
        });
        return alertDialog;
    }

    public void setNewDialogListener(CostItemDialogListener listener) {
        this.listener = listener;
    }

    public interface CostItemDialogListener {
        void addItem(CostItemObject costItemObject);
    }
}
