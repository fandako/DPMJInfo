package com.example.dpmjinfo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BusStopDeparturesAdapter extends /*RecyclerView.Adapter<BusStopDeparturesAdapter.BaseViewHolder>*/ BaseAdapter<BusStopDeparturesAdapter.BaseViewHolder, BusStopDeparture> {
    private List<BusStopDeparture> mDepartures;
    //private List<BusStopDeparture> highlightedItems;
    private int layout;
    private int highlightedLayout;
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_HIGHLIGHTED = 2;
    //private boolean isLoaderVisible = false;

    public BusStopDeparturesAdapter(List<BusStopDeparture> departures) {
        this.mDepartures = departures;
        layout = R.layout.busstop_departure_list_item;
        highlightedLayout = R.layout.busstop_departure_list_item_highlighted;
        highlightedItems = new ArrayList<>();
    }

    public BusStopDeparturesAdapter(List<BusStopDeparture> departures, @LayoutRes int layout) {
        this.mDepartures = departures;
        this.layout = layout;
        highlightedLayout = R.layout.busstop_departure_list_item_highlighted;
        highlightedItems = new ArrayList<>();
    }

    /*public void highlightItem(BusStopDeparture departure) {
        highlightedItems.add(departure);
        //Log.d("dbg", "" + departure.getLineId() + " " + departure.getConnectionId() + " " + departure.getDeparture() + " " + departure.getName() + " " + departure.getLine());

    }*/

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

    public List<BusStopDeparture> getDepartureList() {
        return mDepartures;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        /*View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(layout, viewGroup, false);
        return new BaseViewHolder(*/

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
                        return null;
                }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int i) {
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
            if(isOnSameLine/*highlightedItems.contains(getItem(position))*/){
                return VIEW_TYPE_HIGHLIGHTED;
            } else {
                return VIEW_TYPE_NORMAL;
            }
        }
    }

    /*public void addItems(List<BusStopDeparture> postItems) {
        mDepartures.addAll(postItems);
        notifyDataSetChanged();
    }

    public void addLoading() {
        isLoaderVisible = true;
        mDepartures.add(new BusStopDeparture("", "", ""));
        notifyItemInserted(mDepartures.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        if(!mDepartures.isEmpty()) {
            int position = mDepartures.size() - 1;
            BusStopDeparture item = getItem(position);
            if (item != null) {
                mDepartures.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    BusStopDeparture getItem(int position) {
        return mDepartures.get(position);
    }

    public void clear() {
        mDepartures.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDepartures.size();
    }*/

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        private int mCurrentPosition;

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void clear();

        public void onBind(int position) {
            mCurrentPosition = position;
            clear();
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }
    }

    public class ViewHolder extends BaseViewHolder {
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

    public class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void clear() {
        }
    }
}
