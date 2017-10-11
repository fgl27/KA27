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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;
import com.kerneladiutor.library.root.RootUtils;

/**
 * Created by willi on 27.12.14.
 */
public class VM implements Constants {

    private static String[] mAvail_Comp_Algo;

    public static boolean hasZRAMDataSize() {
        return Utils.existFile(ZRAM_ORG_DATA_SIZE) && Utils.existFile(ZRAM_COMP_DATA_SIZE);
    }

    public static int getZramOrigDataSize() {
        return Utils.stringToInt(Utils.readFile(ZRAM_ORG_DATA_SIZE));
    }

    public static int getZramCompDataSize() {
        return Utils.stringToInt(Utils.readFile(ZRAM_COMP_DATA_SIZE));
    }

    public static boolean hasZRAMReadWrites() {
        return Utils.existFile(ZRAM_READ) && Utils.existFile(ZRAM_WRITES);
    }

    public static boolean hasZRAMFailReadWrites() {
        return Utils.existFile(ZRAM_FAIL_READ) && Utils.existFile(ZRAM_FAIL_WRITES);
    }

    public static String getZramReadWrites() {
        return " " + Utils.readFile(ZRAM_READ) + " | " + Utils.readFile(ZRAM_WRITES);
    }

    public static String getZramFailReadWrites() {
        return " " + Utils.readFile(ZRAM_FAIL_READ) + " | " + Utils.readFile(ZRAM_FAIL_WRITES);
    }

    public static boolean hasZRAMReadOnly() {
        return RootUtils.runCommand("cat proc/swaps").contains("dev/block/zram0");
    }

    public static String getFreeSwap(Context context) {
        return RootUtils.runCommand(context.getFilesDir().getPath() + "/busybox free | grep Swap | head -1");
    }

    public static void setZRAM(String algo, String disksize, String max_comp, Context context) {
        if (max_comp == null && hasZRAMMaxCompStreams()) {
            max_comp = String.valueOf(getZRAMMaxCompStreams());
        }
        if (algo == null && hasZRAMCompAlgos()) {
            algo = getZRAMCompAlgo();
        }
        if (disksize == null) {
            disksize = String.valueOf(getZRAMDisksize());
        }

        Control.runCommand("swapoff " + ZRAM_BLOCK + " > /dev/null 2>&1", ZRAM_BLOCK, Control.CommandType.CUSTOM, "swapoff", context);
        Control.runCommand("1", ZRAM_RESET, Control.CommandType.GENERIC, context);
        Control.runCommand("0", ZRAM_DISKSIZE, Control.CommandType.GENERIC, context);
        if (algo != null) {
            Control.runCommand(algo, ZRAM_COMP_ALGO, Control.CommandType.GENERIC, context);
        }
        if (max_comp != null) {
            Control.runCommand(max_comp, ZRAM_MAX_COMP_STREAMS, Control.CommandType.GENERIC, context);
        }
        Control.runCommand(disksize + "M", ZRAM_DISKSIZE, Control.CommandType.GENERIC, context);
        Control.runCommand("mkswap " + ZRAM_BLOCK + " > /dev/null 2>&1", ZRAM_BLOCK,
            Control.CommandType.CUSTOM, "mkswap", context);
        Control.runCommand("swapon " + ZRAM_BLOCK + " > /dev/null 2>&1", ZRAM_BLOCK,
            Control.CommandType.CUSTOM, "swapon", context);
    }

    public static List < String > getZRAMCompAlgos() {
        if (Utils.existFile(ZRAM_COMP_ALGO)) {
            String values = Utils.readFile(ZRAM_COMP_ALGO);
            if (values != null) {
                String[] valueArray = values.split(" ");
                String[] out = new String[valueArray.length];

                for (int i = 0; i < valueArray.length; i++)
                    out[i] = valueArray[i].replace("[", "").replace("]", "");
                Collections.sort(Arrays.asList(out), String.CASE_INSENSITIVE_ORDER);
                return new ArrayList < > (Arrays.asList(out));
            }
        }
        return null;
    }

    public static String getZRAMCompAlgo() {
        if (Utils.existFile(ZRAM_COMP_ALGO)) {
            String values = Utils.readFile(ZRAM_COMP_ALGO);
            if (values != null) {
                String[] valueArray = values.split(" ");

                for (String value: valueArray)
                    if (value.contains("["))
                        return value.replace("[", "").replace("]", "");
            }
        }
        return "";
    }

    public static boolean hasZRAMCompAlgos() {
        return Utils.existFile(ZRAM_COMP_ALGO);
    }

    public static int getZRAMMaxCompStreams() {
        return Utils.stringToInt(Utils.readFile(ZRAM_MAX_COMP_STREAMS));
    }

    public static boolean hasZRAMMaxCompStreams() {
        return Utils.existFile(ZRAM_MAX_COMP_STREAMS);
    }

