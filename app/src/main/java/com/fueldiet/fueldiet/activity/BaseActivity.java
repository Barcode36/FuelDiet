package com.fueldiet.fueldiet.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        String localSelected = PreferenceManager.getDefaultSharedPreferences(context).getString("language_select", "english");
        Locale locale;
        if ("slovene".equals(localSelected)) {
            locale = new Locale("sl", "SI");
        } else {
            locale = new Locale("en", "GB");
        }
        Locale.setDefault(locale);

        return updateResourcesLocale(context, locale);
    }

    private Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
