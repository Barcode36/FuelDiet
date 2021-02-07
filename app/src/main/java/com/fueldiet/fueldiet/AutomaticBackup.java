package com.fueldiet.fueldiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.utils.AsyncTaskCoroutine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

import static com.fueldiet.fueldiet.activity.MainActivity.PERMISSIONS_STORAGE;

public class AutomaticBackup {

    private static final String TAG = "AutomaticBackup";

    public File backupDir;
    Locale locale;
    Context context;

    public AutomaticBackup(Context context, Locale locale) {
        Log.d(TAG, "AutomaticBackup: started");
        this.locale = locale;
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and higher can only access one data folder
            File directory = context.getFilesDir();
            backupDir = new File(directory, "backups");

            if (!backupDir.exists()) {
                backupDir.mkdir();
            }
        } else {
            //Before Android 10 app created folder in storage that is visible to user
            if (EasyPermissions.hasPermissions(context, PERMISSIONS_STORAGE)) {
                File dir = Environment.getExternalStorageDirectory();

                String fueldietPath = dir.getAbsolutePath() + "/Fueldiet backups";
                backupDir = new File(fueldietPath);

                if (!backupDir.exists()) {
                    backupDir.mkdir();
                }
            }
        }
        Log.d(TAG, "AutomaticBackup: finished");
    }

    public AutomaticBackup(Context context) {
        Log.d(TAG, "AutomaticBackup: started - lite edition");
        this.locale = null;
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and higher can only access one data folder
            File directory = context.getFilesDir();
            backupDir = new File(directory, "backups");

            if (!backupDir.exists()) {
                backupDir.mkdir();
            }
        } else {
            //Before Android 10 app created folder in storage that is visible to user
            if (EasyPermissions.hasPermissions(context, PERMISSIONS_STORAGE)) {
                File dir = Environment.getExternalStorageDirectory();

                String fueldietPath = dir.getAbsolutePath() + "/Fueldiet backups";
                backupDir = new File(fueldietPath);

                if (!backupDir.exists()) {
                    backupDir.mkdir();
                }
            }
        }
        Log.d(TAG, "AutomaticBackup: finished");
    }

    public List<File> getAllBackups() {
        Log.d(TAG, "getAllBackups: started...");
        List<File> files = new ArrayList<>();
        if (!backupDir.exists()) {
            return files;
        }
        files = Arrays.asList(Objects.requireNonNull(backupDir.listFiles((dir, name) -> name.contains(".csv"))));
        Log.d(TAG, "getAllBackups: finished");
        return files;
    }

    private File[] getOldBackups() {
        Log.d(TAG, "getOldBackups: started...");
        if (!backupDir.exists()) {
            return new File[]{};
        }
        Log.d(TAG, "getOldBackups: finished");
        return backupDir.listFiles((dir, name) -> name.contains("fueldiet_autobackup_") && name.contains(".csv"));
    }

    public void createBackup(Context context) {
        Log.d(TAG, "createBackup: started...");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoBackup = pref.getBoolean("auto_backup", false);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !autoBackup) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && (!autoBackup || !EasyPermissions.hasPermissions(context, PERMISSIONS_STORAGE))) {
            return;
        }
        BackupCoroutine backupCoroutine = new BackupCoroutine();
        backupCoroutine.execute(0);
    }

    private class BackupCoroutine extends AsyncTaskCoroutine<Integer, Boolean> {
        private static final String TAG = "BackupCoroutine";

        @Override
        public void onPostExecute(@Nullable Boolean result) {
            Log.d(TAG, "onPostExecute: started...");
            if (result) {
                Toast.makeText(context, context.getString(R.string.backup_created), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.backup_error), Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "onPostExecute: finished");
        }

        @Override
        public void onPreExecute() {
            // Do nothing
        }

        @Override
        public Boolean doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground");
            File[] files = getOldBackups();

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", locale);
            String formattedDate = df.format(c);

            if (files.length > 15) {
                //remove oldest
                long oldestDate = Long.MAX_VALUE;
                File oldestFile = null;
                for(File f : files){
                    if(f.lastModified() < oldestDate){
                        oldestDate = f.lastModified();
                        oldestFile = f;
                    }
                }
                if(oldestFile != null){
                    oldestFile.delete();
                }
            }

            //create backup
            File newBackup = new File(backupDir.getAbsolutePath() + "/fueldiet_autobackup_"+ formattedDate + ".csv");
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(Uri.fromFile(newBackup));
                return Utils.createCsvFile(outputStream, context);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
