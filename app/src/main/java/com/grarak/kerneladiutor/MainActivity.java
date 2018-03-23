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

package com.grarak.kerneladiutor;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.ScrimInsetsFrameLayout;
import com.grarak.kerneladiutor.elements.SplashView;
import com.grarak.kerneladiutor.fragments.BaseFragment;
import com.grarak.kerneladiutor.fragments.information.FrequencyTableFragment;
import com.grarak.kerneladiutor.fragments.information.KernelInformationFragment;
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
import com.grarak.kerneladiutor.fragments.kernel.WakeLockFragment;
import com.grarak.kerneladiutor.fragments.kernel.WakeFragment;
import com.grarak.kerneladiutor.fragments.other.AboutusFragment;
import com.grarak.kerneladiutor.fragments.other.SettingsFragment;
import com.grarak.kerneladiutor.fragments.tools.BackupFragment;
import com.grarak.kerneladiutor.fragments.tools.BuildpropFragment;
import com.grarak.kerneladiutor.fragments.tools.InitdFragment;
import com.grarak.kerneladiutor.fragments.tools.LogsFragment;
import com.grarak.kerneladiutor.fragments.tools.StartUpCommandsFragment;
import com.grarak.kerneladiutor.fragments.tools.ProfileFragment;
import com.grarak.kerneladiutor.fragments.tools.RecoveryFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.CPUHotplug;
import com.grarak.kerneladiutor.utils.kernel.CPUVoltage;
import com.grarak.kerneladiutor.utils.kernel.Entropy;
import com.grarak.kerneladiutor.utils.kernel.GPU;
import com.grarak.kerneladiutor.utils.kernel.KSM;
import com.grarak.kerneladiutor.utils.kernel.LMK;
import com.grarak.kerneladiutor.utils.kernel.Ram;
import com.grarak.kerneladiutor.utils.kernel.Screen;
import com.grarak.kerneladiutor.utils.kernel.Sound;
import com.grarak.kerneladiutor.utils.kernel.Thermal;
import com.grarak.kerneladiutor.utils.kernel.Wake;
import com.grarak.kerneladiutor.utils.kernel.WakeLock;
import com.grarak.kerneladiutor.utils.tools.Backup;
import com.grarak.kerneladiutor.utils.tools.Buildprop;
import com.kerneladiutor.library.root.RootUtils;
import com.kerneladiutor.library.root.RootFile;

import java.io.File;
import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 01.12.14.
 */
public class MainActivity extends BaseActivity implements Constants {

    /**
     * Views
     */
    private Toolbar toolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private static ScrimInsetsFrameLayout mScrimInsetsFrameLayout;
    private RecyclerView mDrawerList;
    private SplashView mSplashView;

    private DAdapter.Adapter mAdapter;

    private boolean pressAgain = true;

