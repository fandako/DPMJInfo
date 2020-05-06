package com.example.dpmjinfo.recyclerViewHandling;

import android.view.View;

public interface RecycleViewClickListener {
    public void onClick(View view, int position);
    public void onLongClick(View view,int position);
}
