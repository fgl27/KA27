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
package com.grarak.kerneladiutor.fragments.kernel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.WakeLock;
import com.grarak.kerneladiutor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 27.12.14.
 */

public class WakeLockFragment extends RecyclerViewFragment implements SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener {

    private SwitchCardView.DSwitchCard mSmb135xWakeLockCard, mBlueSleepWakeLockCard, mBlueDroidTimeWakeLockCard, mSensorIndWakeLockCard, mMsmHsicHostWakeLockCard, mTimerFdWakeLockCard, mNetlinkWakeLockCard, mWakeLockDebugCard;
    private SwitchCardView.DSwitchCard mWlanrxWakelockCard, mWlanctrlWakelockCard, mWlanWakelockCard;
    private SeekBarCardView.DSeekBarCard mWlanrxWakelockDividerCard, mMsmHsicWakelockDividerCard, mBCMDHDWakelockDividerCard;

    private CardViewItem.DCardView mTestWakeLock;
    private boolean temp_bool = false;
    private String wake_sources, test_wake, getTestWakeLock;
    private int wakelockcount = 0;

    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        wakelockDebugInit();
        wakelockInit();
    }


    private void wakelockDebugInit() {

        if (WakeLock.hasWakeLockDebug()) {
            temp_bool = WakeLock.isWakeLockDebugActive();
            mWakeLockDebugCard = new SwitchCardView.DSwitchCard();
            mWakeLockDebugCard.setTitle(getString(R.string.wakelock_debug));
            mWakeLockDebugCard.setDescription(String.format(getString(R.string.wakelock_debug_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mWakeLockDebugCard.setChecked(temp_bool);
            mWakeLockDebugCard.setOnDSwitchCardListener(this);

            addView(mWakeLockDebugCard);

            CardViewItem.DCardView wakesourceCard = new CardViewItem.DCardView();
            wakesourceCard.setTitle(getString(R.string.wakelock_list));
            wakesourceCard.setDescription(getString(R.string.wakelock_list_summary));
            wakesourceCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            WakeLock.activateWakeLockDebug(false, getActivity());
                            wakelockcount = WakeLock.getWakeLocksCount();
                            if (wakelockcount != 0) getWakeLocksAlert();
                            else Utils.toast(getString(R.string.wakelock_list_empty), getActivity(), Toast.LENGTH_LONG);
                        }
                    });
                }
            });
            addView(wakesourceCard);
        }

        if (WakeLock.hasTestWakeLock()) {

            getTestWakeLock = WakeLock.getTestWakeLock();
            mTestWakeLock = new CardViewItem.DCardView();
            mTestWakeLock.setTitle(getString(R.string.wakelock_test));
            mTestWakeLock.setDescription(String.format(getString(R.string.wakelock_test_summary), !getTestWakeLock.isEmpty() ?
                getTestWakeLock : getString(R.string.wakelock_test_empty)));
            mTestWakeLock.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            getTestWakeLock();
                        }
                    });
                }
            });
            addView(mTestWakeLock);

        }
    }

    private void wakelockInit() {
        List < DAdapter.DView > views = new ArrayList < > ();

        if (WakeLock.hasSmb135xWakeLock()) {
            temp_bool = WakeLock.isSmb135xWakeLockActive();
            mSmb135xWakeLockCard = new SwitchCardView.DSwitchCard();
            mSmb135xWakeLockCard.setTitle(getString(R.string.smb135x_wakelock));
            mSmb135xWakeLockCard.setDescription(String.format(getString(R.string.smb135x_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mSmb135xWakeLockCard.setChecked(temp_bool);
            mSmb135xWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mSmb135xWakeLockCard);
        }

        if (WakeLock.hasBlueSleepWakeLock()) {
            temp_bool = WakeLock.isBlueSleepWakeLockActive();
            mBlueSleepWakeLockCard = new SwitchCardView.DSwitchCard();
            mBlueSleepWakeLockCard.setTitle(getString(R.string.bluesleep_wakelock));
            mBlueSleepWakeLockCard.setDescription(String.format(getString(R.string.bluesleep_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mBlueSleepWakeLockCard.setChecked(temp_bool);
            mBlueSleepWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mBlueSleepWakeLockCard);
        }

        if (WakeLock.hasBlueDroidTimeWakeLock()) {
            temp_bool = WakeLock.isBlueDroidTimeWakeLockActive();
            mBlueDroidTimeWakeLockCard = new SwitchCardView.DSwitchCard();
            mBlueDroidTimeWakeLockCard.setTitle(getString(R.string.bluedroid_time_wakelock));
            mBlueDroidTimeWakeLockCard.setDescription(String.format(getString(R.string.bluedroid_time_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mBlueDroidTimeWakeLockCard.setChecked(temp_bool);
            mBlueDroidTimeWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mBlueDroidTimeWakeLockCard);
        }

        if (WakeLock.hasSensorIndWakeLock()) {
            temp_bool = WakeLock.isSensorIndWakeLockActive();
            mSensorIndWakeLockCard = new SwitchCardView.DSwitchCard();
            mSensorIndWakeLockCard.setTitle(getString(R.string.sensor_ind_wakelock));
            mSensorIndWakeLockCard.setDescription(String.format(getString(R.string.sensor_ind_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mSensorIndWakeLockCard.setChecked(temp_bool);
            mSensorIndWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mSensorIndWakeLockCard);
        }

        if (WakeLock.hasTimerFdWakeLock()) {
            temp_bool = WakeLock.isSensorIndWakeLockActive();
            mTimerFdWakeLockCard = new SwitchCardView.DSwitchCard();
            mTimerFdWakeLockCard.setTitle(getString(R.string.timerfd_wakelock));
            mTimerFdWakeLockCard.setDescription(String.format(getString(R.string.timerfd_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mTimerFdWakeLockCard.setChecked(temp_bool);
            mTimerFdWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mTimerFdWakeLockCard);
        }

        if (WakeLock.hasNetlinkWakeLock()) {
            temp_bool = WakeLock.isNetlinkWakeLockActive();
            mNetlinkWakeLockCard = new SwitchCardView.DSwitchCard();
            mNetlinkWakeLockCard.setTitle(getString(R.string.netlink_wakelock));
            mNetlinkWakeLockCard.setDescription(String.format(getString(R.string.netlink_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mNetlinkWakeLockCard.setChecked(temp_bool);
            mNetlinkWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mNetlinkWakeLockCard);
        }

        if (WakeLock.hasMsmHsicHostWakeLock()) {
            temp_bool = WakeLock.isMsmHsicHostWakeLockActive();
            mMsmHsicHostWakeLockCard = new SwitchCardView.DSwitchCard();
            mMsmHsicHostWakeLockCard.setTitle(getString(R.string.msm_hsic_host_wakelock));
            mMsmHsicHostWakeLockCard.setDescription(String.format(getString(R.string.msm_hsic_host_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mMsmHsicHostWakeLockCard.setChecked(temp_bool);
            mMsmHsicHostWakeLockCard.setOnDSwitchCardListener(this);

            views.add(mMsmHsicHostWakeLockCard);
        }

        if (WakeLock.hasMsmHsicWakelockDivider()) {
            List < String > list = new ArrayList < > ();
            list.add(getString(R.string.disabled));
            for (int i = 1; i < 17; i++) list.add((100 / i) + "%");

            mMsmHsicWakelockDividerCard = new SeekBarCardView.DSeekBarCard(list);
            mMsmHsicWakelockDividerCard.setTitle(getString(R.string.msm_hsic_wakelock_divider));
            mMsmHsicWakelockDividerCard.setProgress(WakeLock.getMsmHsicWakelockDivider());
            mMsmHsicWakelockDividerCard.setOnDSeekBarCardListener(this);

            views.add(mMsmHsicWakelockDividerCard);
        }

        if (WakeLock.hasWlanrxWakeLock()) {
            temp_bool = WakeLock.isWlanrxWakeLockActive();
            mWlanrxWakelockCard = new SwitchCardView.DSwitchCard();
            mWlanrxWakelockCard.setTitle(getString(R.string.wlan_rx_wakelock));
            mWlanrxWakelockCard.setDescription(String.format(getString(R.string.wlan_rx_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mWlanrxWakelockCard.setChecked(temp_bool);
            mWlanrxWakelockCard.setOnDSwitchCardListener(this);

            views.add(mWlanrxWakelockCard);
        }

        if (WakeLock.hasWlanctrlWakeLock()) {
            temp_bool = WakeLock.isWlanctrlWakeLockActive();
            mWlanctrlWakelockCard = new SwitchCardView.DSwitchCard();
            mWlanctrlWakelockCard.setTitle(getString(R.string.wlan_ctrl_wakelock));
            mWlanctrlWakelockCard.setDescription(String.format(getString(R.string.wlan_ctrl_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mWlanctrlWakelockCard.setChecked(temp_bool);
            mWlanctrlWakelockCard.setOnDSwitchCardListener(this);

            views.add(mWlanctrlWakelockCard);
        }

        if (WakeLock.hasWlanWakeLock()) {
            temp_bool = WakeLock.isWlanWakeLockActive();
            mWlanWakelockCard = new SwitchCardView.DSwitchCard();
            mWlanWakelockCard.setTitle(getString(R.string.wlan_wakelock));
            mWlanWakelockCard.setDescription(String.format(getString(R.string.wlan_wakelock_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mWlanWakelockCard.setChecked(temp_bool);
            mWlanWakelockCard.setOnDSwitchCardListener(this);

            views.add(mWlanWakelockCard);
        }

        if (WakeLock.hasWlanrxWakelockDivider()) {
            List < String > list = new ArrayList < > ();
            for (int i = 1; i < 17; i++) list.add((100 / i) + "%");
            list.add("0%");

            mWlanrxWakelockDividerCard = new SeekBarCardView.DSeekBarCard(list);
            mWlanrxWakelockDividerCard.setTitle(getString(R.string.wlan_rx_wakelock_divider));
            mWlanrxWakelockDividerCard.setProgress(WakeLock.getWlanrxWakelockDivider());
            mWlanrxWakelockDividerCard.setOnDSeekBarCardListener(this);

            views.add(mWlanrxWakelockDividerCard);
        }

        if (WakeLock.hasBCMDHDWakelockDivider()) {
            List < String > list = new ArrayList < > ();
            for (int i = 1; i < 9; i++) list.add(String.valueOf(i));

            mBCMDHDWakelockDividerCard = new SeekBarCardView.DSeekBarCard(list);
            mBCMDHDWakelockDividerCard.setTitle(getString(R.string.bcmdhd_wakelock_divider));
            mBCMDHDWakelockDividerCard.setProgress(WakeLock.getBCMDHDWakelockDivider());
            mBCMDHDWakelockDividerCard.setOnDSeekBarCardListener(this);

            views.add(mBCMDHDWakelockDividerCard);
        }


        if (!views.isEmpty()) {
            DDivider mWakelockDividerCard = new DDivider();
            mWakelockDividerCard.setText(getString(R.string.wakelock));
            mWakelockDividerCard.setDescription(getString(R.string.wakelocks_summary));
            addView(mWakelockDividerCard);

            addAllViews(views);
        }
    }


    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mWlanrxWakelockDividerCard)
            WakeLock.setWlanrxWakelockDivider(position, getActivity());
        else if (dSeekBarCard == mMsmHsicWakelockDividerCard)
            WakeLock.setMsmHsicWakelockDivider(position, getActivity());
        else if (dSeekBarCard == mBCMDHDWakelockDividerCard)
            WakeLock.setBCMDHDWakelockDivider(position, getActivity());

    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mSmb135xWakeLockCard) {
            mSmb135xWakeLockCard.setDescription(String.format(getString(R.string.smb135x_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateSmb135xWakeLock(checked, getActivity());
        } else if (dSwitchCard == mBlueSleepWakeLockCard) {
            mBlueSleepWakeLockCard.setDescription(String.format(getString(R.string.bluesleep_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateBlueSleepWakeLock(checked, getActivity());
        } else if (dSwitchCard == mBlueDroidTimeWakeLockCard) {
            mBlueDroidTimeWakeLockCard.setDescription(String.format(getString(R.string.bluedroid_time_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateBlueDroidTimeWakeLock(checked, getActivity());
        } else if (dSwitchCard == mSensorIndWakeLockCard) {
            mSensorIndWakeLockCard.setDescription(String.format(getString(R.string.sensor_ind_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateSensorIndWakeLock(checked, getActivity());
        } else if (dSwitchCard == mTimerFdWakeLockCard) {
            mTimerFdWakeLockCard.setDescription(String.format(getString(R.string.timerfd_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateTimerFdWakeLock(checked, getActivity());
        } else if (dSwitchCard == mNetlinkWakeLockCard) {
            mNetlinkWakeLockCard.setDescription(String.format(getString(R.string.netlink_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateNetlinkWakeLock(checked, getActivity());
        } else if (dSwitchCard == mMsmHsicHostWakeLockCard) {
            mMsmHsicHostWakeLockCard.setDescription(String.format(getString(R.string.msm_hsic_host_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateMsmHsicHostWakeLock(checked, getActivity());
        } else if (dSwitchCard == mWlanrxWakelockCard) {
            mWlanrxWakelockCard.setDescription(String.format(getString(R.string.wlan_rx_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateWlanrxWakeLock(checked, getActivity());
        } else if (dSwitchCard == mWlanctrlWakelockCard) {
            mWlanctrlWakelockCard.setDescription(String.format(getString(R.string.wlan_ctrl_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateWlanctrlWakeLock(checked, getActivity());
        } else if (dSwitchCard == mWlanWakelockCard) {
            mWlanWakelockCard.setDescription(String.format(getString(R.string.wlan_wakelock_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateWlanWakeLock(checked, getActivity());
        } else if (dSwitchCard == mWakeLockDebugCard) {
            mWakeLockDebugCard.setDescription(String.format(getString(R.string.wakelock_debug_summary), checked ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            WakeLock.activateWakeLockDebug(checked, getActivity());
        }
    }

    private void getWakeLocksAlert() {

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        TextView result_title = new TextView(getActivity());
        String result_title_content = String.format(getString(R.string.wakelock_list_result), wakelockcount) +
            " " + WakeLock.timeMs(WakeLock.getWakeLocksDuration()) + "(hh:mm:ss)" + "\n\n" + getString(R.string.wakelock_list_info);
        result_title.setText(result_title_content);
        linearLayout.addView(result_title);

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setPadding(0, 0, 0, 10);
        linearLayout.addView(scrollView);

        wake_sources = WakeLock.getWakeLocks();
        TextView final_result = new TextView(getActivity());
        final_result.setText(wake_sources);
        final_result.setTextIsSelectable(true);
        scrollView.addView(final_result);

        new AlertDialog.Builder(getActivity(),
                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
            .setTitle(getString(R.string.wakelock_list))
            .setView(linearLayout).setNegativeButton(getString(R.string.copy_clipboard),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("WakeFrag", wake_sources);
                        clipboard.setPrimaryClip(clip);
                        Utils.toast(getString(R.string.copy_clipboard_ok), getActivity(), Toast.LENGTH_LONG);
                        return;
                    }
                })
            .setPositiveButton(getString(R.string.close),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                }).show();
    }

    private void getTestWakeLock() {
        test_wake = WakeLock.getTestWakeLock();

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        final AppCompatEditText mTestWakeLockText = new AppCompatEditText(getActivity());
        mTestWakeLockText.setInputType(InputType.TYPE_CLASS_TEXT);
        if (test_wake.isEmpty()) mTestWakeLockText.setHint(getString(R.string.wakelock_test_hint));
        else mTestWakeLockText.setText(test_wake);

        linearLayout.addView(mTestWakeLockText);

        new AlertDialog.Builder(getActivity(),
                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight)).setView(linearLayout)
            .setNeutralButton(getString(R.string.wakelock_test_empty), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    WakeLock.setTestWakeLock("", getActivity());
                }
            })
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            })
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String name = mTestWakeLockText.getText().toString();

                    if (name.contains(" ")) {
                        Utils.toast(getString(R.string.wakelock_test_forbidden_special), getActivity(), Toast.LENGTH_LONG);
                        return;
                    }
                    WakeLock.setTestWakeLock(name, getActivity());
                }
            }).show();
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    public void Update() {
        if (mTestWakeLock != null) {
            getTestWakeLock = WakeLock.getTestWakeLock();
            mTestWakeLock.setDescription(String.format(getString(R.string.wakelock_test_summary), !getTestWakeLock.isEmpty() ?
                getTestWakeLock : getString(R.string.wakelock_test_empty)));
        }
        if (mWakeLockDebugCard != null) {
            temp_bool = WakeLock.isWakeLockDebugActive();
            mWakeLockDebugCard.setDescription(String.format(getString(R.string.wakelock_debug_summary), temp_bool ?
                getString(R.string.enabled) : getString(R.string.disabled)));
            mWakeLockDebugCard.setChecked(temp_bool);
        }
    }
}
