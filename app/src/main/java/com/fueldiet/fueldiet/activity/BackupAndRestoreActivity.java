package com.fueldiet.fueldiet.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.fueldiet.fueldiet.dialog.LoadingDialog;
import com.fueldiet.fueldiet.utils.AsyncTaskCoroutine;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

public class BackupAndRestoreActivity extends BaseActivity {
    private static final String TAG = "BackupAndRestore";
    private static final int RESTORE_CODE = 0;
    private static final int BACKUP_CODE = 1;
    private static final int SAVE_AS_CODE = 2;
    private static final String TYPE = "text/*";
    private static final String DO_IN_BACKGROUND = "doInBackground";
    private static final String POST_STARTED = "onPostExecute: started...";
    private static final String POST_FINISHED = "onPostExecute: finished";
    private static final String PRE_STARTED = "onPreExecute: started...";
    private static final String PRE_FINISHED = "onPreExecute: finished";

    MaterialButton backup;
    MaterialButton restore;
    TextView defaultDir;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    FoldersAdapter adapter;
    AutomaticBackup automaticBackup;
    List<File> data;
    Locale locale;
    File backupToMove;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: starting");
        backupToMove = null;

        setContentView(R.layout.activity_backup_and_restore);
        automaticBackup = new AutomaticBackup(this, locale);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        setTitle(R.string.backup_and_restore);

        initVariables();
        getData();
        initAdapter();
        addClickListeners();
        finishSetUp();
        
