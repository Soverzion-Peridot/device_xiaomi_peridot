/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
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
import android.content.SharedPreferences;
import android.util.Log;

import org.lineageos.settings.utils.FileUtils;

import java.io.File;

public final class BypassChargingUtils {
    private static final String TAG = "BypassChargingUtils";
    public static final String BYPASS_CHARGING_NODE = "/sys/class/qcom-battery/night_charging";

    private BypassChargingUtils() {}

    public static boolean isSupported() {
        try {
            File nodeFile = new File(BYPASS_CHARGING_NODE);
            boolean exists = nodeFile.exists();
            boolean canRead = nodeFile.canRead();
            boolean canWrite = nodeFile.canWrite();
            Log.d(TAG, "Node check - exists: " + exists + ", canRead: " + canRead + ", canWrite: " + canWrite);
            return exists && canRead && canWrite;
        } catch (Exception e) {
            Log.e(TAG, "Error checking bypass charging support", e);
            return false;
        }
    }

    public static void restoreBypassChargingValue(Context context) {
        if (!isSupported()) {
            Log.w(TAG, "Bypass charging not supported");
            return;
        }

        try {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    BypassChargingSettingsFragment.SHARED_BYPASS_CHARGING, Context.MODE_PRIVATE);
            boolean bypassEnabled = sharedPref.getBoolean(
                    BypassChargingSettingsFragment.BYPASS_CHARGING_STATE, false);
            Log.d(TAG, "Restoring state: " + bypassEnabled);
            setBypassChargingState(bypassEnabled);
        } catch (Exception e) {
            Log.e(TAG, "Error restoring bypass charging value", e);
        }
    }

    public static boolean getBypassChargingState() {
        if (!isSupported()) {
            Log.w(TAG, "Bypass charging not supported");
            return false;
        }

        try {
            String currentState = FileUtils.readOneLine(BYPASS_CHARGING_NODE);
            if (currentState != null) {
                boolean state = currentState.trim().equals("1");
                Log.d(TAG, "Current state: " + state);
                return state;
            }
            Log.w(TAG, "Failed to read state, node returned null");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error reading state", e);
            return false;
        }
    }

    public static boolean setBypassChargingState(boolean enabled) {
        if (!isSupported()) {
            Log.w(TAG, "Bypass charging not supported");
            return false;
        }

        try {
            int state = enabled ? 1 : 0;
            boolean success = FileUtils.writeOneLine(BYPASS_CHARGING_NODE, Integer.toString(state));
            if (success) {
                Log.d(TAG, "Set state to: " + enabled);
                if (getBypassChargingState() != enabled) {
                    Log.w(TAG, "State verification failed. Expected: " + enabled);
                    return false;
                }
            } else {
                Log.e(TAG, "Failed to write state");
                return false;
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error setting state", e);
            return false;
        }
    }

    public static void debugNodeInfo() {
        if (!isSupported()) {
            Log.d(TAG, "Debug: Node not supported");
            return;
        }

        try {
            File nodeFile = new File(BYPASS_CHARGING_NODE);
            Log.d(TAG, "Debug node info: Path=" + BYPASS_CHARGING_NODE +
                    ", Exists=" + nodeFile.exists() +
                    ", Readable=" + nodeFile.canRead() +
                    ", Writable=" + nodeFile.canWrite() +
                    ", Value=" + FileUtils.readOneLine(BYPASS_CHARGING_NODE));
        } catch (Exception e) {
            Log.e(TAG, "Error debugging node info", e);
        }
    }
}
