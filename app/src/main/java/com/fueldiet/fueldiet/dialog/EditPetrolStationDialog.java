package com.fueldiet.fueldiet.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class EditPetrolStationDialog extends AppCompatDialogFragment {

    private static final String TAG = "AddPetrolStationDialog";
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputLayout name;
    private Button selectLogo;
    private ImageView showLogo;
    private long id;
    private PetrolStationObject old;

    private Uri customImage = null;

    private EditPetrolStationDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        Bundle args = getArguments();
        id = args.getLong("id");
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(getContext());
        old = dbHelper.getPetrolStation(id);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_petrol_station, null);

        builder.setView(view)
                .setTitle("Edit petrol station")
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (name.getEditText().getText().toString().equals(""))
                            Toast.makeText(getContext(), "No name!", Toast.LENGTH_SHORT).show();
                        else {
                            PetrolStationObject stationObject = new PetrolStationObject(name.getEditText().getText().toString(), old.getOrigin());
                            if (customImage != null) {
                                File storageDIR = getContext().getDir("Images", MODE_PRIVATE);
                                File img = new File(storageDIR, old.getFileName());
                                img.delete();
                                Utils.downloadPSImage(getContext(), customImage, stationObject.getFileName());
                            } else {
                                File storageDIR = getContext().getDir("Images", MODE_PRIVATE);
                                File img = new File(storageDIR, old.getFileName());
                                img.renameTo(new File(storageDIR, stationObject.getFileName()));
                            }
                            stationObject.setId(old.getId());
                            listener.getEditStation(stationObject);
                            dialog.dismiss();
                        }
                    }
                });

        name = view.findViewById(R.id.add_petrol_station_name);
        showLogo = view.findViewById(R.id.add_petrol_station_logo);
        selectLogo = view.findViewById(R.id.add_petrol_station_add_logo);

        name.getEditText().setText(old.getName());
        if (old.getOrigin() == 0)
            name.setEnabled(false);
        selectLogo.setOnClickListener(v -> showImagePicker());
        File storageDIR = getContext().getDir("Images", MODE_PRIVATE);
        Glide.with(getContext()).load(storageDIR+"/"+old.getFileName()).diskCacheStrategy(DiskCacheStrategy.NONE).into(showLogo);

        return builder.create();
    }

    private void showImagePicker() {
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditPetrolStationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement this listener");
        }
    }

    public interface EditPetrolStationDialogListener {
        void getEditStation(PetrolStationObject stationObject);
    }
}