    public static int getZRAMDisksize() {
        return Utils.stringToInt(Utils.readFile(ZRAM_DISKSIZE)) / 1024 / 1024;
    }

    public static boolean hasZRAM() {
        return Utils.existFile(ZRAM);
    }

    public static boolean hasDirtyRatio() {
        if (Utils.existFile(VM_DIRTY_RATIO)) return true;
        return false;
    }

    public static void setDirtyRatio(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_DIRTY_RATIO, Control.CommandType.GENERIC, context);
    }

    public static int getDirtyRatio() {
        return Utils.stringToInt(Utils.readFile(VM_DIRTY_RATIO));
    }

    public static boolean hasDirtyBackgroundRatio() {
        if (Utils.existFile(VM_DIRTY_BACKGROUND_RATIO)) return true;
        return false;
    }

    public static void setDirtyBackgroundRatio(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_DIRTY_BACKGROUND_RATIO, Control.CommandType.GENERIC, context);
    }

    public static int getDirtyBackgroundRatio() {
        return Utils.stringToInt(Utils.readFile(VM_DIRTY_BACKGROUND_RATIO));
    }

    public static boolean hasDirtyExpire() {
        if (Utils.existFile(VM_DIRTY_EXPIRE_CENTISECS)) return true;
        return false;
    }

    public static void setDirtyExpire(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_DIRTY_EXPIRE_CENTISECS, Control.CommandType.GENERIC, context);
    }

    public static int getDirtyExpire() {
        return Utils.stringToInt(Utils.readFile(VM_DIRTY_EXPIRE_CENTISECS));
    }

    public static boolean hasDirtyWriteback() {
        if (Utils.existFile(VM_DIRTY_WRITEBACK_CENTISECS)) return true;
        return false;
    }

    public static void setDirtyWriteback(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_DIRTY_WRITEBACK_CENTISECS, Control.CommandType.GENERIC, context);
    }

    public static int getDirtyWriteback() {
        return Utils.stringToInt(Utils.readFile(VM_DIRTY_WRITEBACK_CENTISECS));
    }

    public static boolean hasOverCommitRatio() {
        if (Utils.existFile(VM_DIRTY_RATIO)) return true;
        return false;
    }

    public static void setOverCommitRatio(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_OVERCOMMIT_RATIO, Control.CommandType.GENERIC, context);
    }

    public static int getOverCommitRatio() {
        return Utils.stringToInt(Utils.readFile(VM_OVERCOMMIT_RATIO));
    }

    public static boolean hasSwappiness() {
        if (Utils.existFile(VM_SWAPPINESS)) return true;
        return false;
    }

    public static void setSwappiness(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_SWAPPINESS, Control.CommandType.GENERIC, context);
    }

    public static int getSwappiness() {
        return Utils.stringToInt(Utils.readFile(VM_SWAPPINESS));
    }

    public static boolean hasVFSCachePressure() {
        if (Utils.existFile(VM_VFS_CACHE_PRESSURE)) return true;
        return false;
    }

    public static void setVFSCachePressure(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_VFS_CACHE_PRESSURE, Control.CommandType.GENERIC, context);
    }

    public static int getVFSCachePressure() {
        return Utils.stringToInt(Utils.readFile(VM_VFS_CACHE_PRESSURE));
    }

    public static boolean hasLaptopMode() {
        if (Utils.existFile(VM_LAPTOP_MODE)) return true;
        return false;
    }

    public static void activateLaptopMode(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", VM_LAPTOP_MODE, Control.CommandType.GENERIC, context);
    }

    public static boolean isLaptopModeActive() {
        return Utils.readFile(VM_LAPTOP_MODE).equals("1");
    }

    public static boolean hasMinFreeKbytes() {
        if (Utils.existFile(VM_MIN_FREE_KBYTES)) return true;
        return false;
    }

    public static void setMinFreeKbytes(String value, Context context) {
        Control.runCommand(value, VM_MIN_FREE_KBYTES, Control.CommandType.GENERIC, context);
    }

    public static String getMinFreeKbytes() {
        String value = Utils.readFile(VM_MIN_FREE_KBYTES);
        if (value != null) return value;
        return null;
    }

    public static boolean hasExtraFreeKbytes() {
        if (Utils.existFile(VM_EXTRA_FREE_KBYTES)) return true;
        return false;
    }

    public static void setExtraFreeKbytes(String value, Context context) {
        Control.runCommand(value, VM_EXTRA_FREE_KBYTES, Control.CommandType.GENERIC, context);
    }

    public static String getExtraFreeKbytes() {
        String value = Utils.readFile(VM_EXTRA_FREE_KBYTES);
        if (value != null) return value;
        return null;
    }

    public static boolean hasDynamicDirtyWriteback() {
        if (Utils.existFile(VM_DYNAMIC_DIRTY_WRITEBACK)) return true;
        return false;
    }

    public static void activateDynamicDirtyWriteback(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", VM_DYNAMIC_DIRTY_WRITEBACK, Control.CommandType.GENERIC, context);
    }

    public static boolean isDynamicDirtyWritebackActive() {
        return Utils.readFile(VM_DYNAMIC_DIRTY_WRITEBACK).equals("1");
    }

    public static boolean hasDirtySuspendWriteback() {
        if (Utils.existFile(VM_DIRTY_WRITEBACK_SUSPEND_CENTISECS)) return true;
        return false;
    }

    public static void setDirtySuspendWriteback(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_DIRTY_WRITEBACK_SUSPEND_CENTISECS, Control.CommandType.GENERIC, context);
    }

    public static int getDirtySuspendWriteback() {
        return Utils.stringToInt(Utils.readFile(VM_DIRTY_WRITEBACK_SUSPEND_CENTISECS));
    }

    public static boolean hasDirtyActiveWriteback() {
        if (Utils.existFile(VM_DIRTY_WRITEBACK_ACTIVE_CENTISECS)) return true;
        return false;
    }

    public static void setDirtyActiveWriteback(int value, Context context) {
        Control.runCommand(String.valueOf(value), VM_DIRTY_WRITEBACK_ACTIVE_CENTISECS, Control.CommandType.GENERIC, context);
    }

    public static int getDirtyActiveWriteback() {
        return Utils.stringToInt(Utils.readFile(VM_DIRTY_WRITEBACK_ACTIVE_CENTISECS));
    }

    public static boolean hasProcessReclaim() {
        if (Utils.existFile(PROCESS_RECLAIM_ENABLE)) return true;
        return false;
    }

    public static void activateProcessReclaim(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", PROCESS_RECLAIM_ENABLE, Control.CommandType.GENERIC, context);
    }

    public static boolean isProcessReclaimActive() {
        return Utils.readFile(PROCESS_RECLAIM_ENABLE).equals("1");
    }

    // short things here PR = Process Reclaim
    public static boolean hasPRPressure() {
        if (Utils.existFile(PROCESS_RECLAIM_PRESSURE)) return true;
        return false;
    }

    public static int getPRPressure() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_PRESSURE));
    }

    public static boolean hasPRAvgEff() {
        if (Utils.existFile(PROCESS_RECLAIM_AVG_EFF)) return true;
        return false;
    }

    public static int getPRAvgEff() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_AVG_EFF));
    }

    public static boolean hasPRSwapWin() {
        if (Utils.existFile(PROCESS_RECLAIM_SWAP_WIN)) return true;
        return false;
    }

    public static int getPRSwapWin() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_SWAP_WIN));
    }

    public static void setPRSwapWin(int value, Context context) {
        Control.runCommand(String.valueOf(value), PROCESS_RECLAIM_SWAP_WIN, Control.CommandType.GENERIC, context);
    }

    public static boolean hasPRSwapOptEff() {
        if (Utils.existFile(PROCESS_RECLAIM_SWAP_OPT_EFF)) return true;
        return false;
    }

    public static int getPRSwapOptEff() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_SWAP_OPT_EFF));
    }

    public static void setPRSwapOptEff(int value, Context context) {
        Control.runCommand(String.valueOf(value), PROCESS_RECLAIM_SWAP_OPT_EFF, Control.CommandType.GENERIC, context);
    }

    public static boolean hasPRPressureMax() {
        if (Utils.existFile(PROCESS_RECLAIM_PRESSURE_MAX)) return true;
        return false;
    }

    public static int getPRPressureMax() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_PRESSURE_MAX));
    }

    public static void setPRPressureMax(int value, Context context) {
        Control.runCommand(String.valueOf(value), PROCESS_RECLAIM_PRESSURE_MAX, Control.CommandType.GENERIC, context);
    }

    public static boolean hasPRPressureMin() {
        if (Utils.existFile(PROCESS_RECLAIM_PRESSURE_MIN)) return true;
        return false;
    }

    public static int getPRPressureMin() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_PRESSURE_MIN));
    }

    public static void setPRPressureMin(int value, Context context) {
        Control.runCommand(String.valueOf(value), PROCESS_RECLAIM_PRESSURE_MIN, Control.CommandType.GENERIC, context);
    }

    public static boolean hasPRPerSwapSize() {
        if (Utils.existFile(PROCESS_RECLAIM_PER_SWAP_SIZE)) return true;
        return false;
    }

    public static int getPRPerSwapSize() {
        return Utils.stringToInt(Utils.readFile(PROCESS_RECLAIM_PER_SWAP_SIZE));
    }

    public static void setPRPerSwapSize(int value, Context context) {
        Control.runCommand(String.valueOf(value), PROCESS_RECLAIM_PER_SWAP_SIZE, Control.CommandType.GENERIC, context);
    }

}
