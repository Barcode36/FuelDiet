package com.fueldiet.fueldiet.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class StationsPricesSearchDialog extends AppCompatDialogFragment {

    private static final String TAG = "StationsPricesSearchDialog";
    private static final String CLEANED_FRANCHISES = "CLEANED FRANCHISES";
    private static final String AVAILABLE_RADIUS = "AVAILABLE RADIUS";
    private static final String CITY = "CITY";
    private static final String RADIUS = "RADIUS";
    private static final String FRANCHISE = "FRANCHISE";

    Button currentLocation, search, cancel;
    TextInputLayout cityName;
    AutoCompleteTextView franchises;
    SeekBar radius;
    TextView seekValue;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getActivity()).create();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_search_stations_prices, null);
        alertDialog.setView(view);

        Bundle bundle = getArguments();

        List<String> availableFranchises = bundle.getStringArrayList(CLEANED_FRANCHISES);
        List<String> availableRadius = bundle.getStringArrayList(AVAILABLE_RADIUS);

        currentLocation = view.findViewById(R.id.search_prices_location);
        search = view.findViewById(R.id.search_prices_search_button);
        cancel = view.findViewById(R.id.search_prices_cancel_button);
        cityName = view.findViewById(R.id.search_prices_city_input);
        radius = view.findViewById(R.id.search_prices_radius_seekbar);
        franchises = view.findViewById(R.id.search_prices_franchises_value);
        seekValue = view.findViewById(R.id.search_radius_value);

        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: seekbar set to " + availableRadius.get(progress));
                seekValue.setText(availableRadius.get(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        radius.setProgress(1);
        radius.setProgress(0);

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use device location
            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item, availableFranchises);
        arrayAdapter.setDropDownViewResource(R.layout.list_item);
        franchises.setAdapter(arrayAdapter);
        franchises.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: new franchise selected " + s.toString());
            }
        });
        franchises.setText("/", false);

        search.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: search button");
            Intent intent = new Intent();
            intent.putExtra(CITY, cityName.getEditText().getText().toString());
            intent.putExtra(RADIUS, availableRadius.get(radius.getProgress()));
            intent.putExtra(FRANCHISE, franchises.getText().toString());
            alertDialog.dismiss();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        });

        cancel.setOnClickListener(v -> {
            Log.d(TAG, "onClick: cancel button");
            alertDialog.cancel();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, new Intent());
        });
        return alertDialog;
    }
}
