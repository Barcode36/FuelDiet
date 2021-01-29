package com.fueldiet.fueldiet.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.fueldiet.fueldiet.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.Objects;


public class TextInputValidator implements TextWatcher {

    private static final String TAG = "TextInputValidator";
    private static final String FIELD_EMPTY = "Field cannot be empty!";

    private final TextInputLayout layout;
    private final Context context;
    private final Locale locale;

    public TextInputValidator(Context context, Locale locale, TextInputLayout layout, TextInputEditText editText) {
        this.context = context;
        this.layout = layout;
        this.locale = locale;

        editText.addTextChangedListener(this);
    }

    public TextInputValidator(Context context, Locale locale, TextInputLayout layout, AutoCompleteTextView autoCompleteTextView) {
        this.context = context;
        this.layout = layout;
        this.locale = locale;

        autoCompleteTextView.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Only check for text when user has stop entering it
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Only check for text when user has stop entering it
    }

    @Override
    public void afterTextChanged(Editable s) {
        isEmpty();
    }

    public boolean isEmpty() {
        if (Objects.requireNonNull(layout.getEditText()).getText().toString().equals("")) {
            if (layout.getError() == null || !layout.getError().toString().equals(FIELD_EMPTY)) {
                Log.d(TAG, "isEmpty: new error");
                layout.setError(FIELD_EMPTY);
            }
            return true;
        } else {
            Log.d(TAG, "isEmpty: removed error");
            layout.setError(null);
            return false;
        }
    }

    public boolean areKilometresWrong(String kmMode, int prevOdoKm) {
        String value = Objects.requireNonNull(layout.getEditText()).getText().toString();
        if (isEmpty()) {
            return true;
        }
        if (kmMode.equals(context.getString(R.string.total_meter)) && prevOdoKm > Integer.parseInt(value)) {
            if (layout.getError() == null || !layout.getError().toString().equals("Kilometres should be higher!")) {
                Log.d(TAG, "checkKilometres: new error");
                layout.setError("Kilometres should be higher!");
            }
            return true;
        }
        displayPrevKm(prevOdoKm);
        return false;
    }

    /**
     * Display previous drive odo
     */
    private void displayPrevKm(int prevOdoKm) {
        if (prevOdoKm != 0) {
            layout.setHelperText(String.format(locale, "ODO: %dkm", prevOdoKm));
        } else {
            layout.setHelperText(this.context.getString(R.string.odo_km_no_km_yet));
        }
    }
}
