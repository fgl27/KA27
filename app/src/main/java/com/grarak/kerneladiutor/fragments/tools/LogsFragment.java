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

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.grarak.kerneladiutor.MainActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.kerneladiutor.library.root.RootUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zeroturnaround.zip.ZipUtil;
/**
 * Created by willi on 09.03.15. Mod by Felipe L 09.26.16
 */
public class LogsFragment extends RecyclerViewFragment {

    private static String final_logcat;
    private static String final_log_radio;
    private static String final_log_events;
    private static String final_driver_message;
    private static String final_get_prop;
    private static String final_grep;

    private final String logcatC = "logcat -d ";
    private final String radioC = "logcat  -b radio -v time -d ";
    private final String eventsC = "logcat -b events -v time -d ";
    private final String dmesgC = "dmesg ";
    private final String getpropC = "getprop ";
    private final String log_folder = "/sdcard/KA_Logs/";
    private final String grep = " | grep -i ";

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        if (!Utils.existFile(log_folder)) {
            File dir = new File(log_folder);
            dir.mkdir();
        }

        debuggingInit();
    }

    private void debuggingInit() {
        DDivider mLogsDividerCard = new DDivider();
        mLogsDividerCard.setText(getString(R.string.logs));
        addView(mLogsDividerCard);

        CardViewItem.DCardView mAllLogsCard = new CardViewItem.DCardView();
        mAllLogsCard.setTitle(getString(R.string.zip_log));
        mAllLogsCard.setDescription(String.format(getString(R.string.zip_log_summary), getDate()));
        mAllLogsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                String log_temp_folder = log_folder + "/tmpziplog/";
                if (!Utils.existFile(log_temp_folder)) {
                    File dir = new File(log_temp_folder);
                    dir.mkdir();
                }
                logs(logcatC, log_temp_folder, "logcat");
                logs(radioC, log_temp_folder, "radio");
                logs(eventsC, log_temp_folder, "events");
                logs(dmesgC, log_temp_folder, "dmesg");
                logs(getpropC, log_temp_folder, "getprop");
                new Execute().execute("zip");
                new Execute().execute("rm -rf " + log_temp_folder);
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
                GrepLogs();
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
                logs(logcatC, log_folder, "logcat" + getDate());
            }
        });

        addView(mLogcatCard);

        CardViewItem.DCardView mLogRadioCard = new CardViewItem.DCardView();
        mLogRadioCard.setTitle(getString(R.string.log_radio));
        mLogRadioCard.setDescription(String.format(getString(R.string.log_radio_summary), getDate()));
        mLogRadioCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs(radioC, log_folder, "radio" + getDate());
            }
        });

        addView(mLogRadioCard);

        CardViewItem.DCardView mLogEventsCard = new CardViewItem.DCardView();
        mLogEventsCard.setTitle(getString(R.string.log_events));
        mLogEventsCard.setDescription(String.format(getString(R.string.log_events_summary), getDate()));
        mLogEventsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs(eventsC, log_folder, "events" + getDate());
            }
        });

        addView(mLogEventsCard);

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

        CardViewItem.DCardView mDmesgCard = new CardViewItem.DCardView();
        mDmesgCard.setTitle(getString(R.string.driver_message));
        mDmesgCard.setDescription(String.format(getString(R.string.driver_message_summary), getDate()));
        mDmesgCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs(dmesgC, log_folder, "dmesg" + getDate());
            }
        });

        addView(mDmesgCard);

        CardViewItem.DCardView mGetPropCard = new CardViewItem.DCardView();
        mGetPropCard.setTitle(getString(R.string.get_prop));
        mGetPropCard.setDescription(String.format(getString(R.string.get_prop_summary), getDate()));
        mGetPropCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs(getpropC, log_folder, "getprop" + getDate());
            }
        });

        addView(mGetPropCard);
    }

    private void logs(String log, String path, String file) {
        new Execute().execute(log + " > " + path + file + ".txt");
    }

    public static String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH_mm");
        Date date = new Date();
        String Final_Date = "_" + dateformate.format(date);
        return Final_Date;
    }

    private class Execute extends AsyncTask < String, Void, Void > {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.execute));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String...params) {
            // ZipUtil doesnot understand folder name that end with /
            if (params[0].equals("zip"))
                ZipUtil.pack(new File(log_folder + "/tmpziplog"), new File(log_folder + "logs" + getDate() + ".zip"));
            else RootUtils.runCommand(params[0]);
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
        checkBoxLayout.addView(logcat);

        final AppCompatCheckBox log_radio = new AppCompatCheckBox(getActivity());
        log_radio.setText(getString(R.string.log_radio));
        checkBoxLayout.addView(log_radio);

        final AppCompatCheckBox log_events = new AppCompatCheckBox(getActivity());
        log_events.setText(getString(R.string.log_events));
        checkBoxLayout.addView(log_events);

        final AppCompatCheckBox driver_message = new AppCompatCheckBox(getActivity());
        driver_message.setText(getString(R.string.driver_message));
        checkBoxLayout.addView(driver_message);

        final AppCompatCheckBox get_prop = new AppCompatCheckBox(getActivity());
        get_prop.setText(getString(R.string.get_prop));
        checkBoxLayout.addView(get_prop);

        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatCheckBox) logcat).setChecked(true);
                ((AppCompatCheckBox) log_radio).setChecked(true);
                ((AppCompatCheckBox) log_events).setChecked(true);
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
                    final_get_prop = "";
                    if (name.isEmpty()) {
                        Utils.toast(getString(R.string.empty_text), getActivity(), Toast.LENGTH_LONG);
                        return;
                    } else {
                        Pattern pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
                        Matcher name_test = pattern.matcher(name);
                        boolean test_special = name_test.find();
                        if (test_special) {
                            Utils.toast(getString(R.string.forbidden_special), getActivity(), Toast.LENGTH_LONG);
                            return;
                        }
                    }
                    if (((AppCompatCheckBox) logcat).isChecked()) {
                        final String logcat = RootUtils.runCommand(logcatC + grep + "'" + name + "'");
                        if (!logcat.isEmpty())
                            final_logcat = getString(R.string.logcat) + " " + getString(R.string.result) + "\n\n" + logcat + "\n";
                    }
                    if (((AppCompatCheckBox) log_radio).isChecked()) {
                        final String log_radio = RootUtils.runCommand(radioC + grep + "'" + name + "'");
                        if (!log_radio.isEmpty())
                            final_log_radio = getString(R.string.log_radio) + " " + getString(R.string.result) + "\n\n" + log_radio + "\n";
                    }
                    if (((AppCompatCheckBox) log_events).isChecked()) {
                        final String log_events = RootUtils.runCommand(eventsC + grep + "'" + name + "'");
                        if (!log_events.isEmpty())
                            final_log_events = getString(R.string.log_events) + " " + getString(R.string.result) + "\n\n" + log_events + "\n";
                    }
                    if (((AppCompatCheckBox) driver_message).isChecked()) {
                        final String driver_message = RootUtils.runCommand(dmesgC + grep + "'" + name + "'");
                        if (!driver_message.isEmpty())
                            final_driver_message = getString(R.string.driver_message) + " " + getString(R.string.result) + "\n\n" + driver_message + "\n";
                    }
                    if (((AppCompatCheckBox) get_prop).isChecked()) {
                        final String get_prop = RootUtils.runCommand(getpropC + grep + "'" + name + "'");
                        if (!get_prop.isEmpty())
                            final_get_prop = getString(R.string.get_prop) + " " + getString(R.string.result) + "\n\n" + get_prop + "\n";
                    }
                    if (!(((AppCompatCheckBox) logcat).isChecked()) && !(((AppCompatCheckBox) log_radio).isChecked()) &&
                        !(((AppCompatCheckBox) log_events).isChecked()) && !(((AppCompatCheckBox) driver_message).isChecked()) &&
                        !(((AppCompatCheckBox) get_prop).isChecked())) {
                        Utils.toast(getString(R.string.no_log_selected), getActivity(), Toast.LENGTH_LONG);
                        return;
                    } else
                        final_grep = final_logcat + final_log_radio + final_log_events +
                        final_driver_message + final_get_prop;
                    if (!final_grep.isEmpty()) {
                        LinearLayout linearLayout = new LinearLayout(getActivity());
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.setGravity(Gravity.CENTER);
                        linearLayout.setPadding(30, 20, 30, 20);

                        TextView result_tip = new TextView(getActivity());
                        result_tip.setText(getString(R.string.result_tip));
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
                                        ClipData clip = ClipData.newPlainText("Logs", final_grep);
                                        clipboard.setPrimaryClip(clip);
                                        Utils.toast(getString(R.string.copy_clipboard_ok), getActivity(), Toast.LENGTH_LONG);
                                        return;
                                    }
                                })
                            .setPositiveButton(getString(R.string.save_to_file),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        RootUtils.runCommand("echo " + "'" + final_grep + "'" + " > " + log_folder + "grep_a_log" + getDate() + ".txt");
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
}
