package com.example.dpmjinfo;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConnectionAdapter extends BaseAdapter<BaseAdapter.BaseViewHolder, Connection> {
    private int highlightedLayout;
    private int layout;

    public ConnectionAdapter(@LayoutRes int layout) {
        this.layout = layout;
    }

    @NonNull
    @Override
    public BaseAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading, viewGroup, false));
            default:
                throw new Error();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == items.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public List<Integer> getHighlightedPositions() {
        return new ArrayList<>();
    }

    public class ViewHolder extends BaseAdapter.BaseViewHolder {
        TextView departure, time, arrival;
        LinearLayout departures;
        Context context;

        ViewHolder(View itemView) {
            super(itemView);
            departures = itemView.findViewById(R.id.connectionDepartures);
            time = itemView.findViewById(R.id.connectionTime);
            departure = itemView.findViewById(R.id.connectionDeparture);
            arrival = itemView.findViewById(R.id.connectionArrival);
            context = itemView.getContext();
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);

            Connection data = items.get(position);

            String firstDeparture = data.get(0).getDeparture();
            String lastArrival = data.get(data.size() - 1).getDeparture();
            departure.setText(firstDeparture);
            arrival.setText(lastArrival);

            String timeFormat = ScheduleQuery.getTimeFormat();
            DateTime departureTime = DateTime.parse(firstDeparture, DateTimeFormat.forPattern(timeFormat));
            DateTime arrivalTime = DateTime.parse(lastArrival, DateTimeFormat.forPattern(timeFormat));

            Period period = new Period(departureTime, arrivalTime);

            long diffSeconds = period.getSeconds();
            long diffMinutes = period.getMinutes();
            long diffHours = period.getHours();
            long diffDays = period.getDays();

            StringBuilder b = new StringBuilder();
            if (diffDays > 0) {
                b.append(diffDays);
                b.append(" d ");
            }
            if (diffHours > 0) {
                b.append(diffHours);
                b.append(" h ");
            }
            b.append(diffMinutes);
            b.append(" min");

            time.setText(b);

            departures.removeAllViews();

            BusStopDeparture currentLineDeparture = data.get(0);
            BusStopDeparture previous = currentLineDeparture;
            for (BusStopDeparture d : data.getDepartures()) {

                if (currentLineDeparture.getLineId() != d.getLineId()) {
                    LinearLayout l = getDepartureView(context, currentLineDeparture, true);
                    departures.addView(l);

                    l = getDepartureView(context, previous, false);
                    departures.addView(l);

                    currentLineDeparture = d;
                }
                previous = d;
            }

            LinearLayout l = getDepartureView(context, currentLineDeparture, true);
            departures.addView(l);

            l = getDepartureView(context, previous, false);
            departures.addView(l);
        }

        private LinearLayout getDepartureView(Context context, BusStopDeparture departure, boolean withLine) {
            LinearLayout l = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.busstop_departure_list_item, null, false);

            if (withLine) {
                TextView lineName = ((TextView) l.findViewById(R.id.line));
                lineName.setText(departure.getLine());
                lineName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            } else {
                ((TextView) l.findViewById(R.id.line)).setText("");
            }

            ((TextView) l.findViewById(R.id.name)).setText(departure.getName());
            ((TextView) l.findViewById(R.id.departure)).setText(departure.getDeparture());

            return l;
        }
    }
}
