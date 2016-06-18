package com.grarak.kerneladiutor.utils.tools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.grarak.kerneladiutor.utils.database.PerAppDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by joe on 2/29/16.
 */
public class Per_App {
    public static Map<String, String> getInstalledApps (Context context) {
        // Get a list of installed apps. Currently this is only the package name
        final PackageManager pm = context.getPackageManager();

        final Map<String, String> applist = new HashMap<>();

        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            applist.put(String.valueOf(packageInfo.loadLabel(pm)), packageInfo.packageName);
        }

        return applist;
    }

    public static String[] getPackageNames (Map<String, String> apps) {
        String[] array = apps.values().toArray(new String[apps.size()]);
        Arrays.sort(array, String.CASE_INSENSITIVE_ORDER);
        array = insertDefaultListing(array);
        return  array;
    }

    public static String[] getAppNames (Map<String, String> apps) {
        String[] array = apps.keySet().toArray(new String[apps.size()]);
        Arrays.sort(array, String.CASE_INSENSITIVE_ORDER);
        array = insertDefaultListing(array);
        return  array;
    }

    private static String[] insertDefaultListing(String[] array){
        String[] newArray = new String[array.length+1];
        newArray[0] = "Default";
        System.arraycopy(array,0,newArray,1,array.length);
        return newArray;
    }

    public static void save_app (String app, String id, Context context) {
        PerAppDB perappDB = new PerAppDB(context);
        List<PerAppDB.PerAppItem> PerAppItem = perappDB.getAllApps() ;
        for (int i = 0; i < PerAppItem.size(); i++) {
            String p = PerAppItem.get(i).getApp();
            if (p != null && p.equals(app)) {
                perappDB.delApp(i);
            }
        }

        perappDB.putApp(app, id);
        perappDB.commit();
    }

    public static void remove_app (String app, String id, Context context) {
        PerAppDB perappDB = new PerAppDB(context);

        List<PerAppDB.PerAppItem> PerAppItem = perappDB.getAllApps() ;
        for (int i = 0; i < PerAppItem.size(); i++) {
            String p = PerAppItem.get(i).getApp();
            if (p != null && p.equals(app)) {
                perappDB.delApp(i);
            }
        }

        perappDB.commit();
    }

    public static boolean app_profile_exists (String app, Context context) {
        PerAppDB perappDB = new PerAppDB(context);
        boolean exists = perappDB.containsApp(app);

        return exists;
    }

    public static ArrayList<String> app_profile_info (String app, Context context) {
        PerAppDB perappDB = new PerAppDB(context);
        if (perappDB.containsApp(app)) {
            return perappDB.get_info(app);
        }

        return null;
    }

    public static boolean[] getExistingSelections (String[] apps, String profile, Context context) {
        PerAppDB perappDB = new PerAppDB(context);
        boolean exists[] = new boolean[apps.length];

        List<PerAppDB.PerAppItem> PerAppItem = perappDB.getAllApps() ;
        for (int i = 0; i < PerAppItem.size(); i++) {
            String p = PerAppItem.get(i).getApp();
            String id = PerAppItem.get(i).getID();
            if (p != null && Arrays.asList(apps).contains(p)) {
                if (id != null && id.equals(profile)) {
                    exists[Arrays.asList(apps).indexOf(p)] = true;
                }
            }
        }
        return exists;
    }

    public static boolean isAccessibilityEnabled(Context context, String id) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            if (id != null && id.equals(service.getId())) {
                return true;
            }
        }

        return false;
    }

}
