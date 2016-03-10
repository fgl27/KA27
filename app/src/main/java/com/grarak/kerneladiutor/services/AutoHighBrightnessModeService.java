/*
 * Copyright (C) 2015 Willi Ye
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

package com.grarak.kerneladiutor.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;

/**
 * Created by willi on 08.03.15.
 */
public class AutoHighBrightnessModeService extends Service {
    float lux = 0, oldlux = 0, newlux = 0;
    public static int LuxThresh = 50000;
    public static boolean AutoHBMSensorEnabled = false, HBMActive = false;

    private SensorManager sMgr;
    Sensor light;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAutoHBMReceiver(getApplicationContext());
    }

    private void init() {
        registerAutoHBMReceiver(getApplicationContext());
    }

    public void activateLightSensorRead() {
        sMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        light = sMgr.getDefaultSensor(Sensor.TYPE_LIGHT);

        sMgr.registerListener(_SensorEventListener, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void deactivateLightSensorRead() {
        sMgr.unregisterListener(_SensorEventListener);
        AutoHBMSensorEnabled = false;
    }

    SensorEventListener _SensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (AutoHBMSensorEnabled) {
                // Store old lux value
                oldlux = newlux;
                // Store new lux value
                newlux = event.values[0];
                // Average both the old value and new value to give some smoothing to reduce jitter
                lux = (oldlux + newlux) / 2;

                HBMActive = Utils.readFile(Constants.SCREEN_HBM).equals("1");

                if (lux >= LuxThresh && !HBMActive) {
                    Log.i("Kernel Adiutor: ", "AutoHBMService Activating HBM: received LUX value: " + lux + " Threshold: " + LuxThresh);
                    Control.runCommand("1", Constants.SCREEN_HBM, Control.CommandType.GENERIC, getApplicationContext());
                }
                if (lux < LuxThresh && HBMActive) {
                    Log.i("Kernel Adiutor: ", "De-Activation: AutoHBMService: received LUX value: " + lux + " Threshold: " + LuxThresh);
                    Control.runCommand("0", Constants.SCREEN_HBM, Control.CommandType.GENERIC, getApplicationContext());
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private BroadcastReceiver AutoHBMreceiver = null;

    private void registerAutoHBMReceiver(Context context) {
        final IntentFilter autohbmfilter = new IntentFilter();
        /** System Defined Broadcast */
        autohbmfilter.addAction(android.content.Intent.ACTION_SCREEN_ON);
        autohbmfilter.addAction(android.content.Intent.ACTION_SCREEN_OFF);

        AutoHBMreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, android.content.Intent intent) {
                String strAction = intent.getAction();
                if (strAction.equals(android.content.Intent.ACTION_SCREEN_OFF)) {
                    if (!Utils.getBoolean("AutoHBM", false, getApplicationContext())) {
                        AutoHBMSensorEnabled = false;
                        LuxThresh = Utils.getInt("AutoHBM_Threshold", 1500, getApplicationContext());
                    }
                    deactivateLightSensorRead();
                }

                if (strAction.equals(android.content.Intent.ACTION_SCREEN_ON)) {
                    if (Utils.getBoolean("AutoHBM", false, getApplicationContext())) {
                        AutoHBMSensorEnabled = true;
                        LuxThresh = Utils.getInt("AutoHBM_Threshold", 1500, getApplicationContext());
                    }
                    activateLightSensorRead();
                }
            }
        };

        context.registerReceiver(AutoHBMreceiver, autohbmfilter);
    }

    private void unregisterAutoHBMReceiver(Context context) {
        int apiLevel = Build.VERSION.SDK_INT;

        if (apiLevel >= 7) {
            try {
                context.unregisterReceiver(AutoHBMreceiver);
            } catch (IllegalArgumentException e) {
                AutoHBMreceiver = null;
            }
        } else {
            AutoHBMreceiver = null;
        }
    }
}

