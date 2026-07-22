package com.tablet.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DisclosureActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclosure);

        Button btnAgree = findViewById(R.id.btnAgree);
        Button btnDecline = findViewById(R.id.btnDecline);

        btnAgree.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        btnDecline.setOnClickListener(v -> {
            finishAffinity();
        });
    }
}