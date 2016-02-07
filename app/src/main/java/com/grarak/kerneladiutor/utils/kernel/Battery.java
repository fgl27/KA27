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

    public static void activateC0State (boolean active, Context context) {
        String path = C0STATE;
        Control.runCommand(active ? "1" : "0", path, Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "1"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "2"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "3"), Control.CommandType.GENERIC, context);
    }


    public static boolean isC0StateActive() {
        return Utils.readFile(C0STATE).equals("1");
    }

    public static boolean hasC0State () {
        return Utils.existFile(C0STATE);
    }

    public static void activateC1State (boolean active, Context context) {
        String path = C1STATE;
        Control.runCommand(active ? "1" : "0", path, Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "1"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "2"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "3"), Control.CommandType.GENERIC, context);
    }


    public static boolean isC1StateActive() {
        return Utils.readFile(C1STATE).equals("1");
    }

    public static boolean hasC1State () {
        return Utils.existFile(C1STATE);
    }

    public static void activateC2State (boolean active, Context context) {
        String path = C2STATE;
        Control.runCommand(active ? "1" : "0", path, Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "1"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "2"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "3"), Control.CommandType.GENERIC, context);
    }


    public static boolean isC2StateActive() {
        return Utils.readFile(C2STATE).equals("1");
    }

    public static boolean hasC2State () {
        return Utils.existFile(C2STATE);
    }

    public static void activateC3State (boolean active, Context context) {
        String path = C3STATE;
        Control.runCommand(active ? "1" : "0", path, Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "1"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "2"), Control.CommandType.GENERIC, context);
        Control.runCommand(active ? "1" : "0", path.replace("0", "3"), Control.CommandType.GENERIC, context);
    }

    public static boolean isC3StateActive() {
        return Utils.readFile(C3STATE).equals("1");
    }

    public static boolean hasC3State () {
        return Utils.existFile(C3STATE);
    }

}