        Log.d(TAG, "onCreate: finished");
    }
    
    private void initVariables() {
        Log.d(TAG, "initVariables: started...");
        backup = findViewById(R.id.activity_backup_button_backup);
        restore = findViewById(R.id.activity_backup_button_restore);
        defaultDir = findViewById(R.id.activity_backup_textview_folder);
        recyclerView = findViewById(R.id.activity_backup_recyclerview_restore);
        data = new ArrayList<>();
        loadingDialog = new LoadingDialog(this);
        Log.d(TAG, "initVariables: finished");
    }
    
    private void initAdapter() {
        Log.d(TAG, "initAdapter: started...");
        layoutManager = new LinearLayoutManager(this);
        adapter = new FoldersAdapter(this, data);
        adapter.setOnItemClickListener(new FoldersAdapter.OnItemClickListener() {
            @Override
            public void onItemClickOld(int position) {
                selectFolderOld(position);
            }

            @Override
            public void onItemClickNew(int position) {
                selectFolderNew(position);
            }
        });
        Log.d(TAG, "initAdapter: finished");
    }
    
    private void addClickListeners() {
        Log.d(TAG, "addClickListeners: started...");
        Context context = this;

        backup.setOnClickListener(v -> {

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", locale);
            String formattedDate = df.format(c);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TYPE);
                intent.putExtra(Intent.EXTRA_TITLE, String.format(locale,"fueldiet_%s.csv", formattedDate));
                startActivityForResult(intent, BACKUP_CODE);
            } else {
                MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(context);
                final EditText edittext = new EditText(context);
                alert.setTitle(R.string.enter_backup_file_name);
                alert.setMessage("");

                alert.setView(edittext);
                edittext.setText(String.format(locale,"fueldiet_%s.csv", formattedDate));

                alert.setPositiveButton(context.getString(R.string.confirm), (dialog, whichButton) -> {
                    String fileName = edittext.getText().toString();
                    File newBackup = new File(automaticBackup.backupDir.getAbsolutePath() + "/" + fileName);
                    dialog.dismiss();
                    backup(Uri.fromFile(newBackup));
                });
                alert.setNegativeButton(context.getString(R.string.cancel), (dialog, whichButton) -> dialog.cancel());
                alert.show();
            }
        });
        restore.setOnClickListener(v -> {
            Intent fileSource = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileSource.addCategory(Intent.CATEGORY_OPENABLE);
            fileSource.setType(TYPE);

            try {
                startActivityForResult(fileSource, RESTORE_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, "addClickListeners: finished");
    }
    
    private void finishSetUp() {
        Log.d(TAG, "finishSetUp: started...");
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                defaultDir.setText(getString(R.string.android_10_saving));
            else
                defaultDir.setText(automaticBackup.backupDir.getCanonicalPath());
        } catch (IOException e) {
            defaultDir.setText(getString(R.string.error));
        }
        Log.d(TAG, "finishSetUp: finished");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("onActivityResult: started with request: %d", requestCode));

        if (requestCode == RESTORE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult: restore");
            Uri uri = data.getData();
            restore(uri);
        } else if (requestCode == BACKUP_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //only android 10 and up can end up here
            Log.d(TAG, "onActivityResult: backup");
            Uri uri = data.getData();
            backup(uri);
        } else if (requestCode == SAVE_AS_CODE && resultCode == RESULT_OK && data != null && data.getData() != null && backupToMove != null) {
            //only for android 10 and up
            Log.d(TAG, "onActivityResult: save as file");
            //save file from app storage to user selected folder
            Uri uri = data.getData();
            saveFile(uri);
        }
    }

    private void restore(Uri uri) {
        Log.d(TAG, "restore: started...");
        RestoreCoroutine restoreCoroutine = new RestoreCoroutine();
        restoreCoroutine.execute(uri);
        Log.d(TAG, "restore: finished");
    }

    private void backup(Uri uri) {
        Log.d(TAG, "backup: started...");
        BackupCoroutine backupCoroutine = new BackupCoroutine();
        backupCoroutine.execute(uri);
        Log.d(TAG, "backup: finished");
    }

    private void saveFile(Uri uri) {
        Log.d(TAG, "saveFile: started...");
        SaveFileCoroutine saveFileCoroutine = new SaveFileCoroutine();
        saveFileCoroutine.execute(uri);
        Log.d(TAG, "saveFile: finished");
    }

    private void getData() {
        Log.d(TAG, "getData: started");
        data.clear();
        data.addAll(automaticBackup.getAllBackups());
        data.sort((o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
        Log.d(TAG, "getData: finished");
    }

    private void selectFolderOld(int position) {
        Log.d(TAG, "selectFolderOld: dialog");
        File selected = data.get(position);
        Date d = new Date(selected.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy, HH:mm", locale);

        LayoutInflater factory = LayoutInflater.from(this);
        final View confirmDialogView = factory.inflate(R.layout.dialog_backup_and_restore_old, null);
        final AlertDialog confirmDialog = new MaterialAlertDialogBuilder(this).create();
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
        final AlertDialog confirmDialog = new MaterialAlertDialogBuilder(this).create();
        confirmDialog.setView(confirmDialogView);

        confirmDialogView.findViewById(R.id.dialog_backup_restore_button_restore).setOnClickListener(v -> {
            Log.d(TAG, "selectFolderNew: pressed restore button");
            confirmDialog.dismiss();
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(selected));
                assert inputStream != null;
                boolean msg = Utils.readCsvFile(inputStream, getApplicationContext());
                Toast.makeText(this, Boolean.toString(msg), Toast.LENGTH_SHORT).show();
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
        intent.setType(TYPE);
        backupToMove = from;

        startActivityForResult(intent, SAVE_AS_CODE);
    }



    private class RestoreCoroutine extends AsyncTaskCoroutine<Uri, Boolean> {
        private static final String TAG = "RestoreCoroutine";

        @Override
        public void onPostExecute(@Nullable Boolean result) {
            Log.d(TAG, POST_STARTED);
            if (result) {
                loadingDialog.setSuccessful();
            } else {
                loadingDialog.setError();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                loadingDialog.hideDialog();
                setResult(MainActivity.RESULT_RESTORE_10_UP);
            }, 1500);
            Log.d(TAG, POST_FINISHED);
        }

        @Override
        public void onPreExecute() {
            Log.d(TAG, PRE_STARTED);
            loadingDialog.showDialog();
            Log.d(TAG, PRE_FINISHED);
        }

        @Override
        public Boolean doInBackground(Uri... params) {
            Log.d(TAG, DO_IN_BACKGROUND);
            try {
                InputStream inputStream = getContentResolver().openInputStream(params[0]);
                assert inputStream != null;
                return Utils.readCsvFile(inputStream, getApplicationContext());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onActivityResult: file was not found on restore backup", e.fillInStackTrace());
                return false;
            }
        }
    }

    private class BackupCoroutine extends AsyncTaskCoroutine<Uri, Boolean> {
        private static final String TAG = "BackupCoroutine";

        @Override
        public void onPostExecute(@Nullable Boolean result) {
            Log.d(TAG, POST_STARTED);
            if (result) {
                loadingDialog.setSuccessful();
            } else {
                loadingDialog.setError();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> loadingDialog.hideDialog(), 1500);
            Log.d(TAG, POST_FINISHED);
        }

        @Override
        public void onPreExecute() {
            Log.d(TAG, PRE_STARTED);
            loadingDialog.showDialog();
            Log.d(TAG, PRE_FINISHED);
        }

        @Override
        public Boolean doInBackground(Uri... params) {
            Log.d(TAG, DO_IN_BACKGROUND);
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(params[0]);
                assert outputStream != null;
                return Utils.createCsvFile(outputStream, getApplicationContext());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onActivityResult: file was not found on create backup", e.fillInStackTrace());
                return false;
            }
        }
    }

    private class SaveFileCoroutine extends AsyncTaskCoroutine<Uri, Boolean> {
        private static final String TAG = "SaveFileCoroutine";

        @Override
        public void onPostExecute(@Nullable Boolean result) {
            Log.d(TAG, POST_STARTED);
            if (result) {
                loadingDialog.setSuccessful();
            } else {
                loadingDialog.setError();
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> loadingDialog.hideDialog(), 1500);
            Log.d(TAG, POST_FINISHED);
        }

        @Override
        public void onPreExecute() {
            Log.d(TAG, PRE_STARTED);
            loadingDialog.showDialog();
            Log.d(TAG, PRE_FINISHED);
        }

        @Override
        public Boolean doInBackground(Uri... params) {
            Log.d(TAG, DO_IN_BACKGROUND);
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(params[0]);
                assert outputStream != null;
                outputStream.write(Files.readAllBytes(backupToMove.toPath()));
                outputStream.close();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: error when moving file from app storage to user defined location", e.fillInStackTrace());
                return false;
            }
        }
    }
}
