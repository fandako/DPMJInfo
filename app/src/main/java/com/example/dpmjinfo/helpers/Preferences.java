package com.example.dpmjinfo.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * helper class for retrieving app preferences
 */
public class Preferences {

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean useOfflineBaseMap() {
        return sharedPreferences.getBoolean("map_base_map", true);
    }
}
