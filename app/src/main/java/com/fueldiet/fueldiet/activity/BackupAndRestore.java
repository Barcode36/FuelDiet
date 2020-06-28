package com.fueldiet.fueldiet.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.adapter.FoldersAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupAndRestore extends BaseActivity {
    private static final String TAG = "BackupAndRestore";
    private static final int RESTORE_CODE = 0;
    private static final int BACKUP_CODE = 1;
    private static final int SAVE_AS_CODE = 2;

    Button backup, restore;
    TextView defaultDir;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    FoldersAdapter mAdapter;
    AutomaticBackup automaticBackup;
    List<File> data;
    Locale locale;
    File backupToMove;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: starting");
        backupToMove = null;

        setContentView(R.layout.activity_backup_and_restore);
        automaticBackup = new AutomaticBackup(this);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        Log.d(TAG, "onCreate: linking fields");
        backup = findViewById(R.id.activity_backup_button_backup);
        restore = findViewById(R.id.activity_backup_button_restore);
        defaultDir = findViewById(R.id.activity_backup_textview_folder);
        mRecyclerView = findViewById(R.id.activity_backup_recyclerview_restore);
        data = new ArrayList<>();

        fillData();

        mLayoutManager= new LinearLayoutManager(this);
        mAdapter = new FoldersAdapter(this, data);
        mAdapter.setOnItemClickListener(new FoldersAdapter.OnItemClickListener() {
            @Override
            public void onItemClickOld(int position) {
                selectFolderOld(position);
            }

            @Override
            public void onItemClickNew(int position) {
                selectFolderNew(position);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Context context = this;

        backup.setOnClickListener(v -> {

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", locale);
            String formattedDate = df.format(c);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_TITLE, String.format(locale,"fueldiet_%s.csv", formattedDate));

                startActivityForResult(intent, BACKUP_CODE);
            } else {

                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                final EditText edittext = new EditText(context);
                alert.setTitle(R.string.enter_backup_file_name);
                alert.setMessage("");

                alert.setView(edittext);
                edittext.setText(String.format(locale,"fueldiet_%s.csv", formattedDate));

                alert.setPositiveButton(context.getString(R.string.confirm), (dialog, whichButton) -> {
                    String fileName = edittext.getText().toString();
                    File newBackup = new File(automaticBackup.backupDir.getAbsolutePath() + "/" + fileName);
                    dialog.dismiss();
                    Intent passData = new Intent();
                    //---set the data to pass back---
                    passData.setData(Uri.fromFile(newBackup));
                    setResult(MainActivity.RESULT_BACKUP, passData);
                    //---close the activity---
                    finish();
                });

                alert.setNegativeButton(context.getString(R.string.cancel), (dialog, whichButton) -> dialog.cancel());

                alert.show();
            }
        });

        restore.setOnClickListener(v -> {
            Intent fileSource = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileSource.addCategory(Intent.CATEGORY_OPENABLE);
            fileSource.setType("text/*");

            //Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", automaticBackup.backupDir);
            //fileDest.setDataAndType(uri, "text/*");
            try {
                startActivityForResult(fileSource, RESTORE_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                defaultDir.setText(getString(R.string.android_10_saving));
            else
                defaultDir.setText(automaticBackup.backupDir.getCanonicalPath());
        } catch (IOException e) {
            defaultDir.setText(getString(R.string.error));
        }
        Log.d(TAG, "onCreate: finished");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("onActivityResult: started with request: %d", requestCode));

        if (requestCode == RESTORE_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Log.d(TAG, "onActivityResult: started to restore");
                    Uri uri = data.getData();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Log.d(TAG, "onActivityResult: restoring on android 10 or higher");
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            assert inputStream != null;
                            String msg = Utils.readCSVfile(inputStream, getApplicationContext());
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "onActivityResult: file was not found on restore backup", e.fillInStackTrace());
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: restoring on android 9 or lower");
                        Intent passData = new Intent();
                        //---set the data to pass back---
                        passData.setData(uri);
                        setResult(MainActivity.RESULT_RESTORE, passData);
                        //---close the activity---
                        finish();
                    }
                }
            }
        } else if (requestCode == BACKUP_CODE) {
            //only for android 10 and up
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Log.d(TAG, "onActivityResult: started to backup on android 10 or higher");
                    Uri uri = data.getData();
                    try {
                        OutputStream outputStream = getContentResolver().openOutputStream(uri);
                        assert outputStream != null;
                        String msg = Utils.createCSVfile(outputStream, getApplicationContext());
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "onActivityResult: file was not found on create backup", e.fillInStackTrace());
                        e.printStackTrace();
                    }
                }
            }
        } else if (requestCode == SAVE_AS_CODE) {
            //only for android 10 and up
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null && backupToMove != null) {
                    Log.d(TAG, "onActivityResult: started to copy backup file");
                    //save file from app storage to user selected folder
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                            assert outputStream != null;
                            outputStream.write(Files.readAllBytes(backupToMove.toPath()));
                            outputStream.close();
                            Toast.makeText(this, getString(R.string.backup_created), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "onActivityResult: error when moving file from app storage to user defined location", e.fillInStackTrace());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void fillData() {
        Log.d(TAG, "fillData: started");
        data.clear();
        data.addAll(automaticBackup.getAllBackups());
        data.sort((o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
        Log.d(TAG, "fillData: finished");
    }

    private void selectFolderOld(int position) {
        Log.d(TAG, "selectFolderOld: dialog");
        File selected = data.get(position);
        Date d = new Date(selected.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy, HH:mm", locale);

        LayoutInflater factory = LayoutInflater.from(this);
        final View confirmDialogView = factory.inflate(R.layout.dialog_backup_and_restore_old, null);
        final AlertDialog confirmDialog = new AlertDialog.Builder(this).create();
        confirmDialog.setView(confirmDialogView);
        confirmDialogView.findViewById(R.id.dialog_backup_restore_button_restore).setOnClickListener(v -> {
            Log.d(TAG, "selectFolderOld: pressed restore button");
            confirmDialog.dismiss();
            Intent passData = new Intent();
            //---set the data to pass back---
            passData.setData(Uri.fromFile(selected));
            setResult(MainActivity.RESULT_RESTORE, passData);
            //---close the activity---
            finish();
        });
        confirmDialogView.findViewById(R.id.dialog_backup_restore_button_cancel).setOnClickListener(v -> {
            Log.d(TAG, "selectFolderOld: pressed cancel button");
            confirmDialog.cancel();
        });

        TextView when = confirmDialogView.findViewById(R.id.dialog_backup_restore_created);
        when.setText(sdf.format(d));
        TextView size = confirmDialogView.findViewById(R.id.dialog_backup_restore_size);
        double kb = selected.length() / 1024.0;
        if (kb < 1025)
            size.setText(String.format(locale,"%.0f KB", kb));
        else
            size.setText(String.format(locale, "%.2f MB", (kb / 1024.0)));
        confirmDialog.show();
    }

    private void selectFolderNew(int position) {
        Log.d(TAG, "selectFolderNew: dialog for android 10 or higher");
        File selected = data.get(position);
        Date d = new Date(selected.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy, HH:mm", locale);

        LayoutInflater factory = LayoutInflater.from(this);
        final View confirmDialogView = factory.inflate(R.layout.dialog_backup_and_restore_new, null);
        final AlertDialog confirmDialog = new AlertDialog.Builder(this).create();
        confirmDialog.setView(confirmDialogView);

        confirmDialogView.findViewById(R.id.dialog_backup_restore_button_restore).setOnClickListener(v -> {
            Log.d(TAG, "selectFolderNew: pressed restore button");
            confirmDialog.dismiss();
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(selected));
                assert inputStream != null;
                String msg = Utils.readCSVfile(inputStream, getApplicationContext());
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onActivityResult: file was not found on restore backup", e.fillInStackTrace());
                e.printStackTrace();
            }
        });

        confirmDialogView.findViewById(R.id.dialog_backup_restore_button_cancel).setOnClickListener(v -> {
            Log.d(TAG, "selectFolderNew: pressed cancel button");
            confirmDialog.cancel();
        });

        confirmDialogView.findViewById(R.id.dialog_backup_restore_button_save).setOnClickListener(v -> {
            Log.d(TAG, "selectFolderNew: pressed save backup file");
            confirmDialog.dismiss();
            loadAndSaveFile(selected);
        });

        TextView when = confirmDialogView.findViewById(R.id.dialog_backup_restore_created);
        when.setText(sdf.format(d));
        TextView size = confirmDialogView.findViewById(R.id.dialog_backup_restore_size);
        double kb = selected.length() / 1024.0;
        if (kb < 1025)
            size.setText(String.format(locale,"%.0f KB", kb));
        else
            size.setText(String.format(locale, "%.2f MB", (kb / 1024.0)));
        confirmDialog.show();
    }

    private void loadAndSaveFile(File from) {
        Log.d(TAG, "loadAndSaveFile: started saving file from app storage to user selected");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, from.getName());
        intent.setType("text/*");
        backupToMove = from;

        startActivityForResult(intent, SAVE_AS_CODE);
    }
}
