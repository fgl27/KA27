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

package com.grarak.kerneladiutor.utils;

import android.annotation.TargetApi;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.fragments.kernel.BatteryFragment;
import com.grarak.kerneladiutor.fragments.kernel.CPUFragment;
import com.grarak.kerneladiutor.fragments.kernel.CPUHotplugFragment;
import com.grarak.kerneladiutor.fragments.kernel.CPUVoltageFragment;
import com.grarak.kerneladiutor.fragments.kernel.EntropyFragment;
import com.grarak.kerneladiutor.fragments.kernel.GPUFragment;
import com.grarak.kerneladiutor.fragments.kernel.IOFragment;
import com.grarak.kerneladiutor.fragments.kernel.KSMFragment;
import com.grarak.kerneladiutor.fragments.kernel.LMKFragment;
import com.grarak.kerneladiutor.fragments.kernel.MiscFragment;
import com.grarak.kerneladiutor.fragments.kernel.RamFragment;
import com.grarak.kerneladiutor.fragments.kernel.ScreenFragment;
import com.grarak.kerneladiutor.fragments.kernel.SoundFragment;
import com.grarak.kerneladiutor.fragments.kernel.ThermalFragment;
import com.grarak.kerneladiutor.fragments.kernel.VMFragment;
import com.grarak.kerneladiutor.fragments.kernel.WakeFragment;
import com.grarak.kerneladiutor.fragments.kernel.WakeLockFragment;
import com.grarak.kerneladiutor.utils.kernel.CPU;
import com.kerneladiutor.library.Tools;
import com.kerneladiutor.library.root.RootUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by willi on 30.11.14.
 */
public class Utils implements Constants {

    public static boolean DARKTHEME = false;

    public static String MbKb(int value, Context context) {
        String converted = "";
        if (value < 1024) converted = value + context.getString(R.string.kb);
        else converted = (Math.round((float)value / 1024L)) + context.getString(R.string.mb);
        return converted;
    }

    public static String percentage(int total, int tocheck, Context context) {
        float value;
        if (tocheck > 0) value = (float)((tocheck * 100.0f) / total);
        else value = 0;
        return String.format(Locale.US, "%.2f", value) + context.getString(R.string.percent);
    }

