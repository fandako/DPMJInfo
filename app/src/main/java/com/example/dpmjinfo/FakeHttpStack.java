package com.example.dpmjinfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FakeHttpStack extends BaseHttpStack {
    private static final int SIMULATED_DELAY_MS = 200;
    private String responseString;

    public FakeHttpStack(String response) {
        this.responseString = response;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        List<Header> headers = new ArrayList<>();
        InputStream targetStream = new ByteArrayInputStream(responseString.getBytes());

        HttpResponse response
                = new HttpResponse(200, headers, responseString.length(), targetStream);

        return response;
    }
}
