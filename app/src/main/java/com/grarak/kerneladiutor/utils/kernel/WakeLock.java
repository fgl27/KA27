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
import android.util.Log;

import java.lang.Math;
import java.util.Arrays;

import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;
import com.grarak.kerneladiutor.utils.root.RootUtils;

/**
 * Created by willi on 27.12.14.
 */
public class WakeLock implements Constants {

    private static String SMB135X_WAKELOCK_FILE;
    private static String WLAN_RX_WAKELOCK_FILE;
    private static String WLAN_CTRL_WAKELOCK_FILE;
    private static String WLAN_WAKELOCK_FILE;

    public static boolean hasAnyWakelocks() {
        for (int i = 0; i < WAKELOCK_ARRAY.length; i++) {
            for (int x = 0; x < WAKELOCK_ARRAY[i].length; x++) {
                if (Utils.existFile(WAKELOCK_ARRAY[i][x])) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void setMsmHsicWakelockDivider(int value, Context context) {
        String command = String.valueOf(value);
        Control.runCommand(command, MSM_HSIC_WAKELOCK_DIVIDER, Control.CommandType.GENERIC, context);
        // Delay 100ms to allow Control.runCommand chain to run
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (value != (getMsmHsicWakelockDivider())) {
            Log.i(TAG, "Divider: " + (getMsmHsicWakelockDivider()));
            Utils.toast("Sorry, your kernel does not support this value. Please choose another.", context);
        }
    }

    public static int getMsmHsicWakelockDivider() {
        int value = Utils.stringToInt(Utils.readFile(MSM_HSIC_WAKELOCK_DIVIDER));
        return value;
    }

    public static boolean hasMsmHsicWakelockDivider() {
        return Utils.existFile(MSM_HSIC_WAKELOCK_DIVIDER);
    }

    public static void setWlanrxWakelockDivider(int value, Context context) {
        String command = String.valueOf(value + 1);
        if (value == 15) command = "0";
        Control.runCommand(command, WLAN_RX_WAKELOCK_DIVIDER, Control.CommandType.GENERIC, context);
    }

    public static int getWlanrxWakelockDivider() {
        int value = Utils.stringToInt(Utils.readFile(WLAN_RX_WAKELOCK_DIVIDER));
        if (value == 0) value = 16;
        return value - 1;
    }

    public static boolean hasWlanrxWakelockDivider() {
        return Utils.existFile(WLAN_RX_WAKELOCK_DIVIDER);
    }

    public static void setBCMDHDWakelockDivider(int value, Context context) {
        String command = String.valueOf(value + 1);
        Control.runCommand(command, BCMDHD_WAKELOCK_DIVIDER, Control.CommandType.GENERIC, context);
    }

    public static int getBCMDHDWakelockDivider() {
        int value = Utils.stringToInt(Utils.readFile(BCMDHD_WAKELOCK_DIVIDER));
        return value - 1;
    }

    public static boolean hasBCMDHDWakelockDivider() {
        return Utils.existFile(BCMDHD_WAKELOCK_DIVIDER);
    }


    public static void activateWlanWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", WLAN_WAKELOCK_FILE, Control.CommandType.GENERIC, context);
    }

    public static boolean isWlanWakeLockActive() {
        return Utils.readFile(WLAN_WAKELOCK_FILE).equals("Y");
    }

    public static boolean hasWlanWakeLock() {
        for (String file: WLAN_WAKELOCKS)
            if (Utils.existFile(file)) {
                WLAN_WAKELOCK_FILE = file;
                return true;
            }
        return false;
    }

    public static void activateWlanctrlWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", WLAN_CTRL_WAKELOCK_FILE, Control.CommandType.GENERIC, context);
    }

    public static boolean isWlanctrlWakeLockActive() {
        return Utils.readFile(WLAN_CTRL_WAKELOCK_FILE).equals("Y");
    }

    public static boolean hasWlanctrlWakeLock() {
        for (String file: WLAN_CTRL_WAKELOCKS)
            if (Utils.existFile(file)) {
                WLAN_CTRL_WAKELOCK_FILE = file;
                return true;
            }
        return false;
    }

    public static void activateWlanrxWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", WLAN_RX_WAKELOCK_FILE, Control.CommandType.GENERIC, context);
    }

    public static boolean isWlanrxWakeLockActive() {
        return Utils.readFile(WLAN_RX_WAKELOCK_FILE).equals("Y");
    }

    public static boolean hasWlanrxWakeLock() {
        for (String file: WLAN_RX_WAKELOCKS)
            if (Utils.existFile(file)) {
                WLAN_RX_WAKELOCK_FILE = file;
                return true;
            }
        return false;
    }

