package com.fueldiet.fueldiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

import static com.fueldiet.fueldiet.activity.MainActivity.PERMISSIONS_STORAGE;

public class AutomaticBackup {

    public File backupDir;
    private Context context;

    public AutomaticBackup(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
    }

    public List<File> getAllBackups() {
        List<File> files = new ArrayList<>();
        if (!backupDir.exists()) {
            return files;
        }
        files = Arrays.asList(
                Objects.requireNonNull(backupDir.listFiles((dir, name) -> {
                    if (name.contains(".csv"))
                        return true;
                    return false;
                })));
        return files;
    }

    private File[] getOldBackups() {
        if (!backupDir.exists()) {
            return new File[]{};
        }
        return backupDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains("fueldiet_autobackup_") && name.contains(".csv"))
                    return true;
                return false;
            }
        });
    }

    public void createBackup(Context context) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoBackup = pref.getBoolean("auto_backup", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!autoBackup)
                return;
        } else {
            if (!autoBackup || !EasyPermissions.hasPermissions(context, PERMISSIONS_STORAGE))
                return;
        }


        File[] files = getOldBackups();

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String formattedDate = df.format(c);

        if (files.length < 10) {
            //create backup
            File newBackup = new File(backupDir.getAbsolutePath() + "/fueldiet_autobackup_"+ formattedDate + ".csv");
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(Uri.fromFile(newBackup));
                String msg = Utils.createCSVfile(outputStream, context);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*String msg =Utils.createCSVfile(Uri.fromFile(newBackup), context);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();*/
        } else {
            //remove oldest and create new
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
            File newBackup = new File(backupDir.getAbsolutePath() + "/fueldiet_autobackup_"+ formattedDate + ".csv");
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(Uri.fromFile(newBackup));
                String msg = Utils.createCSVfile(outputStream, context);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*String msg =Utils.createCSVfile(Uri.fromFile(newBackup), context);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();*

            /*String msg = Utils.createCSVfile(Uri.fromFile(newBackup), context);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();*/
        }
    }
}
