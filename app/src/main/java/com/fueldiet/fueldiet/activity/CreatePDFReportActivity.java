package com.fueldiet.fueldiet.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.fueldiet.fueldiet.R;

public class CreatePDFReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pdf_report);

        ActionBar toolbar = getSupportActionBar();
        toolbar.setTitle(getString(R.string.create_pdf_report));
    }
}
