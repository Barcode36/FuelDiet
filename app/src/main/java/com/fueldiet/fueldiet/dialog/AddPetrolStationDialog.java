package com.fueldiet.fueldiet.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class AddPetrolStationDialog extends DialogFragment {

    private static final String TAG = "AddPetrolStationDialog";
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputLayout name;
    private ImageView showLogo;

    private Uri customImage;

    private AddPetrolStationDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: started...");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_petrol_station, null);

        builder.setView(view)
                .setTitle("Add new petrol station")
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel())
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    if (name.getEditText().getText().toString().equals(""))
                        Toast.makeText(getContext(), "No name!", Toast.LENGTH_SHORT).show();
                    else {
                        PetrolStationObject stationObject = new PetrolStationObject(name.getEditText().getText().toString(), 1);
                        Utils.downloadPSImage(getContext(), customImage, stationObject.getFileName());
                        try {
                            Bitmap img = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), customImage);
                            stationObject.setLogo(img);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        listener.getNewStation(stationObject);
                        dialog.dismiss();
                    }
                });
        name = view.findViewById(R.id.add_petrol_station_name);
        showLogo = view.findViewById(R.id.add_petrol_station_logo);
        MaterialButton selectLogo = view.findViewById(R.id.add_petrol_station_add_logo);
        selectLogo.setOnClickListener(v -> showImagePicker());

        Log.d(TAG, "onCreateDialog: finished");
        return builder.create();
    }

    public void setNewDialogListener(AddPetrolStationDialogListener listener) {
        this.listener = listener;
    }

    private void showImagePicker() {
        Log.d(TAG, "showImagePicker");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            customImage = data.getData();
            Glide.with(this).load(customImage).diskCacheStrategy(DiskCacheStrategy.NONE).into(showLogo);
        }
    }

    public interface AddPetrolStationDialogListener {
        void getNewStation(PetrolStationObject stationObject);
    }
}
