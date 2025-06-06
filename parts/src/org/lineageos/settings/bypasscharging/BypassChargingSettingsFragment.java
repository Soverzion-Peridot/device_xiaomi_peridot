/*
 * Copyright (C) 2018-2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.bypasscharging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import org.lineageos.settings.R;

public class BypassChargingSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "BypassChargingFragment";
    private static final String BYPASS_CHARGING_ENABLE_KEY = "bypass_charging_enable";
    private static final String PER_APP_BYPASS_CHARGING_KEY = "per_app_bypass_charging";
    public static final String SHARED_BYPASS_CHARGING = "SHARED_BYPASS_CHARGING";
    public static final String BYPASS_CHARGING_STATE = "bypass_charging_state";

    private SwitchPreference mBypassChargingPreference;
    private Preference mPerAppPreference;
    private SharedPreferences mPrefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.bypass_charging_settings);
        try {
            if (getActivity() instanceof AppCompatActivity) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not set up action bar", e);
        }

        mBypassChargingPreference = findPreference(BYPASS_CHARGING_ENABLE_KEY);
        mPerAppPreference = findPreference(PER_APP_BYPASS_CHARGING_KEY);
        mPrefs = getActivity().getSharedPreferences(SHARED_BYPASS_CHARGING, Context.MODE_PRIVATE);

        if (!BypassChargingUtils.isSupported()) {
            Log.w(TAG, "Bypass charging not supported");
            mBypassChargingPreference.setEnabled(false);
            mBypassChargingPreference.setSummary(R.string.not_supported);
            mPerAppPreference.setEnabled(false);
            mPerAppPreference.setSummary(R.string.not_supported);
            return;
        }

        boolean bypassEnabled = mPrefs.getBoolean(BYPASS_CHARGING_STATE, false);
        mBypassChargingPreference.setChecked(bypassEnabled);
        mBypassChargingPreference.setOnPreferenceChangeListener(this);

        mPerAppPreference.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), BypassChargingPerAppActivity.class);
            startActivity(intent);
            return true;
        });

        syncAndApplyState(bypassEnabled);
        Log.d(TAG, "Fragment initialized with state: " + bypassEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (BYPASS_CHARGING_ENABLE_KEY.equals(preference.getKey())) {
            boolean isEnabled = (Boolean) newValue;
            Log.d(TAG, "Preference changed to: " + isEnabled);
            try {
                mPrefs.edit().putBoolean(BYPASS_CHARGING_STATE, isEnabled).apply();
                syncAndApplyState(isEnabled);
                Log.d(TAG, "Successfully applied state: " + isEnabled);
            } catch (Exception e) {
                Log.e(TAG, "Error changing state", e);
                return false;
            }
        }
        return true;
    }

    private void syncAndApplyState(boolean enabled) {
        try {
            boolean hardwareState = BypassChargingUtils.getBypassChargingState();
            if (hardwareState != enabled) {
                Log.d(TAG, "Syncing hardware state to preference: " + enabled);
                BypassChargingUtils.setBypassChargingState(enabled);
            }
            manageService(enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error syncing state", e);
        }
    }

    private void manageService(boolean enable) {
        try {
            Intent serviceIntent = new Intent(getActivity(), BypassChargingService.class);
            if (enable || !BypassChargingUtils.getPerAppBypassEnabledApps(getActivity()).isEmpty()) {
                getActivity().startService(serviceIntent);
                Log.d(TAG, "Started service");
            } else {
                getActivity().stopService(serviceIntent);
                Log.d(TAG, "Stopped service");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error managing service", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BypassChargingUtils.isSupported() && mBypassChargingPreference != null) {
            boolean bypassEnabled = mPrefs.getBoolean(BYPASS_CHARGING_STATE, false);
            syncAndApplyState(bypassEnabled);
        }
    }
}
