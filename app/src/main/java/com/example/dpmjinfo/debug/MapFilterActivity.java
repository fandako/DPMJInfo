package com.example.dpmjinfo.debug;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;

import com.example.dpmjinfo.MapFilterItemsAdapter;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.RecycleViewClickListener;
import com.example.dpmjinfo.RecyclerViewTouchListener;

import java.util.ArrayList;
import java.util.List;

public class MapFilterActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter busStopDepartureAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_filter);

        ActionBar bar = getSupportActionBar();

        bar.setTitle("Filter");
        bar.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the required data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        //get list of bus stops
        ArrayList<String> lineFilter = (ArrayList<String>) bundle.getSerializable("com.example.dpmjinfo.lineFilter");
        ArrayList<String> lines = (ArrayList<String>) bundle.getSerializable("com.example.dpmjinfo.lines");

        recyclerView = findViewById(R.id.recyclerView2);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);

        MapFilterItemsAdapter mapFilterItemsAdapter = new MapFilterItemsAdapter(lines, lineFilter);
        recyclerView.setAdapter(mapFilterItemsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this,
                recyclerView, new RecycleViewClickListener() {
            private void openDetail(View view, int position){
                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.checkedTextView);
                MapFilterItemsAdapter mapFilterItemsAdapter = (MapFilterItemsAdapter) recyclerView.getAdapter();
                boolean wasChecked = checkedTextView.isChecked();

                //get object from position
                String item = mapFilterItemsAdapter.getItem(position);

                if(wasChecked) {
                    mapFilterItemsAdapter.removeCheckedLine(item);
                } else {
                    mapFilterItemsAdapter.addCheckedLine(item);
                }

                checkedTextView.setChecked(!wasChecked);
            }

            @Override
            public void onClick(View view, final int position) {
                openDetail(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {
                openDetail(view, position);
            }
        }));

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the result Intent
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                MapFilterItemsAdapter mapFilterItemsAdapter = (MapFilterItemsAdapter) recyclerView.getAdapter();
                bundle.putSerializable("com.example.dpmjinfo.lineFilter", (ArrayList<String>) mapFilterItemsAdapter.getCheckedLines());
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
