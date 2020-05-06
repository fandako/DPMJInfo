package com.example.dpmjinfo.helpers;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * class for loading actual departures
 */
public class ElpDepartureHelper {

    /**
     * loads actual departures
     * @param busStop bus stop to load actual departures for
     * @return list of departures
     */
    public static List<BusStopDeparture> getDepartures(BusStop busStop) {
        ArrayList<BusStopDeparture> departures = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();

        String url = busStop.getHref();

        try {
            Document doc = Jsoup.connect(url).get();
            Elements divs = doc.select("div.stationNameBox");

            for (Element div : divs) {
                //builder.append("\n").append(div.previousElementSibling().text()).append(" ").append(div.text()).append(" ").append(div.nextElementSibling().nextElementSibling().text());
                BusStopDeparture departure = new BusStopDeparture(div.previousElementSibling().text(), div.text(), div.nextElementSibling().nextElementSibling().text());
                departures.add(departure);
            }
        } catch (IOException e) {
            builder.append("Error : ").append(e.getMessage()).append("\n");
        }

        return departures;
    }
}
