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
package com.grarak.kerneladiutor.fragments.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.Manifest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.grarak.kerneladiutor.MainActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.GetPermission;
import com.grarak.kerneladiutor.utils.kernel.CPU;
import com.grarak.kerneladiutor.utils.kernel.Misc;
import com.grarak.kerneladiutor.utils.Utils;
import com.kerneladiutor.library.root.RootUtils;
import com.kerneladiutor.library.root.RootFile;

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
import com.grarak.kerneladiutor.utils.database.CommandDB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.zeroturnaround.zip.ZipUtil;
import com.afollestad.materialdialogs.MaterialDialog;
/**
 * Created by willi on 09.03.15. Mod by Felipe L 09.26.16
 */
public class LogsFragment extends RecyclerViewFragment {

    private static String final_logcat;
    private static String final_log_radio;
    private static String final_log_events;
    private static String final_driver_message;
    private static String final_last_driver_message;
    private static String final_get_prop;
    private static String final_grep;

    private static final String logcatC = "logcat -d ";
    private static final String radioC = "logcat  -b radio -v time -d ";
    private static final String eventsC = "logcat -b events -v time -d ";
    private static final String dmesgC = "dmesg ";
    private static final String getpropC = "getprop ";
    private static final String consoleramoopsC = "cat /sys/fs/pstore/console-ramoops*";
    private static final String dmesgramoopsC = "cat /sys/fs/pstore/dmesg-ramoops*";
    private static final String log_folder = Environment.getExternalStorageDirectory().getPath() + "/KA_Logs/";
    private static final String grep = " | grep -i ";

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void preInit(Bundle savedInstanceState) {
        super.preInit(savedInstanceState);
        if (!Misc.isLoggerActive())
            Utils.toast(getString(R.string.logcat_disable_summary), getActivity(), Toast.LENGTH_LONG);

        if (!Utils.existFile(log_folder)) {
            RootFile dir = new RootFile(log_folder);
            dir.mkdir();
        }
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        debuggingInit();
    }

