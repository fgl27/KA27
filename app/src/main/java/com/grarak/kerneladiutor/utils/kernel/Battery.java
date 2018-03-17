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
package com.grarak.kerneladiutor.utils.kernel;

import android.content.Context;

import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;

/**
 * Created by willi on 03.01.15.
 */
public class Battery implements Constants {

    public static int getChargingCurrent() {
        return Utils.stringToInt(Utils.readFile(BATTERY_CHARGING_CURRENT));
    }

    public static String getChargingType() {
        return Utils.readFile(BATTERY_CHARGING_TYPE);
    }

    public static String getHealth() {
        return Utils.readFile(BATTERY_HEALTH);
    }

    public static void setChargingRate(int value, Context context) {
        Control.runCommand(String.valueOf(value), CUSTOM_CHARGING_RATE, Control.CommandType.GENERIC, context);
    }

    public static int getChargingRate() {
        return Utils.stringToInt(Utils.readFile(CUSTOM_CHARGING_RATE));
    }

    public static boolean hasChargingRate() {
        return Utils.existFile(CUSTOM_CHARGING_RATE);
    }

    public static void activateCustomChargeRate(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", CHARGE_RATE_ENABLE, Control.CommandType.GENERIC, context);
    }

    public static boolean isCustomChargeRateActive() {
        return Utils.readFile(CHARGE_RATE_ENABLE).equals("1");
    }

    public static boolean hasCustomChargeRateEnable() {
        return Utils.existFile(CHARGE_RATE_ENABLE);
    }

    public static boolean hasChargeRate() {
        return Utils.existFile(CHARGE_RATE);
    }

    public static void setBlx(int value, Context context) {
        Control.runCommand(String.valueOf(value), BLX, Control.CommandType.GENERIC, context);
    }

    public static int getCurBlx() {
        return Utils.stringToInt(Utils.readFile(BLX));
    }

    public static boolean hasBlx() {
        return Utils.existFile(BLX);
    }

    public static void activateForceFastCharge(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", FORCE_FAST_CHARGE, Control.CommandType.GENERIC, context);
    }

    public static boolean isForceFastChargeActive() {
        return Utils.readFile(FORCE_FAST_CHARGE).equals("1");
    }

    public static boolean hasForceFastCharge() {
        return Utils.existFile(FORCE_FAST_CHARGE);
    }

    public static void activateC0State(boolean active, Context context) {
        String path = C0STATE;
        for (int i = 0; i < CPU.getCoreCount(); i++) {
            Control.runCommand(active ? "1" : "0", path.replace("0", Integer.toString(i)), Control.CommandType.GENERIC, context);
        }
    }


    public static boolean isC0StateActive() {
        return Utils.readFile(C0STATE).equals("1");
    }

    public static boolean hasC0State() {
        return Utils.existFile(C0STATE);
    }

    public static void activateC1State(boolean active, Context context) {
        String path = C1STATE;
        for (int i = 0; i < CPU.getCoreCount(); i++) {
            Control.runCommand(active ? "1" : "0", path.replace("0", Integer.toString(i)), Control.CommandType.GENERIC, context);
        }
    }


    public static boolean isC1StateActive() {
        return Utils.readFile(C1STATE).equals("1");
    }

    public static boolean hasC1State() {
        return Utils.existFile(C1STATE);
    }

    public static void activateC2State(boolean active, Context context) {
        String path = C2STATE;
        for (int i = 0; i < CPU.getCoreCount(); i++) {
            Control.runCommand(active ? "1" : "0", path.replace("0", Integer.toString(i)), Control.CommandType.GENERIC, context);
        }
    }


    public static boolean isC2StateActive() {
        return Utils.readFile(C2STATE).equals("1");
    }

    public static boolean hasC2State() {
        return Utils.existFile(C2STATE);
    }

    public static void activateC3State(boolean active, Context context) {
        String path = C3STATE;
        for (int i = 0; i < CPU.getCoreCount(); i++) {
            Control.runCommand(active ? "1" : "0", path.replace("0", Integer.toString(i)), Control.CommandType.GENERIC, context);
        }
    }

    public static boolean isC3StateActive() {
        return Utils.readFile(C3STATE).equals("1");
    }

    public static boolean hasC3State() {
        return Utils.existFile(C3STATE);
    }

