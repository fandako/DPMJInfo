package com.example.dpmjinfo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BusStopDeparturesAdapter extends  BaseAdapter<BaseAdapter.BaseViewHolder, BusStopDeparture> {

    public BusStopDeparturesAdapter(List<BusStopDeparture> departures) {
        super();

        items = departures;
        layout = R.layout.busstop_departure_list_item;
        highlightedLayout = R.layout.busstop_departure_list_item_highlighted;
        highlightedItems = new ArrayList<>();
    }

    public BusStopDeparturesAdapter(List<BusStopDeparture> departures, @LayoutRes int layout) {
        super();

        items = departures;
        this.layout = layout;
        highlightedLayout = R.layout.busstop_departure_list_item_highlighted;
        highlightedItems = new ArrayList<>();
    }

    public List<Integer> getHighlightedPositions() {
        ArrayList<Integer> result = new ArrayList<>();

        for (BusStopDeparture h:highlightedItems) {
            for (int i = 0; i < getItemCount(); i++) {
                if(h.isSameDepartureButWithTerminalStopName(getItem(i))){
                    result.add(i);
                    break;
                }
            }
        }

        return result;
    }

    @NonNull
    @Override
    public BaseAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                switch (i) {
                    case VIEW_TYPE_NORMAL:
                        return new ViewHolder(
                                LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false));
                    case VIEW_TYPE_HIGHLIGHTED:
                        return new ViewHolder(
                                LayoutInflater.from(viewGroup.getContext()).inflate(highlightedLayout, viewGroup, false));
                    case VIEW_TYPE_LOADING:
                        return new ProgressHolder(
                                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading, viewGroup, false));
                    default:
                        throw new Error();
                }
    }

    @Override
    public void onBindViewHolder(BaseAdapter.BaseViewHolder viewHolder, int i) {
        viewHolder.onBind(i);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == /*mDepartures.size() - 1*/ items.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            //Log.d("dbg", "" + getItem(position).getLineId() + " " + getItem(position).getConnectionId() + " " + getItem(position).getDeparture()+ " " + getItem(position).getName() + " " + getItem(position).getLine());
            boolean isOnSameLine = false;
            for (BusStopDeparture h:highlightedItems) {
                if(h.isSameDepartureButWithTerminalStopName(getItem(position))){
                    isOnSameLine = true;
                    break;
                }
            }
            if(isOnSameLine){
                return VIEW_TYPE_HIGHLIGHTED;
            } else {
                return VIEW_TYPE_NORMAL;
            }
        }
    }

    public class ViewHolder extends BaseAdapter.BaseViewHolder {
        TextView line, name, departure;
        LinearLayout parent;

        ViewHolder(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            line = itemView.findViewById(R.id.line);
            name = itemView.findViewById(R.id.name);
            departure = itemView.findViewById(R.id.departure);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);

            BusStopDeparture data = items.get(position); //mDepartures.get(position);
            line.setText(data.getLine());
            name.setText(data.getName());
            departure.setText(data.getDeparture());
        }
    }
}
