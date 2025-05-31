/*
 * Copyright (C) 2015-2016 The CyanogenMod Project
 *               2017 The LineageOS Project
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

import android.os.Bundle;
import android.util.Log;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

public class BypassChargingSettingsActivity extends CollapsingToolbarBaseActivity {
    private static final String TAG = "BypassChargingActivity";
    private static final String TAG_BYPASS_CHARGING = "bypasscharging";
    private static final int CONTENT_FRAME_ID = com.android.settingslib.collapsingtoolbar.R.id.content_frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating bypass charging settings activity");

        try {
            getFragmentManager().beginTransaction()
                    .replace(CONTENT_FRAME_ID, new BypassChargingSettingsFragment(), TAG_BYPASS_CHARGING)
                    .commit();
            Log.d(TAG, "Successfully loaded bypass charging settings fragment");
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment", e);
            finish();
        }
    }
}