    public static void activateMsmHsicHostWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", MSM_HSIC_HOST_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static boolean isMsmHsicHostWakeLockActive() {
        return Utils.readFile(MSM_HSIC_HOST_WAKELOCK).equals("Y");
    }

    public static boolean hasMsmHsicHostWakeLock() {
        return Utils.existFile(MSM_HSIC_HOST_WAKELOCK);
    }

    public static void activateBlueSleepWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", BLUESLEEP_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static boolean isBlueSleepWakeLockActive() {
        return Utils.readFile(BLUESLEEP_WAKELOCK).equals("Y");
    }

    public static boolean hasBlueSleepWakeLock() {
        return Utils.existFile(BLUESLEEP_WAKELOCK);
    }

    public static void activateBlueDroidTimeWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", BLUEDROID_TIMER_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static boolean isBlueDroidTimeWakeLockActive() {
        return Utils.readFile(BLUEDROID_TIMER_WAKELOCK).equals("Y");
    }

    public static boolean hasBlueDroidTimeWakeLock() {
        return Utils.existFile(BLUEDROID_TIMER_WAKELOCK);
    }


    public static void activateSensorIndWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", SENSOR_IND_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static boolean isSensorIndWakeLockActive() {
        return Utils.readFile(SENSOR_IND_WAKELOCK).equals("Y");
    }

    public static boolean hasSensorIndWakeLock() {
        return Utils.existFile(SENSOR_IND_WAKELOCK);
    }

    public static void activateSmb135xWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", SMB135X_WAKELOCK_FILE, Control.CommandType.GENERIC, context);
    }

    public static boolean isSmb135xWakeLockActive() {
        return Utils.readFile(SMB135X_WAKELOCK_FILE).equals("Y");
    }

    public static boolean hasSmb135xWakeLock() {
        for (String file: SMB135X_WAKELOCKS)
            if (Utils.existFile(file)) {
                SMB135X_WAKELOCK_FILE = file;
                return true;
            }
        return false;
    }

    public static void activateTimerFdWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", TIMERFD_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static boolean isTimerFdWakeLockActive() {
        return Utils.readFile(TIMERFD_WAKELOCK).equals("Y");
    }

    public static boolean hasTimerFdWakeLock() {
        return Utils.existFile(TIMERFD_WAKELOCK);
    }

    public static void activateNetlinkWakeLock(boolean active, Context context) {
        Control.runCommand(active ? "Y" : "N", NETLINK_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static boolean isNetlinkWakeLockActive() {
        return Utils.readFile(NETLINK_WAKELOCK).equals("Y");
    }

    public static boolean hasNetlinkWakeLock() {
        return Utils.existFile(NETLINK_WAKELOCK);
    }

    public static void setTestWakeLock(String value, Context context) {
        Control.runCommand(value, TEST_WAKELOCK, Control.CommandType.GENERIC, context);
    }

    public static String getTestWakeLock() {
        return Utils.readFile(TEST_WAKELOCK);
    }

    public static boolean hasTestWakeLock() {
        return Utils.existFile(TEST_WAKELOCK);
    }

    public static boolean getWakeLocksCount() {
        String[] wakes_temp;
        String pre_wakes = RootUtils.runCommand("cat sys/kernel/debug/wakeup_sources|tail -n +2");
        String[] wakes = pre_wakes != null ? pre_wakes.split("\\r?\\n") : null;

        if (wakes != null && wakes.length > 1) {
            for (int i = 0; i < wakes.length; ++i) {
                if (!wakes[i].contains("event") && !wakes[i].contains("KeyEvents") && !wakes[i].contains("Service Cal")) {
                    wakes_temp = wakes[i].split("\\s+");
                    if ((Utils.stringToInt(wakes_temp[1]) > 0) && (Utils.stringToInt(wakes_temp[7]) > 1000))
                        return true;
                }
            }
        }

        return false;
    }

    public static String getWakeLocks(boolean time) {
        String result = "", tempSort = "";
        int offset = 0, temp = 0, space_length = 0, space_length_temp = 0;
        if (time) offset = 1;
        String[] wakes_temp;

        String pre_wakes = RootUtils.runCommand("cat sys/kernel/debug/wakeup_sources|tail -n +2");
        String[] wakes = pre_wakes != null ? pre_wakes.split("\\r?\\n") : null;

        if (wakes != null && wakes.length > 1) {

            //sort add number of duplicated for it result and remove duplicated result
            Arrays.sort(wakes);
            for (int i = 0; i < wakes.length; ++i) {
                if (!wakes[i].contains("event") && !wakes[i].contains("KeyEvents") && !wakes[i].contains("Service Cal")) {
                    wakes_temp = wakes[i].split("\\s+");
                    temp = Utils.stringToInt(wakes_temp[1]);
                    if ((temp > 0) && (Utils.stringToInt(wakes_temp[7]) > 1000)) {
                        space_length_temp = (int)(Math.log10(temp) + 1);
                        if (space_length_temp > space_length)
                            space_length = space_length_temp;
                        result += wakes_temp[1] + " " + wakes_temp[6] + " " + wakes_temp[0] + "\n";
                    }
                }
            }

            //sort bigger to lower
            wakes = result.split("\\r?\\n");
            for (int a = 0; a < wakes.length; a++) {
                for (int b = 0; b < wakes.length; b++) {
                    if ((b + 1) != wakes.length) {
                        if (Utils.stringToInt(wakes[b].split(" ")[0 + offset]) < Utils.stringToInt(wakes[b + 1].split(" ")[0 + offset])) {
                            tempSort = wakes[b];
                            wakes[b] = wakes[b + 1];
                            wakes[b + 1] = tempSort;
                        }
                    }
                }
            }

            //Make a \n from array on to a string formating the time
            result = "";
            for (int i = 0; i < wakes.length; i++) {
                wakes_temp = wakes[i].split(" ");
                if (time)
                    result += Utils.timeMs(Utils.stringToInt(wakes_temp[1])) + "|" + center(space_length, wakes_temp[0]) +
                    "|" + wakes_temp[2] + "\n";
                else
                    result += center(space_length, wakes_temp[0]) + "|" + Utils.timeMs(Utils.stringToInt(wakes_temp[1])) +
                    "|" + wakes_temp[2] + "\n";
            }
        }
        return result;
    }

    // center text but only works well with TextView.setTypeface(Typeface.MONOSPACE); maybe others font too
    public static String center(int len, String text) {
        if (len <= text.length())
            return text.substring(0, len);
        int before = (len - text.length()) / 2;
        if (before == 0)
            return String.format("%-" + len + "s", text);
        int rest = len - before;
        return String.format("%-" + (rest + before) + "s", text); // align left
        //return String.format("%" + (before) + "s%-" + (rest) + "s", "", text);// align center
    }

}