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
import java.util.TreeMap;

/**
 * Created by joe on 2/29/16.
 */
public class Per_App {
    public static Map getInstalledApps (Context context) {
        // Get a list of installed apps. Currently this is only the package name
        final PackageManager pm = context.getPackageManager();
        //ArrayList<String> installedApps = new ArrayList<String>();
        final Map applist = new TreeMap();

        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        // Add a "Default" Selection to set the default profile"
        applist.put("Default","Default");

        Thread t = new Thread() {
            @Override
            public void run() {
                for (ApplicationInfo packageInfo : packages) {
                    applist.put(packageInfo.loadLabel(pm), packageInfo.packageName);
                }
            }
        };

        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return applist;
    }

    public static String[] getPackageNames (Map apps) {
        ArrayList<String> packages = new ArrayList<String>();
        for (int i = 0; i < apps.size(); i++) {
            packages.add(i, apps.values().toArray()[i].toString());
        }
        // Convert the list to strings for displaying
        String[] packagelist = new String[packages.size()];
        packagelist = packages.toArray(packagelist);

        return packagelist;
    }

    public static String[] getAppNames (Map apps) {
        ArrayList<String> applist = new ArrayList<String>();
        for (int i = 0; i < apps.size(); i++) {
            applist.add(i, apps.keySet().toArray()[i].toString());
        }
        // Convert the list to strings for displaying
        String[] app_names = new String[applist.size()];
        app_names = applist.toArray(app_names);

        return app_names;
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
            if (id.equals(service.getId())) {
                return true;
            }
        }

        return false;
    }

}
