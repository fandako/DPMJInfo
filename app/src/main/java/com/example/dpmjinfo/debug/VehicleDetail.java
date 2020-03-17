package com.example.dpmjinfo.debug;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.dpmjinfo.R;
import com.example.dpmjinfo.Vehicle;

public class VehicleDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        getSupportActionBar().setTitle("Detail vozu");

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Vehicle vehicle = (Vehicle) bundle.getSerializable("com.android.dpmjinfo.vehicle");
    }
}
