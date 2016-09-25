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

package com.grarak.kerneladiutor.fragments.other;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.grarak.kerneladiutor.MainActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.services.BootService;
import com.grarak.kerneladiutor.services.ProfileTileReceiver;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.database.ProfileDB;
import com.kerneladiutor.library.root.RootUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 09.03.15.
 */
public class SettingsFragment extends RecyclerViewFragment {

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

	String ppath = ("/sdcard/KA_Logs/");
	if (!Utils.existFile(ppath)) {
	    File dir = new File(ppath);
	    dir.mkdir();
	}

        if (!Resources.getSystem().getConfiguration().locale.getLanguage().startsWith("en") && !Utils.isTV(getActivity()))
            forceenglishlanguageInit();
        darkthemeInit();
        if (Utils.hasCMSDK()) profileTileInit();
        applyonbootInit();
        perappInit();
        debuggingInit();
        securityInit();
        showSectionsInit();
    }

    private void darkthemeInit() {
        SwitchCardView.DSwitchCard mDarkthemeCard = new SwitchCardView.DSwitchCard();
        mDarkthemeCard.setDescription(getString(R.string.dark_theme));
        mDarkthemeCard.setChecked(Utils.DARKTHEME);
        mDarkthemeCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("darktheme", checked, getActivity());
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });

        addView(mDarkthemeCard);
    }

    private void forceenglishlanguageInit() {
        SwitchCardView.DSwitchCard mForceEnglishLanguageCard = new SwitchCardView.DSwitchCard();
        mForceEnglishLanguageCard.setDescription(getString(R.string.force_english_language));
        mForceEnglishLanguageCard.setChecked(Utils.getBoolean("forceenglish", false, getActivity()));
        mForceEnglishLanguageCard.setOnDSwitchCardListener(
                new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean("forceenglish", checked, getActivity());
                        if (!checked)
                            Utils.setLocale(Resources.getSystem().getConfiguration().locale.getLanguage(), getActivity());
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                });

        addView(mForceEnglishLanguageCard);
    }

    private void profileTileInit() {
        SwitchCardView.DSwitchCard mShowProfileTileCard = new SwitchCardView.DSwitchCard();
        mShowProfileTileCard.setDescription(getString(R.string.show_profile_tile));
        mShowProfileTileCard.setChecked(Utils.getBoolean("profiletile", true, getActivity()));
        mShowProfileTileCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("profiletile", checked, getActivity());
                ProfileTileReceiver.publishProfileTile(checked ? new ProfileDB(getActivity())
                        .getAllProfiles() : null, getActivity());
            }
        });

        addView(mShowProfileTileCard);
    }

    private void applyonbootInit() {
        DDivider mApplyonBootDividerCard = new DDivider();
        mApplyonBootDividerCard.setText(getString(R.string.apply_on_boot));

        addView(mApplyonBootDividerCard);

        if (!Utils.isTV(getActivity())) {
            SwitchCardView.DSwitchCard mHideApplyOnBootCard = new SwitchCardView.DSwitchCard();
            mHideApplyOnBootCard.setTitle(getString(R.string.hide_apply_on_boot));
            mHideApplyOnBootCard.setDescription(getString(R.string.hide_apply_on_boot_summary));
            mHideApplyOnBootCard.setChecked(Utils.getBoolean("hideapplyonboot", true, getActivity()));
            mHideApplyOnBootCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                @Override
                public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                    Utils.saveBoolean("hideapplyonboot", checked, getActivity());
                }
            });

            addView(mHideApplyOnBootCard);
        }

        final List<String> list = new ArrayList<>();
        list.add(0 + " " + getString(R.string.sec));
        for (int i = 5; i < 421; i *= 2)
            list.add(i + " " + getString(R.string.sec));

        PopupCardView.DPopupCard mApplyonbootDelayCard = new PopupCardView.DPopupCard(list);
        mApplyonbootDelayCard.setDescription(getString(R.string.delay));
        mApplyonbootDelayCard.setItem(Utils.getInt("applyonbootdelay", 0, getActivity()) + " " + getString(R.string.sec));
        mApplyonbootDelayCard.setOnDPopupCardListener(new PopupCardView.DPopupCard.OnDPopupCardListener() {
            @Override
            public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
                Utils.saveInt("applyonbootdelay", Utils.stringToInt(list.get(position)
                        .replace(" " + getString(R.string.sec), "")), getActivity());
            }
        });

        addView(mApplyonbootDelayCard);

        SwitchCardView.DSwitchCard mApplyonbootNotificationCard = new SwitchCardView.DSwitchCard();
        mApplyonbootNotificationCard.setTitle(getString(R.string.notification));
        mApplyonbootNotificationCard.setDescription(getString(R.string.notification_summary));
        mApplyonbootNotificationCard.setChecked(Utils.getBoolean("applyonbootnotification", true, getActivity()));
        mApplyonbootNotificationCard.setOnDSwitchCardListener(
                new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean("applyonbootnotification", checked, getActivity());
                    }
                });

        addView(mApplyonbootNotificationCard);

        SwitchCardView.DSwitchCard mShowToastCard = new SwitchCardView.DSwitchCard();
        mShowToastCard.setDescription(getString(R.string.show_toast));
        mShowToastCard.setChecked(Utils.getBoolean("applyonbootshowtoast", true, getActivity()));
        mShowToastCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("applyonbootshowtoast", checked, getActivity());
            }
        });

        addView(mShowToastCard);

        CardViewItem.DCardView mTestCard = new CardViewItem.DCardView();
        mTestCard.setTitle(getString(R.string.test));
        mTestCard.setDescription(getString(R.string.test_summary));
        mTestCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                boolean applyonbootenabled = false;
                for (DAdapter.DView item : Constants.ITEMS)
                    if (item.getFragment() != null && Utils.getBoolean(item.getFragment().getClass().getSimpleName()
                            + "onboot", false, getActivity())) {
                        applyonbootenabled = true;
                        break;
                    }
                if (applyonbootenabled)
                    getActivity().startService(new Intent(getActivity(), BootService.class));
                else Utils.toast(getString(R.string.enable_apply_on_boot_first), getActivity());
            }
        });

        addView(mTestCard);
    }

    public static String getDate() {
        DateFormat dateformate = new SimpleDateFormat("MMM_dd_yyyy_HH:mm");
        Date date = new Date();
        String Final_Date = "_" + dateformate.format(date);
        return Final_Date;
    }

    private void debuggingInit() {
        DDivider mDebuggingDividerCard = new DDivider();
        mDebuggingDividerCard.setText(getString(R.string.debugging));

        addView(mDebuggingDividerCard);

        CardViewItem.DCardView mAllLogsCard = new CardViewItem.DCardView();
        mAllLogsCard.setTitle(getString(R.string.full_logcat));
        mAllLogsCard.setDescription(String.format(getString(R.string.full_logcat_summary), getDate()));
        mAllLogsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {

                logs("logcat -d ", "/", "logcat");
                logs("logcat -b radio -v time -d ", "/", "radio");
                logs("logcat -b events -v time -d ", "/", "events");
                logs("dmesg", "/", "dmesg");
                logs("getprop", "/", "getprop");
                new Execute().execute("zip -r9 /sdcard/KA_Logs/logs" + getDate() + ".zip logcat.txt radio.txt events.txt dmesg.txt getprop.txt");
            }
        });

        addView(mAllLogsCard);

        CardViewItem.DCardView mLogcatCard = new CardViewItem.DCardView();
        mLogcatCard.setTitle(getString(R.string.logcat));
        mLogcatCard.setDescription(String.format(getString(R.string.logcat_summary), getDate()));
        mLogcatCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("logcat -d ", "/sdcard/KA_Logs/", "logcat");
            }
        });

        addView(mLogcatCard);

        CardViewItem.DCardView mLogRadioCard = new CardViewItem.DCardView();
        mLogRadioCard.setTitle(getString(R.string.log_radio));
        mLogRadioCard.setDescription(String.format(getString(R.string.log_radio_summary), getDate()));
        mLogRadioCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("logcat -b radio -v time -d ", "/sdcard/KA_Logs/", "radio");
            }
        });

        addView(mLogRadioCard);

        CardViewItem.DCardView mLogEventsCard = new CardViewItem.DCardView();
        mLogEventsCard.setTitle(getString(R.string.log_events));
        mLogEventsCard.setDescription(String.format(getString(R.string.log_events_summary), getDate()));
        mLogEventsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("logcat -b events -v time -d ", "/sdcard/KA_Logs/", "events");
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
                    logs("cat " + lastKmsg.toString(), "/sdcard/KA_Logs/", "last_kmsg");
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
                logs("dmesg", "/sdcard/KA_Logs/", "dmesg");
            }
        });

        addView(mDmesgCard);

        CardViewItem.DCardView mGetPropCard = new CardViewItem.DCardView();
        mGetPropCard.setTitle(getString(R.string.get_prop));
        mGetPropCard.setDescription(String.format(getString(R.string.get_prop_summary), getDate()));
        mGetPropCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                logs("getprop", "/sdcard/KA_Logs/", "getprop");

            }
        });

        addView(mGetPropCard);
    }

    private void logs(String log, String path, String file) {
        new Execute().execute(log + " > " + path + file + getDate() + ".txt");
    }

    private class Execute extends AsyncTask<String, Void, Void> {
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
        protected Void doInBackground(String... params) {
            RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    private void securityInit() {
        DDivider mSecurityDividerCard = new DDivider();
        mSecurityDividerCard.setText(getString(R.string.security));

        addView(mSecurityDividerCard);

        CardViewItem.DCardView mSetPasswordCard = new CardViewItem.DCardView();
        mSetPasswordCard.setTitle(getString(R.string.set_password));
        mSetPasswordCard.setDescription(getString(R.string.set_password_summary));
        mSetPasswordCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                editPasswordDialog(Utils.getString("password", "", getActivity()));
            }
        });

        addView(mSetPasswordCard);

        CardViewItem.DCardView mDeletePasswordCard = new CardViewItem.DCardView();
        mDeletePasswordCard.setDescription(getString(R.string.delete_password));
        mDeletePasswordCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                deletePasswordDialog(Utils.getString("password", "", getActivity()));
            }
        });

        addView(mDeletePasswordCard);
    }

    private void editPasswordDialog(final String oldPass) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        final AppCompatEditText mOldPassword = new AppCompatEditText(getActivity());
        mOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mOldPassword.setHint(getString(R.string.old_password));
        if (!oldPass.isEmpty()) linearLayout.addView(mOldPassword);

        final AppCompatEditText mNewPassword = new AppCompatEditText(getActivity());
        mNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewPassword.setHint(getString(R.string.new_password));
        linearLayout.addView(mNewPassword);

        final AppCompatEditText mConfirmNewPassword = new AppCompatEditText(getActivity());
        mConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfirmNewPassword.setHint(getString(R.string.confirm_new_password));
        linearLayout.addView(mConfirmNewPassword);

        new AlertDialog.Builder(getActivity()).setView(linearLayout)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!oldPass.isEmpty() && !mOldPassword.getText().toString().equals(Utils.decodeString(oldPass))) {
                            Utils.toast(getString(R.string.old_password_wrong), getActivity());
                            return;
                        }

                        if (mNewPassword.getText().toString().isEmpty()) {
                            Utils.toast(getString(R.string.password_empty), getActivity());
                            return;
                        }

                        if (!mNewPassword.getText().toString().equals(mConfirmNewPassword.getText().toString())) {
                            Utils.toast(getString(R.string.password_not_match), getActivity());
                            return;
                        }

                        if (mNewPassword.getText().toString().length() > 20) {
                            Utils.toast(getString(R.string.password_too_long), getActivity());
                            return;
                        }

                        Utils.saveString("password", Utils.encodeString(mNewPassword.getText().toString()), getActivity());
                    }
                }).show();
    }

    private void deletePasswordDialog(final String password) {
        if (password.isEmpty()) {
            Utils.toast(getString(R.string.set_password_first), getActivity());
            return;
        }

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        final AppCompatEditText mPassword = new AppCompatEditText(getActivity());
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setHint(getString(R.string.password));
        linearLayout.addView(mPassword);

        new AlertDialog.Builder(getActivity()).setView(linearLayout)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mPassword.getText().toString().equals(Utils.decodeString(password))) {
                            Utils.toast(getString(R.string.password_wrong), getActivity());
                            return;
                        }

                        Utils.saveString("password", "", getActivity());
                    }
                }).show();
    }

    private void perappInit(){
        DDivider mPerAppDividerCard = new DDivider();
        mPerAppDividerCard.setText(getString(R.string.per_app));
        addView(mPerAppDividerCard);

        SwitchCardView.DSwitchCard mPerAppToastCard = new SwitchCardView.DSwitchCard();
        mPerAppToastCard.setTitle(getString(R.string.per_app_toast));
        mPerAppToastCard.setDescription(getString(R.string.per_app_toast_summary));
        mPerAppToastCard.setChecked(Utils.getBoolean("Per_App_Toast", false, getActivity()));
        mPerAppToastCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("Per_App_Toast", checked, getActivity());
            }
        });

        addView(mPerAppToastCard);

    }

    private void showSectionsInit() {
        DDivider mShowSectionsDividerCard = new DDivider();
        mShowSectionsDividerCard.setText(getString(R.string.show_sections));
        addView(mShowSectionsDividerCard);

        for (final DAdapter.DView section : Constants.ITEMS) {
            if (section.getFragment() != null
                    && !section.getFragment().getClass().getSimpleName().equals(getClass().getSimpleName())) {
                SwitchCardView.DSwitchCard mSectionCard = new SwitchCardView.DSwitchCard();
                mSectionCard.setDescription(section.getTitle());
                mSectionCard.setChecked(Utils.getBoolean(section.getFragment().getClass().getSimpleName()
                        + "visible", true, getActivity()));
                mSectionCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean(section.getFragment().getClass().getSimpleName()
                                + "visible", checked, getActivity());
                        ((MainActivity) getActivity()).setItems(SettingsFragment.this);
                    }
                });

                addView(mSectionCard);
            }
        }
    }

}
