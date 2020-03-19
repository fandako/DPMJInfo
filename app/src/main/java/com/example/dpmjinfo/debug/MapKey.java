package com.example.dpmjinfo.debug;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.dpmjinfo.R;

public class MapKey extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_key);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
