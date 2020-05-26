package com.example.dpmjinfo;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.NoCache;

public class FakeRequestQueue extends RequestQueue {
    public FakeRequestQueue(String response) {
        super(new NoCache(), new BasicNetwork(new FakeHttpStack(response)));
    }
}
