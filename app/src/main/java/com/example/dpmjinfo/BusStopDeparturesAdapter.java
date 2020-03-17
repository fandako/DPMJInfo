package com.example.dpmjinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BusStopDeparturesAdapter extends RecyclerView.Adapter<BusStopDeparturesAdapter.MyViewHolder>{
        List<BusStopDeparture> mDepartures;
        public BusStopDeparturesAdapter(List<BusStopDeparture> departures) {
            this.mDepartures = departures;
        }

        public List<BusStopDeparture> getDepartureList(){
            return mDepartures;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.busstop_departure_list_item, viewGroup, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, int i) {
            BusStopDeparture data=mDepartures.get(i);
            viewHolder.line.setText(data.getLine());
            viewHolder.name.setText(data.getName());
            viewHolder.departure.setText(data.getDeparture());
        }

        @Override
        public int getItemCount() {
            return mDepartures.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView line, name, departure;
            LinearLayout parent;
            public MyViewHolder(View itemView) {
                super(itemView);
                parent = itemView.findViewById(R.id.parent);
                line = itemView.findViewById(R.id.line);
                name = itemView.findViewById(R.id.name);
                departure = itemView.findViewById(R.id.departure);
            }
        }
}
