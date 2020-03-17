package com.example.dpmjinfo;

import android.graphics.drawable.Drawable;

public abstract class MapObjectSelectionItem {
    public abstract Integer getIconDrawableID();
    public abstract String getTitle();
    public abstract Class<?> getDetailActivityClass();
    public abstract String getBundleID();
    public abstract Object getObject();
    public abstract Class<?> getObjectClass();
}
