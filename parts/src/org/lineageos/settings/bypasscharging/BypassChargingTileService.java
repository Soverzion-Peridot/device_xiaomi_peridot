/*
 * Copyright (C) 2024 The LineageOS Project
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
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import org.lineageos.settings.R;
import org.lineageos.settings.bypasscharging.BypassChargingUtils;
import org.lineageos.settings.bypasscharging.BypassChargingService;
import org.lineageos.settings.bypasscharging.BypassChargingSettingsFragment;

public class BypassChargingTileService extends TileService {
    private static final String TAG = "BypassChargingTile";

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.d(TAG, "Tile added");
        updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.d(TAG, "Tile removed");
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.d(TAG, "Start listening");
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d(TAG, "Stop listening");
    }

    @Override
    public void onClick() {
        super.onClick();
        Log.d(TAG, "Tile clicked");

        if (!BypassChargingUtils.isSupported()) {
            Log.w(TAG, "Bypass charging not supported");
            updateTile();
            return;
        }

        try {
            SharedPreferences sharedPref = getSharedPreferences(
                    BypassChargingSettingsFragment.SHARED_BYPASS_CHARGING, Context.MODE_PRIVATE);
            boolean currentState = sharedPref.getBoolean(
                    BypassChargingSettingsFragment.BYPASS_CHARGING_STATE, false);
            boolean newState = !currentState;

            Log.d(TAG, "Changing state to: " + newState);
            sharedPref.edit().putBoolean(BypassChargingSettingsFragment.BYPASS_CHARGING_STATE, newState).apply();
            if (!BypassChargingUtils.setBypassChargingState(newState)) {
                Log.e(TAG, "Failed to apply state, reverting");
                sharedPref.edit().putBoolean(BypassChargingSettingsFragment.BYPASS_CHARGING_STATE, currentState).apply();
                return;
            }

            Intent serviceIntent = new Intent(this, BypassChargingService.class);
            if (newState) {
                startService(serviceIntent);
                Log.d(TAG, "Started service");
            } else {
                stopService(serviceIntent);
                Log.d(TAG, "Stopped service");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling click", e);
        } finally {
            updateTile();
        }
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            Log.w(TAG, "Tile is null");
            return;
        }

        try {
            if (!BypassChargingUtils.isSupported()) {
                setTileState(tile, Tile.STATE_UNAVAILABLE, R.string.not_supported);
                return;
            }

            SharedPreferences sharedPref = getSharedPreferences(
                    BypassChargingSettingsFragment.SHARED_BYPASS_CHARGING, Context.MODE_PRIVATE);
            boolean preferenceState = sharedPref.getBoolean(
                    BypassChargingSettingsFragment.BYPASS_CHARGING_STATE, false);
            boolean hardwareState = BypassChargingUtils.getBypassChargingState();

            if (preferenceState != hardwareState) {
                Log.w(TAG, "State mismatch - preference: " + preferenceState + ", hardware: " + hardwareState);
            }

            setTileState(tile, preferenceState ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE,
                    preferenceState ? R.string.enabled : R.string.disabled);
        } catch (Exception e) {
            Log.e(TAG, "Error updating tile", e);
            setTileState(tile, Tile.STATE_UNAVAILABLE, R.string.not_supported); // Mengganti "Error" dengan resource yang valid
        }
    }

    private void setTileState(Tile tile, int state, int subtitleResId) {
        tile.setState(state);
        tile.setLabel(getString(R.string.bypass_charging_title));
        tile.setSubtitle(getString(subtitleResId));
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_battery_bypass));
        tile.updateTile();
        Log.d(TAG, "Tile updated - state: " + state);
    }
}