    private void debuggingInit() {

        if (!Misc.isLoggerActive()) {
            CardViewItem.DCardView mLogDisableCard = new CardViewItem.DCardView();
            mLogDisableCard.setTitle(getString(R.string.logcat_disable));
            mLogDisableCard.setDescription(getString(R.string.logcat_disable_summary));

            addView(mLogDisableCard);
        }

        DDivider mLogsDividerCard = new DDivider();
        mLogsDividerCard.setText(getString(R.string.logs));
        addView(mLogsDividerCard);

        CardViewItem.DCardView mAllLogsCard = new CardViewItem.DCardView();
        mAllLogsCard.setTitle(getString(R.string.zip_log));
        mAllLogsCard.setDescription(String.format(getString(R.string.zip_log_summary), getDate()));
        mAllLogsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(1);
            }
        });

        addView(mAllLogsCard);

        DDivider mGrepDividerCard = new DDivider();
        mGrepDividerCard.setText(getString(R.string.grep_log));
        addView(mGrepDividerCard);

        CardViewItem.DCardView mSearchCard = new CardViewItem.DCardView();
        mSearchCard.setTitle(getString(R.string.search_log));
        mSearchCard.setDescription(getString(R.string.search_log_summary));
        mSearchCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(2);
            }
        });

        addView(mSearchCard);

        DDivider mIndLogsDividerCard = new DDivider();
        mIndLogsDividerCard.setText(getString(R.string.ind_logs));
        addView(mIndLogsDividerCard);

        CardViewItem.DCardView mLogcatCard = new CardViewItem.DCardView();
        mLogcatCard.setTitle(getString(R.string.logcat));
        mLogcatCard.setDescription(String.format(getString(R.string.logcat_summary), getDate()));
        mLogcatCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(3);
            }
        });

        CardViewItem.DCardView mLogRadioCard = new CardViewItem.DCardView();
        mLogRadioCard.setTitle(getString(R.string.log_radio));
        mLogRadioCard.setDescription(String.format(getString(R.string.log_radio_summary), getDate()));
        mLogRadioCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(4);
            }
        });

        CardViewItem.DCardView mLogEventsCard = new CardViewItem.DCardView();
        mLogEventsCard.setTitle(getString(R.string.log_events));
        mLogEventsCard.setDescription(String.format(getString(R.string.log_events_summary), getDate()));
        mLogEventsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(5);

            }
        });

        CardViewItem.DCardView mLastDmesgCard = new CardViewItem.DCardView();
        mLastDmesgCard.setTitle(getString(R.string.last_driver_message));
        mLastDmesgCard.setDescription(String.format(getString(R.string.last_driver_message_summary), getDate()));
        mLastDmesgCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(6);
            }
        });

        CardViewItem.DCardView mDmesgCard = new CardViewItem.DCardView();
        mDmesgCard.setTitle(getString(R.string.driver_message));
        mDmesgCard.setDescription(String.format(getString(R.string.driver_message_summary), getDate()));
        mDmesgCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(7);
            }
        });

        CardViewItem.DCardView mGetPropCard = new CardViewItem.DCardView();
        mGetPropCard.setTitle(getString(R.string.get_prop));
        mGetPropCard.setDescription(String.format(getString(R.string.get_prop_summary), getDate()));
        mGetPropCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(8);
            }
        });

        CardViewItem.DCardView mKernelChanges = new CardViewItem.DCardView();
        mKernelChanges.setTitle(getString(R.string.kernel_changes));
        mKernelChanges.setDescription(String.format(getString(R.string.kernel_changes_summary), getDate()));
        mKernelChanges.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                LogsonClick(9);
            }
        });

        addView(mLastDmesgCard);
        addView(mDmesgCard);
        addView(mLogEventsCard);
        addView(mGetPropCard);
        addView(mKernelChanges);
        addView(mLogcatCard);
        addView(mLogRadioCard);

        final StringBuilder lastKmsg = new StringBuilder();
        if (Utils.existFile("/proc/last_kmsg")) lastKmsg.append("/proc/last_kmsg");
        else if (Utils.existFile("/sys/fs/pstore/console-ramoops"))
            lastKmsg.append("/sys/fs/pstore/console-ramoops");
        if (lastKmsg.length() > 0) {
            CardViewItem.DCardView mLastKmsgCard = new CardViewItem.DCardView();
            mLastKmsgCard.setTitle(getString(R.string.last_kmsg));
            mLastKmsgCard.setDescription(getString(R.string.last_kmsg_summary));
            mLastKmsgCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    logs("cat " + lastKmsg.toString(), log_folder, "last_kmsg" + getDate());
                }
            });

            addView(mLastKmsgCard);
        }
    }

    private void logs(String log, String path, String file) {
        new Execute(getActivity()).execute(log + " > " + path + file + ".txt");
    }

    public static String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH_mm", Locale.US);
        Date date = new Date();
        String Final_Date = "_" + dateformate.format(date);
        return Final_Date;
    }

    private static class Execute extends AsyncTask < String, Void, Void > {
        private MaterialDialog progressDialog;
        private WeakReference < Context > contextRef;

        public Execute(Context context) {
            contextRef = new WeakReference < > (context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context mContext = contextRef.get();
            progressDialog = new MaterialDialog.Builder(mContext)
                .title(mContext.getString(R.string.logs))
                .content(mContext.getString(R.string.execute))
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .show();
        }

        @Override
        protected Void doInBackground(String...params) {
            Context mContext = contextRef.get();
            if (params[0].equals("zip")) {
                String log_temp_folder = log_folder + ".tmpziplog/";
                boolean zip_ok = false;
                String zip_file = log_folder + "logs" + getDate() + ".zip";
                String logcat = log_temp_folder + "logcat.txt";
                String tmplogcat = log_temp_folder + "tmplogcat.txt";
                if (Utils.existFile(log_temp_folder)) {
                    RootUtils.runCommand("rm -rf " + log_temp_folder);
                    RootFile dir = new RootFile(log_temp_folder);
                    dir.mkdir();
                } else {
                    RootFile dir = new RootFile(log_temp_folder);
                    dir.mkdir();
                }
                if (!Misc.isLoggerActive()) {
                    RootUtils.runCommand(dmesgC + " > " + log_temp_folder + "dmesg.txt");
                    RootUtils.runCommand(getpropC + " > " + log_temp_folder + "getprop.txt");
                    RootUtils.runCommand(consoleramoopsC + " > " + log_temp_folder + "lastdmesg.txt");
                    RootUtils.runCommand("echo '\ndmesgramoops\n' >> " + log_temp_folder + "lastdmesg.txt");
                    RootUtils.runCommand(dmesgramoopsC + " >> " + log_temp_folder + "lastdmesg.txt");
                    KernelChanges(log_temp_folder, false, mContext);
                    // ZipUtil doesnot understand folder name that end with /
                    ZipUtil.pack(new File(log_folder + "/.tmpziplog"), new File(log_folder + "logs" + getDate() + ".zip"));
                } else {
                    RootUtils.runCommand(logcatC + " > " + logcat);
                    RootUtils.runCommand(radioC + " > " + log_temp_folder + "radio.txt");
                    RootUtils.runCommand(eventsC + " > " + log_temp_folder + "events.txt");
                    RootUtils.runCommand(dmesgC + " > " + log_temp_folder + "dmesg.txt");
                    RootUtils.runCommand(getpropC + " > " + log_temp_folder + "getprop.txt");
                    RootUtils.runCommand(consoleramoopsC + " > " + log_temp_folder + "lastdmesg.txt");
                    RootUtils.runCommand("echo '\ndmesgramoops\n' >> " + log_temp_folder + "lastdmesg.txt");
                    RootUtils.runCommand(dmesgramoopsC + " >> " + log_temp_folder + "lastdmesg.txt");
                    KernelChanges(log_temp_folder, false, mContext);
                    RootUtils.runCommand("rm -rf " + log_temp_folder + "logcat_wile.txt");
                    // ZipUtil doesn’t understand folder name that end with /
                    // Logcat some times is too long and the zip logcat.txt may be empty, do some check
                    while (!zip_ok) {
                        ZipUtil.pack(new File(log_folder + "/.tmpziplog"), new File(zip_file));
                        ZipUtil.unpackEntry(new File(zip_file), "logcat.txt", new File(tmplogcat));
                        if (Utils.compareFiles(logcat, tmplogcat)) {
                            Log.i(Constants.TAG, "ziped logcat.txt is ok");
                            RootUtils.runCommand("rm -rf " + log_temp_folder);
                            zip_ok = true;
                        } else {
                            Log.i(Constants.TAG, "logcat.txt is nok");
                            RootUtils.runCommand("rm -rf " + zip_file);
                            RootUtils.runCommand("rm -rf " + tmplogcat);
                        }
                    }
                }
            } else if (params[0].equals("kernel_changes"))
                KernelChanges(log_folder, true, mContext);
            else if (params[0].equals("lastdmesg")) {
                String lastdate = getDate();
                RootUtils.runCommand(consoleramoopsC + " > " + log_folder + "lastdmesg" + lastdate + ".txt");
                RootUtils.runCommand("echo '\ndmesgramoops\n' >> " + log_folder + "lastdmesg" + lastdate + ".txt");
                RootUtils.runCommand(dmesgramoopsC + " >> " + log_folder + "lastdmesg" + lastdate + ".txt");
            } else
                RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    private void GrepLogs() {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        TextView descriptionText = new TextView(getActivity());
        descriptionText.setText(getString(R.string.search_dialog_summary));
        linearLayout.addView(descriptionText);

        final AppCompatEditText grep_string = new AppCompatEditText(getActivity());
        grep_string.setHint(getString(R.string.log_hint));
        linearLayout.addView(grep_string);

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setPadding(0, 0, 0, 10);
        linearLayout.addView(scrollView);

        LinearLayout checkBoxLayout = new LinearLayout(getActivity());
        checkBoxLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(checkBoxLayout);

        AppCompatButton selectAllButton = new AppCompatButton(getActivity());
        selectAllButton.setText(getString(R.string.select_all));
        checkBoxLayout.addView(selectAllButton);

        final AppCompatCheckBox logcat = new AppCompatCheckBox(getActivity());
        logcat.setText(getString(R.string.logcat));

        final AppCompatCheckBox log_radio = new AppCompatCheckBox(getActivity());
        log_radio.setText(getString(R.string.log_radio));

        final AppCompatCheckBox log_events = new AppCompatCheckBox(getActivity());
        log_events.setText(getString(R.string.log_events));

        final AppCompatCheckBox driver_message = new AppCompatCheckBox(getActivity());
        driver_message.setText(getString(R.string.driver_message));

        final AppCompatCheckBox last_driver_message = new AppCompatCheckBox(getActivity());
        last_driver_message.setText(getString(R.string.last_driver_message));

        final AppCompatCheckBox get_prop = new AppCompatCheckBox(getActivity());
        get_prop.setText(getString(R.string.get_prop));

        checkBoxLayout.addView(last_driver_message);
        checkBoxLayout.addView(driver_message);
        if (Misc.isLoggerActive()) checkBoxLayout.addView(log_events);
        checkBoxLayout.addView(get_prop);
        if (Misc.isLoggerActive()) {
            checkBoxLayout.addView(logcat);
            checkBoxLayout.addView(log_radio);
        }

        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatCheckBox) logcat).setChecked(true);
                ((AppCompatCheckBox) log_radio).setChecked(true);
                ((AppCompatCheckBox) log_events).setChecked(true);
                ((AppCompatCheckBox) last_driver_message).setChecked(true);
                ((AppCompatCheckBox) driver_message).setChecked(true);
                ((AppCompatCheckBox) get_prop).setChecked(true);
            }
        });
        new AlertDialog.Builder(getActivity(),
                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
            .setTitle(getString(R.string.search_dialog))
            .setView(linearLayout).setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String name = grep_string.getText().toString();
                    final_logcat = "";
                    final_log_radio = "";
                    final_log_events = "";
                    final_driver_message = "";
                    final_last_driver_message = "";
                    final_get_prop = "";
                    if (name.isEmpty()) {
                        Utils.toast(getString(R.string.empty_text), getActivity(), Toast.LENGTH_LONG);
                        return;
                    } else if (name.contains("-") || name.contains("'") || name.contains("\"")) {
                        Utils.toast(getString(R.string.forbidden_special), getActivity(), Toast.LENGTH_LONG);
                        return;
                    }
                    if (((AppCompatCheckBox) logcat).isChecked()) {
                        final String logcat = RootUtils.runCommand(logcatC + grep + "'" + name + "'");
                        if (!logcat.isEmpty())
                            final_logcat = getString(R.string.logcat) + " " + getString(R.string.result) + "\n\n" + logcat + "\n\n";
                    }
                    if (((AppCompatCheckBox) log_radio).isChecked()) {
                        final String log_radio = RootUtils.runCommand(radioC + grep + "'" + name + "'");
                        if (!log_radio.isEmpty())
                            final_log_radio = getString(R.string.log_radio) + " " + getString(R.string.result) + "\n\n" + log_radio + "\n\n";
                    }
                    if (((AppCompatCheckBox) log_events).isChecked()) {
                        final String log_events = RootUtils.runCommand(eventsC + grep + "'" + name + "'");
                        if (!log_events.isEmpty())
                            final_log_events = getString(R.string.log_events) + " " + getString(R.string.result) + "\n\n" + log_events + "\n\n";
                    }
                    if (((AppCompatCheckBox) driver_message).isChecked()) {
                        final String driver_message = RootUtils.runCommand(dmesgC + grep + "'" + name + "'");
                        if (!driver_message.isEmpty())
                            final_driver_message = getString(R.string.driver_message) + " " + getString(R.string.result) + "\n\n" + driver_message + "\n\n";
                    }
                    if (((AppCompatCheckBox) last_driver_message).isChecked()) {
                        String last_driver_message = "";
                        last_driver_message = last_driver_message + RootUtils.runCommand(consoleramoopsC + grep + "'" + name + "'");
                        last_driver_message = last_driver_message + RootUtils.runCommand(dmesgramoopsC + grep + "'" + name + "'");
                        if (!last_driver_message.isEmpty())
                            final_last_driver_message = getString(R.string.last_driver_message) + " " + getString(R.string.result) + "\n\n" + last_driver_message + "\n\n";
                    }
                    if (((AppCompatCheckBox) get_prop).isChecked()) {
                        final String get_prop = RootUtils.runCommand(getpropC + grep + "'" + name + "'");
                        if (!get_prop.isEmpty())
                            final_get_prop = getString(R.string.get_prop) + " " + getString(R.string.result) + "\n\n" + get_prop + "\n\n\n";
                    }
                    if (!(((AppCompatCheckBox) logcat).isChecked()) && !(((AppCompatCheckBox) log_radio).isChecked()) &&
                        !(((AppCompatCheckBox) log_events).isChecked()) && !(((AppCompatCheckBox) driver_message).isChecked()) &&
                        !(((AppCompatCheckBox) get_prop).isChecked()) && !(((AppCompatCheckBox) last_driver_message).isChecked())) {
                        Utils.toast(getString(R.string.no_log_selected), getActivity(), Toast.LENGTH_LONG);
                        return;
                    } else
                        final_grep = final_logcat + final_log_radio + final_log_events +
                        final_last_driver_message + final_driver_message + final_get_prop;
                    if (!final_grep.isEmpty()) {
                        LinearLayout linearLayout = new LinearLayout(getActivity());
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.setGravity(Gravity.CENTER);
                        linearLayout.setPadding(30, 20, 30, 20);

                        TextView result_tip = new TextView(getActivity());
                        String result_tip_final = getString(R.string.result_tip) + name + "\n";
                        result_tip.setText(result_tip_final);
                        linearLayout.addView(result_tip);

                        ScrollView scrollView = new ScrollView(getActivity());
                        scrollView.setPadding(0, 0, 0, 10);
                        linearLayout.addView(scrollView);

                        TextView final_result = new TextView(getActivity());
                        final_result.setText(final_grep);
                        final_result.setTextIsSelectable(true);
                        scrollView.addView(final_result);

                        new AlertDialog.Builder(getActivity(),
                                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                            .setTitle(getString(R.string.result))
                            .setView(linearLayout).setNegativeButton(getString(R.string.copy_clipboard),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Logs", getString(R.string.search_text) + name + "\n\n" + final_grep);
                                        clipboard.setPrimaryClip(clip);
                                        Utils.toast(getString(R.string.copy_clipboard_ok), getActivity(), Toast.LENGTH_LONG);
                                        return;
                                    }
                                })
                            .setPositiveButton(getString(R.string.save_to_file),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        RootUtils.runCommand("echo " + "'" + getString(R.string.search_text) + name + "\n\n" + final_grep + "'" + " > " + log_folder + "grep_a_log" + getDate() + ".txt");
                                        new AlertDialog.Builder(getActivity(),
                                                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                                            .setTitle(getString(R.string.saved_to))
                                            .setMessage(String.format(getString(R.string.saved_to_summary), getDate()))
                                            .setNegativeButton(getString(R.string.close),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        return;
                                                    }
                                                }).show();
                                    }
                                }).show();
                    } else
                        Utils.toast(getString(R.string.result_empty), getActivity(), Toast.LENGTH_LONG);
                }
            }).show();
    }

    private void dumpsysfs(String path, boolean date) {
        String arrays[][] = {
            Constants.BATTERY_ARRAY,
            Constants.CPU_ARRAY,
            Constants.CPU_VOLTAGE_ARRAY,
            Constants.IO_ARRAY,
            Constants.RAM_ARRAY,
            Constants.VM_ARRAY
        };
        String twodarrays[][][] = {
            Constants.CPU_HOTPLUG_ARRAY,
            Constants.THERMAL_ARRAYS,
            Constants.SCREEN_ARRAY,
            Constants.WAKE_ARRAY,
            Constants.SOUND_ARRAY,
            Constants.MISC_ARRAY
        };
        String file_name = "";
        String arrays_one = "";
        String arrays_one_formated = "";
        if (date)
            file_name = "kernel_state" + getDate() + ".txt";
        else
            file_name = "kernel_state.txt";
        // loop through each array in the constants file. These contain all the other arrays.
        // have to do this once for the 1d arrays and again for the 2 arrays
        try {
            File sysfsdump = new File(path, file_name);
            if (sysfsdump.exists())
                sysfsdump.delete();
            FileWriter output = new FileWriter(sysfsdump);
            for (int i = 0; i < arrays.length; i++) {
                for (int a = 0; a < arrays[i].length; a++) {
                    arrays_one = arrays[i][a];
                    if (arrays_one.contains("cpu") && arrays_one.contains("%d")) {
                        arrays_one_formated = String.format(Locale.US, arrays_one, 0);
                        if (Utils.existFile(arrays_one_formated) && !arrays_one_formated.contains("/system/bin"))
                            output.write(sysfsrecord(arrays_one_formated));
                    } else if (Utils.existFile(arrays_one) && !arrays_one.contains("/system/bin"))
                        output.write(sysfsrecord(arrays_one));
                }
            }
            for (int i = 0; i < twodarrays.length; i++) {
                for (int a = 0; a < twodarrays[i].length; a++) {
                    for (int b = 0; b < twodarrays[i][a].length; b++) {
                        if (Utils.existFile(twodarrays[i][a][b]) && !twodarrays[i][a][b].contains("/system/bin"))
                            output.write(sysfsrecord(twodarrays[i][a][b]));
                    }
                }
            }
            output.flush();
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String sysfsrecord(String file) {
        String ret = "";
        RootFile sysfspath = new RootFile(file);
        if (sysfspath.isDirectory()) return ret = sysfspathIsdirectory(file);
        else {
            Log.i(Constants.TAG, "Path: " + file + " | Value: " + Utils.readFile(file));
            return ret = ret + "Path: " + file + " | Value: " + Utils.readFile(file) + "\n";
        }
    }

    private String sysfspathIsdirectory(String file) {
        String ret = "";
        List < RootFile > directoryListing;
        Log.i(Constants.TAG, "Dir: " + file);
        String path = file;
        ret = ret + "Dir: " + path + "\n";
        RootFile dir = new RootFile(path);
        directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (RootFile child: directoryListing) {
                if (!child.isDirectory()) {
                    Log.i(Constants.TAG, "File: " + child + " | Value: " + Utils.readFile(child.toString()));
                    ret = ret + "File: " + child + " | Value: " + Utils.readFile(child.toString()) + "\n";
                } else
                    ret = ret + sysfspathIsdirectory(child.toString());
            }
        }
        return ret;
    }

    private static void KernelChanges(String path, boolean date, Context context) {
        String file_name;
        if (date)
            file_name = path + "kernel_changes" + getDate() + ".txt";
        else
            file_name = path + "kernel_changes.txt";
        RootUtils.runCommand("echo " + "'" + listcommands(context) + "'" + " > " + file_name);
    }

    private static String listcommands(Context context) {
        CommandDB commandDB = new CommandDB(context);
        List < CommandDB.CommandItem > commandItems = commandDB.getAllCommands();
        final List < String > applys = new ArrayList < > ();
        List < String > commands = new ArrayList < > ();

        Class[] classes = {
            BatteryFragment.class,
            CPUFragment.class,
            CPUHotplugFragment.class,
            CPUVoltageFragment.class,
            EntropyFragment.class,
            GPUFragment.class,
            IOFragment.class,
            KSMFragment.class,
            LMKFragment.class,
            MiscFragment.class,
            RamFragment.class,
            ScreenFragment.class,
            SoundFragment.class,
            ThermalFragment.class,
            VMFragment.class,
            WakeFragment.class,
            WakeLockFragment.class
        };

        for (Class mClass: classes) {
            if (Utils.getBoolean(mClass.getSimpleName() + "onboot", false, context)) {
                applys.addAll(Utils.getApplys(mClass));
            }
        }

        if (applys.size() > 0) {
            for (CommandDB.CommandItem commandItem: commandItems)
                for (String sys: applys) {
                    String path = commandItem.getPath();
                    if ((sys.contains(path) || path.contains(sys))) {
                        String command = commandItem.getCommand();
                        if (commands.indexOf(command) < 0)
                            commands.add(command);
                    }
                }
        }

        if (commands.size() > 0) {
            final String allcommands = android.text.TextUtils.join("\n", commands);
            return allcommands;
        }
        return "No changes";
    }

    private void LogsonClick(int position) {
        new GetPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE).ask(new GetPermission.PermissionCallBack() {
            @Override
            public void granted() {
                if (FolderExit()) {
                    if (position == 1) {
                        if (!Misc.isLoggerActive()) Utils.toast(getString(R.string.logcat_disable_zip), getActivity(), Toast.LENGTH_LONG);
                        new Execute(getActivity()).execute("zip");
                    } else if (position == 2) {
                        if (!Misc.isLoggerActive()) Utils.toast(getString(R.string.logcat_disable_zip), getActivity(), Toast.LENGTH_LONG);
                        GrepLogs();
                    } else if (position == 3) {
                        if (!Misc.isLoggerActive()) Utils.toast(getString(R.string.logcat_disable_summary), getActivity(), Toast.LENGTH_LONG);
                        else logs(logcatC, log_folder, "logcat" + getDate());
                    } else if (position == 4) {
                        if (!Misc.isLoggerActive()) Utils.toast(getString(R.string.logcat_disable_summary), getActivity(), Toast.LENGTH_LONG);
                        else logs(radioC, log_folder, "radio" + getDate());
                    } else if (position == 5) {
                        if (!Misc.isLoggerActive()) Utils.toast(getString(R.string.logcat_disable_summary), getActivity(), Toast.LENGTH_LONG);
                        else logs(eventsC, log_folder, "events" + getDate());
                    } else if (position == 6) new Execute(getActivity()).execute("lastdmesg");
                    else if (position == 7) logs(dmesgC, log_folder, "dmesg" + getDate());
                    else if (position == 8) logs(getpropC, log_folder, "getprop" + getDate());
                    else if (position == 9) new Execute(getActivity()).execute("kernel_changes");
                }
            }

            @Override
            public void denied() {
                Utils.request_writeexternalstorage(getActivity());
            }
        });
    }

    private boolean FolderExit() {
        if (!Utils.existFile(log_folder)) {
            RootFile dir = new RootFile(log_folder);
            dir.mkdir();

            if (!Utils.existFile(log_folder)) {
                Utils.toast(getString(R.string.log_folder_error), getActivity(), Toast.LENGTH_LONG);
                return false;
            } else return true;
        } else return true;
    }
}