    public static boolean isAppInstalled(String packagename, Context context) {
        try {
            context.getPackageManager().getApplicationInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void openAppInStore(String packagename, Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packagename)));
        } catch (ActivityNotFoundException ignored) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packagename)));
        }
    }

    // MD5 code from
    // https://github.com/CyanogenMod/android_packages_apps_CMUpdater/blob/cm-12.1/src/com/cyanogenmod/updater/utils/MD5.java
    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        Log.v(TAG, "Calculated digest: " + calculatedDigest);
        Log.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public static boolean isRTL(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static Bitmap scaleDownBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = width;
        int newHeight = height;

        if (maxWidth != 0 && newWidth > maxWidth) {
            newHeight = Math.round((float) maxWidth / newWidth * newHeight);
            newWidth = maxWidth;
        }

        if (maxHeight != 0 && newHeight > maxHeight) {
            newWidth = Math.round((float) maxHeight / newHeight * newWidth);
            newHeight = maxHeight;
        }

        return width != newWidth || height != newHeight ? resizeBitmap(bitmap, newWidth, newHeight) : bitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    public static String getDeviceName() {
        return Build.DEVICE;
    }

    public static String getVendorName() {
        return Build.MANUFACTURER;
    }

    public static String decodeString(String text) {
        try {
            return new String(Base64.decode(text, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeString(String text) {
        try {
            return Base64.encodeToString(text.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void errorDialog(Context context, Exception e) {
        new AlertDialog.Builder(context).setMessage(e.getMessage()).setNeutralButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    public static void circleAnimate(final View view, int cx, int cy) {
        if (view == null) return;
        try {
            view.setVisibility(View.INVISIBLE);

            int finalRadius = Math.max(view.getWidth(), view.getHeight());
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            anim.setDuration(500);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    view.setVisibility(View.VISIBLE);
                }
            });
            anim.start();
        } catch (IllegalStateException e) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void confirmDialog(String title, String message, DialogInterface.OnClickListener onClickListener,
                                     Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setPositiveButton(context.getString(R.string.ok), onClickListener).show();
    }

    public static String readAssetFile(Context context, String file) {
        InputStream input = null;
        BufferedReader buf = null;
        try {
            StringBuilder s = new StringBuilder();
            input = context.getAssets().open(file);
            buf = new BufferedReader(new InputStreamReader(input));

            String str;
            while ((str = buf.readLine()) != null) s.append(str).append("\n");
            return s.toString().trim();
        } catch (IOException e) {
            Log.e(TAG, "Unable to read " + file);
        } finally {
            try {
                if (input != null) input.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void extractAssets(Context context, String executableFilePath, String filename) {

        AssetManager assetManager = context.getAssets();
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            inStream = assetManager.open(filename);
            outStream = new FileOutputStream(executableFilePath); // for override file content
            //outStream = new FileOutputStream(out,true); // for append file content

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + filename, e);
        }
        File execFile = new File(executableFilePath);
        execFile.setExecutable(true);
        Log.e(TAG, "Copy success: " + filename);
    }

    public static void vibrate(int duration) {
        RootUtils.runCommand("echo " + duration + " > /sys/class/timed_output/vibrator/enable");
    }

    public static List<String> getApplys(Class mClass) {
        List<String> applys = new ArrayList<>();

        if (mClass == BatteryFragment.class)
           applys.addAll(new ArrayList<>(Arrays.asList(BATTERY_ARRAY)));
        else if (mClass == CPUFragment.class) {
            for (String cpu : CPU_ARRAY)
                if (cpu.startsWith("/sys/devices/system/cpu/cpu%d"))
                    for (int i = 0; i < CPU.getCoreCount(); i++)
                        applys.add(String.format(cpu, i));
                else applys.add(cpu);
        } else if (mClass == CPUHotplugFragment.class) for (String[] array : CPU_HOTPLUG_ARRAY)
            applys.addAll(new ArrayList<>(Arrays.asList(array)));
        else if (mClass == CPUVoltageFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(CPU_VOLTAGE_ARRAY)));
        else if (mClass == EntropyFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(ENTROPY_ARRAY)));
        else if (mClass == GPUFragment.class) for (String[] arrays : GPU_ARRAY)
            applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == IOFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(IO_ARRAY)));
        else if (mClass == KSMFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(KSM_ARRAY)));
        else if (mClass == LMKFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(LMK_ARRAY)));
        else if (mClass == MiscFragment.class) for (String[] arrays : MISC_ARRAY)
             applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == ScreenFragment.class) for (String[] arrays : SCREEN_ARRAY)
            applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == SoundFragment.class) for (String[] arrays : SOUND_ARRAY)
            applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == ThermalFragment.class) for (String[] arrays : THERMAL_ARRAYS)
            applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == VMFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(VM_ARRAY)));
        else if (mClass == WakeFragment.class) for (String[] arrays : WAKE_ARRAY)
            applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == WakeLockFragment.class) for (String[] arrays : WAKELOCK_ARRAY)
            applys.addAll(new ArrayList<>(Arrays.asList(arrays)));
        else if (mClass == RamFragment.class)
            applys.addAll(new ArrayList<>(Arrays.asList(RAM_ARRAY)));

        return applys;
    }

    public static String formatCelsius(double celsius) {
        return round(celsius, 2) + "°C";
    }

    public static String celsiusToFahrenheit(double celsius) {
        return round(celsius * 9 / 5 + 32, 2) + "°F";
    }

    public static String round(double value, int places) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < places; i++) stringBuilder.append("#");
        DecimalFormat df = new DecimalFormat("#." + stringBuilder.toString());
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value);
    }

    public static long stringToLong(String number) {
        if(TextUtils.isEmpty(number)){
            return 0;
        }
        try {
            return Long.parseLong(number);
        } catch (Exception e) {
            return 0;
        }
    }

    public static int stringToInt(String number) {
        if(TextUtils.isEmpty(number)){
            return 0;
        }
        if(number.contains(".")){
            try {
                return Math.round(Float.parseFloat(number));
            } catch (Exception ignored){}
        } else {
            try {
                return Integer.parseInt(number);
            } catch (Exception ignored){}
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String link) {
        if (Build.VERSION.SDK_INT >= 24)
             return Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY);
        else
             return Html.fromHtml(link);
    }

    @SuppressWarnings("deprecation")
    public static String sysLocale() {
        if (Build.VERSION.SDK_INT >= 24)
            return Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        else
            return Resources.getSystem().getConfiguration().locale.getLanguage();
    }

    public static void launchUrl(Context context, String link) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int getActionBarHeight(Context context) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionBarSize = ta.getDimensionPixelSize(0, 112);
        ta.recycle();
        return actionBarSize;
    }

    public static boolean isTV(Context context) {
        return ((UiModeManager) context
                .getSystemService(Context.UI_MODE_SERVICE))
                .getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getScreenOrientation(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels <
                context.getResources().getDisplayMetrics().heightPixels ?
                Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
    }

   @MainThread
    public static void toast(String message, Context context) {
        toast(message, context, Toast.LENGTH_SHORT);
    }

   @MainThread
    public static void toast(String message, Context context, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
        if (view != null) view.setGravity(Gravity.CENTER);
        toast.show();
    }

    public static int getInt(String name, int defaults, Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(name, defaults);
    }

    public static void saveInt(String name, int value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(name, value).apply();
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        try {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(name, defaults);
        } catch (Exception ignored){
            return false;
        }
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(name, value).apply();
    }

    public static String getString(String name, String defaults, Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(name, defaults);
    }

    public static void saveString(String name, String value, Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(name, value).apply();
    }

    public static String getProp(String key) {
        return RootUtils.runCommand("getprop " + key);
    }

    public static boolean isPropActive(String key) {
        try {
            return RootUtils.runCommand("getprop | grep " + key).split("]:")[1].contains("running");
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean hasProp(String key) {
        try {
            return RootUtils.runCommand("getprop | grep " + key).split("]:").length > 1;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean existFile(String file) {
        return Tools.existFile(file, true);
    }

    public static boolean compareFiles(String file, String file2) {
        return Tools.compareFiles(file, file2, true);
    }

    public static void writeFile(String path, String text, boolean append) {
        Tools.writeFile(path, text, append, true);
    }

    public static String readFile(String file) {
        return Tools.readFile(file, true);
    }

    public static double stringtodouble (String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isLetter (String testchar) {
        if (Character.isLetter(testchar.charAt(0))) {
            return true;
        }
        return false;
    }

    public static boolean is64bit() {
        if (Build.SUPPORTED_64_BIT_ABIS.length >= 1) {
                return true;
        }
        return false;
    }


    public static String getsysfspath(String[] paths) {
        for (int i = 0; i < paths.length; i++) {
            if (Utils.existFile(paths[i])) {
                return paths[i];
            }
        }
        return "";
    }

    //Helper function to get paths with integer format substitutions
    public static String getsysfspath(String[] paths, int sub) {
        for (int i = 0; i < paths.length; i++) {
            if (Utils.existFile(String.format(paths[i], sub))){
                return String.format(paths[i], sub);
            }
        }
        return "";
    }

    public static String lessthanten(int time) {
        return (time < 10) ? "0" + time : "" + time;
    }

    public static String timeMs(long time) {
        String seconds, minutes, hours;

        time = time / 1000;
        seconds = lessthanten((int) time % 60);

        time = time / 60;
        minutes = lessthanten((int) time % 60);

        time = (time / 60);
        hours = lessthanten((int) time);

        return (hours + ":" + minutes + ":" + seconds);
    }

    public static void StartAppService(boolean enable, String service) {
        RootUtils.runCommand("am " + (enable ? "startservice " : "stopservice ") + service);
    }

    public static boolean GlobalIntGet(Context context, String name) {
        return (Settings.Global.getInt(context.getContentResolver(), name, 0) != 0);
    }

    public static void GlobalIntSet(Boolean isChecked, Context context, String name) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED)
            Settings.Global.putInt(context.getContentResolver(), name, isChecked ? 1 : 0);
    }

    public static int HasGlobalInt(Context context, String name, int def) {
        return (Settings.Global.getInt(context.getContentResolver(), name, def)); // e.g. SHOW_CPU can only be 0 or 1, def=10 if return 10 name doesn't exist
    }

    public static void WriteSettings(Context context) {
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_GRANTED)
            RootUtils.runCommand("pm grant " + context.getPackageName() + " android.permission.WRITE_SECURE_SETTINGS");
    }

    public static void DoNotification(Context context) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "no_thermal");
        notification.setSmallIcon(R.drawable.ka);
        notification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        notification.setContentTitle(context.getString(R.string.no_termal_toast));
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.no_termal_toast_2)));
        notification.setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent reEnableReceiver = new Intent();
        reEnableReceiver.setAction(THERMAL_ENGINE_RE_ENABLE);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, reEnableReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action ActionSet = new NotificationCompat.Action.Builder(R.drawable.ic_accept, context.getString(R.string.no_termal_reenable), pendingIntentYes).build();
        notification.addAction(ActionSet);

        notificationManager.notify(10, notification.build());
    }

    public static void ClearAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @TargetApi(23)
    public static void check_writeexternalstorage(FragmentActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                request_writeexternalstorage(activity);
            }
        }
    }

    @TargetApi(23)
    public static void request_writeexternalstorage(FragmentActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                123);
        }
    }
}