    /**
     * Current Fragment position
     */
    private int cur_position;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CustomContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            this.registerReceiver(updateMainReceiver, new IntentFilter("updateMainReceiver"));
        } catch (NullPointerException ignored) {}

        setView();
        String password;
        extractAssets(this);
        if (!(password = Utils.getString("password", "", this)).isEmpty()) askPassword(password);
        else new Task().execute(); // Use an AsyncTask to initialize everything
        Utils.saveBoolean("ka_run", true, MainActivity.this);
    }

    @Override
    public int getParentViewId() {
        return Utils.isTV(this) ? R.layout.activity_main_tv : R.layout.activity_main;
    }

    @Override
    public View getParentView() {
        return null;
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar == null ? toolbar = (Toolbar) findViewById(R.id.toolbar) : toolbar;
    }

    @Override
    public void setStatusBarColor() {
    }

    /**
     * Dialog which asks the user to enter his password
     *
     * @param password current encoded password
     */
    private void askPassword(final String password) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        final AppCompatEditText mPassword = new AppCompatEditText(this);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setHint(getString(R.string.password));
        linearLayout.addView(mPassword);

        new AlertDialog.Builder(this).setView(linearLayout).setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mPassword.getText().toString().equals(Utils.decodeString(password)))
                            new Task().execute();
                        else {
                            Utils.toast(getString(R.string.password_wrong), MainActivity.this);
                            finish();
                        }
                    }
                }).show();
    }

    /**
     * Gets called when there is an input on the navigation drawer
     *
     * @param position position of the fragment
     */
    private void selectItem(int position) {
        Fragment fragment = VISIBLE_ITEMS.get(position).getFragment();

        if (mScrimInsetsFrameLayout != null) mDrawerLayout.closeDrawer(mScrimInsetsFrameLayout);
        if (fragment == null || cur_position == position) return;
        cur_position = position;

        try {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null)
            actionBar.setTitle(VISIBLE_ITEMS.get(position).getTitle());
        mAdapter.setItemChecked(position, true);
    }

    /**
     * Add all fragments in a list
     */
    private void setList() {
        ITEMS.clear();
        ITEMS.add(new DAdapter.MainHeader());
        ITEMS.add(new DAdapter.Header(getString(R.string.information)));
        ITEMS.add(new DAdapter.Item(getString(R.string.kernel_information), new KernelInformationFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.frequency_table), new FrequencyTableFragment()));
        ITEMS.add(new DAdapter.Header(getString(R.string.kernel)));

        ITEMS.add(new DAdapter.Item(getString(R.string.cpu), new CPUFragment()));
        if (CPUHotplug.hasCpuHotplug()) ITEMS.add(new DAdapter.Item(getString(R.string.cpu_hotplug), new CPUHotplugFragment()));
        if (CPUVoltage.hasCpuVoltage()) ITEMS.add(new DAdapter.Item(getString(R.string.cpu_voltage), new CPUVoltageFragment()));
        if (Entropy.hasEntropy()) ITEMS.add(new DAdapter.Item(getString(R.string.entropy), new EntropyFragment()));
        if (GPU.hasGpuControl()) ITEMS.add(new DAdapter.Item(getString(R.string.gpu), new GPUFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.io_scheduler), new IOFragment()));
        if (KSM.hasKsm()) ITEMS.add(new DAdapter.Item(getString(R.string.ksm), new KSMFragment()));
        if (LMK.getMinFrees() != null) ITEMS.add(new DAdapter.Item(getString(R.string.low_memory_killer), new LMKFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.misc_controls), new MiscFragment()));
        if (!Utils.isTV(this)) ITEMS.add(new DAdapter.Item(getString(R.string.battery), new BatteryFragment()));//Power and battery
        if (Ram.hasRamControl()) ITEMS.add(new DAdapter.Item(getString(R.string.ram), new RamFragment()));
        if (Screen.hasScreen()) ITEMS.add(new DAdapter.Item(getString(R.string.screen), new ScreenFragment()));
        if (Sound.hasSound()) ITEMS.add(new DAdapter.Item(getString(R.string.sound), new SoundFragment()));
        if (Thermal.hasThermal()) ITEMS.add(new DAdapter.Item(getString(R.string.thermal), new ThermalFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.virtual_memory), new VMFragment()));
        if (Wake.hasWake()) ITEMS.add(new DAdapter.Item(getString(R.string.wake_controls), new WakeFragment()));
        if (WakeLock.hasAnyWakelocks()) ITEMS.add(new DAdapter.Item(getString(R.string.wakelocks), new WakeLockFragment()));

        ITEMS.add(new DAdapter.Header(getString(R.string.tools)));
        if (Backup.hasBackup()) ITEMS.add(new DAdapter.Item(getString(R.string.backup), new BackupFragment()));
        if (Buildprop.hasBuildprop()) ITEMS.add(new DAdapter.Item(getString(R.string.build_prop_editor), new BuildpropFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.initd), new InitdFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.logs), new LogsFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.profile), new ProfileFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.recovery), new RecoveryFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.startup_commands), new StartUpCommandsFragment()));
        ITEMS.add(new DAdapter.Header(getString(R.string.other)));
        ITEMS.add(new DAdapter.Item(getString(R.string.settings), new SettingsFragment()));
        ITEMS.add(new DAdapter.Item(getString(R.string.about_us), new AboutusFragment()));
    }

    /**
     * Define all views
     */
    private void setView() {
        mScrimInsetsFrameLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.statusbar_color));
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }
        mDrawerList = (RecyclerView) findViewById(R.id.drawer_list);
        mSplashView = (SplashView) findViewById(R.id.splash_view);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mDrawerList.setLayoutManager(mLayoutManager);
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }

    public void setItems(BaseFragment fragment) {
        List<DAdapter.DView> tmpViews = new ArrayList<>();
        for (DAdapter.DView item : ITEMS)
            if (item.getFragment() == null
                    || Utils.getBoolean(item.getFragment().getClass().getSimpleName() + "visible", true, this))
                tmpViews.add(item);

        VISIBLE_ITEMS.clear();
        // Sort out headers without any sections
        for (int i = 0; i < tmpViews.size(); i++)
            if ((tmpViews.get(i).getFragment() == null && i < tmpViews.size() && tmpViews.get(i + 1).getFragment() != null)
                    || tmpViews.get(i).getFragment() != null
                    || tmpViews.get(i) instanceof DAdapter.MainHeader)
                VISIBLE_ITEMS.add(tmpViews.get(i));

        mAdapter = new DAdapter.Adapter(VISIBLE_ITEMS);
        mDrawerList.setAdapter(mAdapter);
        mAdapter.setItemOnly(true);
        mAdapter.setOnItemClickListener(new DAdapter.Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectItem(position);
            }
        });
        if (fragment != null) for (int i = 0; i < VISIBLE_ITEMS.size(); i++)
            if (VISIBLE_ITEMS.get(i).getFragment() != null && VISIBLE_ITEMS.get(i).getFragment() == fragment) {
                cur_position = i;
                mAdapter.setItemChecked(i, true);
            }

    }

    /**
     * Setup the views
     */
    private void setInterface() {
        if (mScrimInsetsFrameLayout != null) {
            mScrimInsetsFrameLayout.setLayoutParams(getDrawerParams());
            if (Utils.DARKTHEME)
                mScrimInsetsFrameLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.navigationdrawer_background_dark));
        }

        setItems(null);
        if (mDrawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, 0, 0);
            mDrawerLayout.addDrawerListener(mDrawerToggle);

            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (mDrawerToggle != null) mDrawerToggle.syncState();
                }
            });
        }
    }

    private class Task extends AsyncTask<Void, Void, Void> {

        private boolean hasRoot;

        @Override
        protected Void doInBackground(Void...params) {
            // Check root access
            if (RootUtils.rooted()) hasRoot = RootUtils.rootAccess();

            if (hasRoot) {
                // Set permissions to specific files which are not readable by default
                String[] writePermission = {
                    LMK_MINFREE
                };
                for (String file: writePermission)
                    RootUtils.runCommand("chmod 644 " + file);

                setList();
            }
            Utils.check_writeexternalstorage(MainActivity.this);

            // Create a blank profiles.json to prevent logspam.
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            String profpath = (sdcard +"/KA_profiles/");
            if (!Utils.existFile(profpath)) {
                RootFile dir = new RootFile(profpath);
                dir.mkdir();
            }
            String file = sdcard + "/KA_profiles/profiles.json";
            if (!Utils.existFile(file)) RootUtils.runCommand("echo > " + file);
            file = sdcard + "/KA_profiles/per_app.json";
            if (!Utils.existFile(file)) RootUtils.runCommand("echo > " + file);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!hasRoot) {
                Intent i = new Intent(MainActivity.this, TextActivity.class);
                Bundle args = new Bundle();
                args.putString(TextActivity.ARG_TEXT, getString(R.string.no_root));
                Log.d(TAG, "no root");
                i.putExtras(args);
                startActivity(i);

                cancel(true);
                finish();
                return;
            }

            mSplashView.finish();
            setInterface();

            // Start with the very first fragment on the list
            for (int i = 0; i < VISIBLE_ITEMS.size(); i++) {
                if (VISIBLE_ITEMS.get(i).getFragment() != null) {
                    selectItem(i);
                    break;
                }
            }
        }
    }

    @Override
    public boolean getDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mScrimInsetsFrameLayout != null)
            mScrimInsetsFrameLayout.setLayoutParams(getDrawerParams());
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * This makes onBackPressed function work in Fragments
     */
    @Override
    public void onBackPressed() {
        try {
            if (!VISIBLE_ITEMS.get(cur_position).getFragment().onBackPressed())
                if (mDrawerLayout == null || !mDrawerLayout.isDrawerOpen(mScrimInsetsFrameLayout)) {
                    if (pressAgain) {
                        Utils.toast(getString(R.string.press_back_again), this);
                        pressAgain = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pressAgain = true;
                            }
                        }, 2000);
                    } else super.onBackPressed();
                } else mDrawerLayout.closeDrawer(mScrimInsetsFrameLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exit SU
     */
    @Override
    protected void onDestroy() {
        RootUtils.closeSU();
        super.onDestroy();
    }

    /**
	     * A function to set Navigation Drawer Parameters
     *
     * @return the LayoutParams for the Drawer
     */
    private DrawerLayout.LayoutParams getDrawerParams() {
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mScrimInsetsFrameLayout.getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels;

        boolean tablet = Utils.isTablet(this);
        int actionBarSize = Utils.getActionBarHeight(this);
        if (Utils.getScreenOrientation(this) == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = width / 2;
            if (tablet)
                params.width -= actionBarSize + (35 * getResources().getDisplayMetrics().density);
        } else params.width = tablet ? width / 2 : width - actionBarSize;

        // Allow configuration of the Navigation drawer to the right side rather than the left
        if (Utils.getBoolean("Navbar_Position_Alternate", false, this)) {
            params.gravity = Gravity.END;
        }

        return params;
    }

    /**
     * Interface to make onBackPressed function work in Fragments
     */
    public interface OnBackButtonListener {
        boolean onBackPressed();
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @TargetApi(23)
    private void check_writeexternalstorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWriteExternalPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        return;
    }

    public void extractAssets(Context context) {
        String executableFilePath = context.getFilesDir().getPath() + "/";
        if (!Utils.existFile(executableFilePath + "busybox"))
            Utils.extractAssets(context, executableFilePath + "busybox", "busybox");
    }

    // Helper function to allow dynamic relocation of Navigation Drawer
    public static void reconfigureNavigationDrawer(Context context) {
        if (mScrimInsetsFrameLayout != null) {
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mScrimInsetsFrameLayout.getLayoutParams();
            // Allow configuration of the Navigation drawer to the right side rather than the left
            if (Utils.getBoolean("Navbar_Position_Alternate", false, context)) {
                params.gravity = Gravity.END;
            } else {
                params.gravity = Gravity.START;
            }
        }
    }

    private final BroadcastReceiver updateMainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
}
