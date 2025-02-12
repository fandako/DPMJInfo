package com.example.dpmjinfo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.dpmjinfo.R;
import com.example.dpmjinfo.Vehicle;

/**
 * activity for displaying vehicle detail
 */
public class VehicleDetailActivity extends AppCompatActivity {
    private TextView line;
    private TextView terminal;
    private TextView lastStop;
    private TextView delay;
    private TextView delayLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        getSupportActionBar().setTitle(getResources().getString(R.string.vehicle_detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Vehicle vehicle = (Vehicle) bundle.getSerializable("com.android.dpmjinfo.vehicle");

        line = findViewById(R.id.line);
        terminal = findViewById(R.id.terminal);
        lastStop = findViewById(R.id.last);
        delay = findViewById(R.id.delay);
        delayLabel = findViewById(R.id.delayLabel);

        if(vehicle.isWaiting()){
            delayLabel.setText(getResources().getString(R.string.vehicle_detail_time_to_departure_label));
        }

        line.setText(vehicle.getLine());
        terminal.setText(vehicle.getTerminalStop());
        lastStop.setText(vehicle.getLastStop());

        if(vehicle.getDelayInMins() <= 0 && !vehicle.isWaiting()){
            delay.setText(getResources().getString(R.string.vehicle_detail_without_delay));
        }else {
            delay.setText(String.format("%d min", Math.abs(vehicle.getDelayInMins().intValue())));
        }

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
