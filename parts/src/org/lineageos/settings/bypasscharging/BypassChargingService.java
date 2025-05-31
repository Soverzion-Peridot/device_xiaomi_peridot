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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class BypassChargingService extends Service {
    private static final String TAG = "BypassChargingService";
    private BroadcastReceiver mPowerStateReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BypassChargingService started");

        if (!BypassChargingUtils.isSupported()) {
            Log.w(TAG, "Bypass charging not supported, stopping service");
            stopSelf();
            return;
        }

        mPowerStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_POWER_CONNECTED.equals(action) ||
                    Intent.ACTION_POWER_DISCONNECTED.equals(action) ||
                    Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d(TAG, "Power state changed or screen on. Reapplying bypass charging state.");
                    applyBypassChargingState(null);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mPowerStateReceiver, filter);

        applyBypassChargingState(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        applyBypassChargingState(null);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BypassChargingService stopped");

        applyBypassChargingState(false);

        if (mPowerStateReceiver != null) {
            try {
                unregisterReceiver(mPowerStateReceiver);
            } catch (Exception e) {
                Log.w(TAG, "Error unregistering receiver", e);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void applyBypassChargingState(Boolean enabled) {
        if (!BypassChargingUtils.isSupported()) {
            return;
        }

        try {
            boolean targetState = enabled != null ? enabled :
                    getSharedPreferences(BypassChargingSettingsFragment.SHARED_BYPASS_CHARGING, Context.MODE_PRIVATE)
                            .getBoolean(BypassChargingSettingsFragment.BYPASS_CHARGING_STATE, false);

            boolean currentState = BypassChargingUtils.getBypassChargingState();
            if (currentState != targetState) {
                Log.d(TAG, "Applying bypass charging state: " + targetState);
                BypassChargingUtils.setBypassChargingState(targetState);
            } else {
                Log.d(TAG, "Bypass charging state already correct: " + targetState);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying bypass charging state", e);
        }
    }
}