    public static boolean getBatteryLed() {
        if (Utils.existFile(BATTERY_LED))
            return Utils.readFile(BATTERY_LED).contains("[battery-full]");
        return false;
    }

    public static void setBatteryLed(boolean active, Context context) {
        Control.runCommand(active ? "battery-full" :
            "none", BATTERY_LED, Control.CommandType.GENERIC, context);
        Control.setProp(BATTERY_LED_PROP, active ? "1" : "0", context);
    }

    public static boolean hasBatteryLed() {
        if (Utils.existFile(BATTERY_LED))
            return Utils.readFile(BATTERY_LED).contains("battery-full");
        return false;
    }


    public static void activateBcl(boolean active, Context context) {
        if (!active && Battery.hasBclHotplug() && Battery.isBclHotplugActive()) {
            Battery.activateBclHotplug(false, context);
        }
        Control.runCommand(active ? "enabled" : "disabled", BCL, Control.CommandType.ASTERISK, context);
    }

    public static boolean isBclActive() {
        return Utils.readFile(BCL).equals("enabled");
    }

    public static boolean hasBcl() {
        return Utils.existFile(BCL);
    }

    public static void activateBclHotplug(boolean active, Context context) {
        if (active && Battery.hasBcl() && !Battery.isBclActive()) {
            Battery.activateBcl(true, context);
        }
        Control.runCommand(active ? "Y" : "N", BCL_HOTPLUG, Control.CommandType.ASTERISK, context);
    }

    public static boolean isBclHotplugActive() {
        return Utils.readFile(BCL_HOTPLUG).equals("Y");
    }

    public static boolean hasBclHotplug() {
        return Utils.existFile(BCL_HOTPLUG);
    }

    public static boolean hasBclFreq() {
        if (Utils.existFile(BCL_FREQ_MAX) && Utils.existFile(BCL_FREQ_LIMIT)) return true;
        return false;
    }

    public static int getBclLimitFreq() {
        return Utils.stringToInt(Utils.readFile(BCL_FREQ_LIMIT));
    }

    public static int getBclFreq() {
        return Utils.stringToInt(Utils.readFile(BCL_FREQ_MAX));
    }

    public static void setBclFreq(int value, Context context) {
        Control.runCommand(String.valueOf(value), BCL_FREQ_MAX, Control.CommandType.ASTERISK, context);
    }

    public static boolean hasBclVphLow() {
        if (Utils.existFile(BCL_VPH_LOW)) return true;
        return false;
    }

    public static int getBclVphLow() {
        return Utils.stringToInt(Utils.readFile(BCL_VPH_LOW));
    }

    public static void setBclVphLow(int value, Context context) {
        Control.runCommand(String.valueOf(value), BCL_VPH_LOW, Control.CommandType.ASTERISK, context);
    }

    public static boolean hasBclVphHigh() {
        if (Utils.existFile(BCL_VPH_HIGH)) return true;
        return false;
    }

    public static int getBclVphHigh() {
        return Utils.stringToInt(Utils.readFile(BCL_VPH_HIGH));
    }

    public static void setBclVphHigh(int value, Context context) {
        Control.runCommand(String.valueOf(value), BCL_VPH_HIGH, Control.CommandType.ASTERISK, context);
    }

    public static int getBatteryVoltageNow() {
        return Utils.stringToInt(Utils.readFile(BATTERY_VOLTAGE_NOW));
    }

    public static int getBatteryTemp() {
        return Utils.stringToInt(Utils.readFile(BATTERY_TEMP));
    }

    public static int getBatteryLevel() {
        return Utils.stringToInt(Utils.readFile(BATTERY_LEVEL));
    }

    public static boolean hasBclHotMask() {
        if (Utils.existFile(BCL_HOT_MASK)) return true;
        return false;
    }

    public static int getBclHotMask() {
        int read_position = Utils.stringToInt(Utils.readFile(BCL_HOT_MASK));
        if (read_position == 8) return 1;
        else if (read_position == 10) return 2;
        else if (read_position == 12) return 3;
        else if (read_position == 14) return 4;
        return 0;
    }

    public static void setBclHotMask(int value, Context context) {
        String position = "0";
        if (value == 1) position = "8";
        if (value == 2) position = "10";
        if (value == 3) position = "12";
        if (value == 4) position = "14";
        Control.runCommand(position, BCL_HOT_MASK, Control.CommandType.ASTERISK, context);
    }
}
