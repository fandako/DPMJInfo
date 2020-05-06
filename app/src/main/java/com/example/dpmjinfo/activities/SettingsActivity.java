package com.example.dpmjinfo.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.example.dpmjinfo.R;
import com.example.dpmjinfo.helpers.NetworkHelper;
import com.example.dpmjinfo.helpers.OfflineFileManagerRequestsDoneListener;
import com.example.dpmjinfo.helpers.OfflineFilesManager;

import java.util.ArrayList;
import java.util.Hashtable;

public class SettingsActivity extends AppCompatActivity implements OfflineFileManagerRequestsDoneListener {

    private SettingsFragment settingsFragment;
    private OfflineFilesManager ofm;
    AlertDialog.Builder alertBuilder = null;

    static final int DOWNLOAD_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        settingsFragment = new SettingsFragment(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ofm = new OfflineFilesManager(this);


    }

    /**
     * deletes base map from local storage
     * @return true ons success, false otherwise
     */
    private boolean deleteBaseMap() {
        String mapFilePath = ofm.getFilePath(OfflineFilesManager.MAP);

        if(!mapFilePath.equals("")) {
            boolean deleteResult = ofm.deleteFile(OfflineFilesManager.MAP);
            if(deleteResult) {
                Toast.makeText(this, getString(R.string.base_map_deleted_toast), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.base_map_delete_error_toast), Toast.LENGTH_SHORT).show();
            }

            return deleteResult;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request it is that we're responding to
        if (requestCode == DOWNLOAD_REQUEST) {
            if (resultCode == RESULT_OK) {

            } else {
                //download canceled or failed
                alertBuilder = new AlertDialog.Builder(this);
                alertBuilder
                        .setMessage(getString(R.string.download_error_alert_message))
                        .setTitle(getString(R.string.download_error_alert_title))
                        .setPositiveButton(getString(R.string.no_internet_connection_ok_button_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = alertBuilder.create();
                alert.show();
            }

        }
    }

    @Override
    public void onOfflineFileManagerRequestsDone(Hashtable<String, String> results) {
        ArrayList<String> urls = new ArrayList<>();
        for (String key : results.keySet()
        ) {
            urls.add(results.get(key));
        }

        if (!urls.isEmpty()) {
            //download canceled or failed
            alertBuilder = new AlertDialog.Builder(this);
            alertBuilder
                    .setCancelable(false)
                    .setMessage(getString(R.string.update_available_alert_message))
                    .setTitle(getString(R.string.update_available_alert_title))
                    .setPositiveButton(getString(R.string.update_available_alert_ok_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OfflineFilesManager ofm = new OfflineFilesManager(getApplicationContext());
                            Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("com.example.dpmjinfo.url", results);
                            bundle.putSerializable("com.example.dpmjinfo.downloadDir", ofm.getDownloadDir());
                            intent.putExtras(bundle);
                            startActivityForResult(intent, DOWNLOAD_REQUEST);
                        }
                    })
                    .setNeutralButton(getString(R.string.update_available_alert_cancel_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = alertBuilder.create();
            alert.show();
        } else {
            Toast.makeText(this, getString(R.string.schedules_are_actual_toast), Toast.LENGTH_SHORT).show();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SwitchPreference mapModeSwitch;
        private Preference deleteMap;
        private Preference updateSchedule;
        AlertDialog.Builder alertBuilder = null;
        private SettingsActivity settingsActivity;
        private OfflineFilesManager ofm;

        public SettingsFragment(SettingsActivity s){
            super();
            settingsActivity = s;
            ofm = new OfflineFilesManager(settingsActivity);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            mapModeSwitch = findPreference("map_base_map");
            deleteMap = findPreference("delete_base_map");
            updateSchedule = findPreference("update_schedule");

            if(ofm.getFilePath(OfflineFilesManager.MAP).equals("")){
                deleteMap.setVisible(false);
            }

            mapModeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean useOffline = (boolean) newValue;
                    SwitchPreference s = (SwitchPreference) preference;
                    String mapFilePath = ofm.getFilePath(OfflineFilesManager.MAP);

                    //if switch to use online map, ask user if downloaded base map should be deleted
                    if(!useOffline) {
                        if (!mapFilePath.equals("")) {
                            alertBuilder = new AlertDialog.Builder(settingsActivity);
                            alertBuilder
                                    .setCancelable(false)
                                    .setMessage(getString(R.string.base_map_delete_alert_message))
                                    .setTitle(getString(R.string.base_map_delete_alert_title))
                                    .setPositiveButton(getString(R.string.base_map_delete_alert_positive_button), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            s.setChecked(false);
                                            if (settingsActivity.deleteBaseMap()) {
                                                deleteMap.setVisible(false);
                                            }
                                        }
                                    })
                                    .setNeutralButton(getString(R.string.base_map_delete_alert_negative_button), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            s.setChecked(false);
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert = alertBuilder.create();
                            alert.show();
                        }
                    }

                    s.setChecked(useOffline);
                    return useOffline;
                }
            });

            deleteMap.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(settingsActivity.deleteBaseMap()){
                        deleteMap.setVisible(false);
                    }

                    return false;
                }
            });

            updateSchedule.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (NetworkHelper.isNetworkAvailable(settingsActivity)) {
                        ArrayList<String> requiredFiles = new ArrayList<>();
                        requiredFiles.add(OfflineFilesManager.SCHEDULE);
                        requiredFiles.add(OfflineFilesManager.CALENDAR);
                        ofm.getFilesToDownload(settingsActivity, requiredFiles);
                    } else {
                        Toast.makeText(settingsActivity, settingsActivity.getString(R.string.no_internet_connection_alert_message), Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}