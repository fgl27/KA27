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

package com.kerneladiutor.library.root;

import android.util.Log;

import com.kerneladiutor.library.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by willi on 14.12.14.
 */

/**
 * Here you have different functions which will help you with root commands.
 * I think they are self explained and do no need any further descriptions.
 */
public class RootUtils {

    private static SU su;

    public static boolean rooted() {
        return existBinary("su");
    }

    public static boolean rootAccess() {
        SU su = getSU();
        su.runCommand("echo /testRoot/");
        return !su.denied;
    }

    public static boolean hasAppletSupport() {
       if (busyboxInstalled() || toyboxInstalled())
            return true;
        return false;
    }

    public static boolean busyboxInstalled() {
        return existBinary("busybox");
    }

    public static boolean toyboxInstalled() {
        return existBinary("toybox");
    }

    public static boolean stockrom() {
        String rom = RootUtils.runCommand("getprop | grep -i ro.build.display.id");
        if (rom.contains("MPG24.107"))
            return false;
        return true;
    }

    private static boolean existBinary(String binary) {
        for (String path : System.getenv("PATH").split(":")) {
            if (!path.endsWith("/")) path += "/";
            if (new File(path + binary).exists() || Tools.existFile(path + binary, true))
                return true;
        }
        return false;
    }

    public static String getKernelVersion() {
        return runCommand("uname -r");
    }

    public static void mount(boolean writeable, String mountpoint) {
        runCommand(writeable ? "mount -o remount,rw " + mountpoint + " " + mountpoint :
                "mount -o remount,ro " + mountpoint + " " + mountpoint);
        runCommand(writeable ? "mount -o remount,rw " + mountpoint :
                "mount -o remount,ro " + mountpoint);
    }

    public static void closeSU() {
        if (su != null) su.close();
        su = null;
    }

    public static String runCommand(String command) {
        return getSU().runCommand(command);
    }

    private static SU getSU() {
        if (su == null || su.closed || su.denied) {
            if (su != null && !su.closed) {
                su.close();
            }
            su = new SU();
        }
        return su;
    }

    /*
     * Based on AndreiLux's SU code in Synapse
     * https://github.com/AndreiLux/Synapse/blob/master/src/main/java/com/af/synapse/utils/Utils.java#L238
     */
    public static class SU {

        private Process mProcess;
        private BufferedWriter mWriter;
        private BufferedReader mReader;
        private final boolean mRoot;
        private final String mTag;
        private boolean closed;
        private boolean denied;
        private boolean firstTry;

        public SU() {
            this(true, null);
        }

        public SU(boolean root, String tag) {
            mRoot = root;
            mTag = tag;
            try {
                if (mTag != null) {
                    Log.i(mTag, String.format("%s initialized", root ? "SU" : "SH"));
                }
                firstTry = true;
                mProcess = Runtime.getRuntime().exec(root ? "su" : "sh");
                mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            } catch (IOException e) {
                if (mTag != null) {
                    Log.e(mTag, root ? "Failed to run shell as su" : "Failed to run shell as sh");
                }
                denied = true;
                closed = true;
            }
        }

        public synchronized String runCommand(final String command) {
            synchronized (this) {
                try {
                    StringBuilder sb = new StringBuilder();
                    String callback = "/shellCallback/";
                    mWriter.write(command + "\necho " + callback + "\n");
                    mWriter.flush();

                    int i;
                    char[] buffer = new char[256];
                    while (true) {
                        sb.append(buffer, 0, mReader.read(buffer));
                        if ((i = sb.indexOf(callback)) > -1) {
                            sb.delete(i, i + callback.length());
                            break;
                        }
                    }
                    firstTry = false;
                    if (mTag != null) {
                        Log.i(mTag, "run: " + command + " output: " + sb.toString().trim());
                    }
                    return sb.toString().trim();
                } catch (IOException e) {
                    closed = true;
                    e.printStackTrace();
                    if (firstTry) denied = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    denied = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    denied = true;
                }
                return null;
            }
        }

        public void close() {
            try {
                if (mWriter != null) {
                    mWriter.write("exit\n");
                    mWriter.flush();

                    mWriter.close();
                }
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mProcess.destroy();
            if (mTag != null) {
                Log.i(mTag, String.format("%s closed: %d", mRoot ? "SU" : "SH", mProcess.exitValue()));
            }
            closed = true;
        }

    }

}
