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
import android.os.AsyncTask;
import android.os.Bundle;

import com.grarak.kerneladiutor.MainActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.kerneladiutor.library.root.RootUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zeroturnaround.zip.ZipUtil;
/**
 * Created by willi on 09.03.15. Mod by Felipe L 09.26.16
 */
public class LogsFragment extends RecyclerViewFragment {

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        String temp_path = ("/sdcard/KA_Logs/");
        if (!Utils.existFile(temp_path)) {
            File dir = new File(temp_path);
            dir.mkdir();
        }

        debuggingInit();
    }

    private void debuggingInit() {
        DDivider mLogsDividerCard = new DDivider();
        mLogsDividerCard.setText(getString(R.string.logs));
        mLogsDividerCard.setDescription(getString(R.string.logs_summary));
        addView(mLogsDividerCard);

        CardViewItem.DCardView mAllLogsCard = new CardViewItem.DCardView();
        mAllLogsCard.setTitle(getString(R.string.zip_log));
        mAllLogsCard.setDescription(String.format(getString(R.string.zip_log_summary), getDate()));
        mAllLogsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                String temp_path = ("/tmp/tmp/");
                if (!Utils.existFile(temp_path)) {
                    File dir = new File(temp_path);
                    dir.mkdir();
                }
                logs("logcat -d", temp_path, "logcat");
                logs("logcat -b radio -v time -d ", temp_path, "radio");
                logs("logcat -b events -v time -d ", temp_path, "events");
                logs("dmesg", temp_path, "dmesg");
                logs("getprop", temp_path, "getprop");
                new Execute().execute("zip");
            }
        });

        addView(mAllLogsCard);

        DDivider mIndLogsDividerCard = new DDivider();
        mIndLogsDividerCard.setText(getString(R.string.ind_logs));
        addView(mIndLogsDividerCard);

        CardViewItem.DCardView mLogcatCard = new CardViewItem.DCardView();
        mLogcatCard.setTitle(getString(R.string.logcat));
        mLogcatCard.setDescription(String.format(getString(R.string.logcat_summary), getDate()));
        mLogcatCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("logcat -d", "/sdcard/KA_Logs/", "logcat" + getDate());
            }
        });

        addView(mLogcatCard);

        CardViewItem.DCardView mLogRadioCard = new CardViewItem.DCardView();
        mLogRadioCard.setTitle(getString(R.string.log_radio));
        mLogRadioCard.setDescription(String.format(getString(R.string.log_radio_summary), getDate()));
        mLogRadioCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("logcat -b radio -v time -d ", "/sdcard/KA_Logs/", "radio" + getDate());
            }
        });

        addView(mLogRadioCard);

        CardViewItem.DCardView mLogEventsCard = new CardViewItem.DCardView();
        mLogEventsCard.setTitle(getString(R.string.log_events));
        mLogEventsCard.setDescription(String.format(getString(R.string.log_events_summary), getDate()));
        mLogEventsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("logcat -b events -v time -d ", "/sdcard/KA_Logs/", "events" + getDate());
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
                    logs("cat " + lastKmsg.toString(), "/sdcard/KA_Logs/", "last_kmsg" + getDate());
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
                logs("dmesg", "/sdcard/KA_Logs/", "dmesg" + getDate());
            }
        });

        addView(mDmesgCard);

        CardViewItem.DCardView mGetPropCard = new CardViewItem.DCardView();
        mGetPropCard.setTitle(getString(R.string.get_prop));
        mGetPropCard.setDescription(String.format(getString(R.string.get_prop_summary), getDate()));
        mGetPropCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("getprop", "/sdcard/KA_Logs/", "getprop" + getDate());
            }
        });

        addView(mGetPropCard);
    }

    private void logs(String log, String path, String file) {
        new Execute().execute(log + " > " + path + file + ".txt");
    }

    public static String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH:mm");
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
            if (params[0].equals("zip"))
                ZipUtil.pack(new File("/tmp/tmp"), new File("/sdcard/KA_Logs/logs" + getDate() + ".zip"));
            else RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

}
