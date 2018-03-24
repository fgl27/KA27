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

import android.os.Bundle;
import android.util.Log;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.EditTextCardView;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by willi on 02.01.15.
 */
public class MiscFragment extends RecyclerViewFragment implements PopupCardView.DPopupCard.OnDPopupCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener, EditTextCardView.DEditTextCard.OnDEditTextCardListener {

    private SeekBarCardView.DSeekBarCard mVibrationCard;

    private SwitchCardView.DSwitchCard mLoggerEnableCard;

    private SwitchCardView.DSwitchCard mSELinuxCard;

    private SwitchCardView.DSwitchCard mCrcCard;

    private SwitchCardView.DSwitchCard mFsyncCard;
    private SwitchCardView.DSwitchCard mDynamicFsyncCard;

    private SwitchCardView.DSwitchCard mMotoTouchxCard;

    private SwitchCardView.DSwitchCard mGentleFairSleepersCard;

    private PopupCardView.DPopupCard mPowerSuspendModeCard;
    private SwitchCardView.DSwitchCard mOldPowerSuspendStateCard;
    private SeekBarCardView.DSeekBarCard mNewPowerSuspendStateCard;

    private PopupCardView.DPopupCard mTcpCongestionCard;
    private EditTextCardView.DEditTextCard mHostnameCard;

    private SwitchCardView.DSwitchCard mEnableADBOverWifiCard;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        selinuxInit();
        MotoTouchxInit();
        if (Misc.hasLoggerEnable()) loggerInit();

        if (Misc.hasCrc()) crcInit();
        fsyncInit();
        if (Misc.hasVibration()) vibrationInit();
        if (Misc.hasGentleFairSleepers()) gentlefairsleepersInit();
        if (Misc.hasPowerSuspend()) powersuspendInit();
        networkInit();
        Update();
    }

    private void selinuxInit() {
        mSELinuxCard = new SwitchCardView.DSwitchCard();
        mSELinuxCard.setTitle(getString(R.string.se_linux));
        mSELinuxCard.setDescription(getString(R.string.se_linux_summary) + " " + Misc.getSELinuxStatus());
        mSELinuxCard.setChecked(Misc.isSELinuxActive());
        mSELinuxCard.setOnDSwitchCardListener(this);

        addView(mSELinuxCard);

        DDivider mMiscCard = new DDivider();
        mMiscCard.setText("Misc Settings");
        addView(mMiscCard);
    }

    private void vibrationInit() {
        List < String > list = new ArrayList < > ();
        for (int i = 0; i < 101; i++)
            list.add(i + "%");

        int max = Misc.getVibrationMax();
        int min = Misc.getVibrationMin();
        float offset = (max - min) / (float) 101;

        mVibrationCard = new SeekBarCardView.DSeekBarCard(list);
        mVibrationCard.setTitle(getString(R.string.vibration_strength));
        mVibrationCard.setProgress(Math.round((Misc.getCurVibration() - min) / offset));
        mVibrationCard.setOnDSeekBarCardListener(this);

        addView(mVibrationCard);
    }

    private void loggerInit() {
        mLoggerEnableCard = new SwitchCardView.DSwitchCard();
        mLoggerEnableCard.setTitle(getString(R.string.android_logger));
        mLoggerEnableCard.setDescription(getString(R.string.android_logger_summary));
        mLoggerEnableCard.setChecked(Misc.isLoggerActive());
        mLoggerEnableCard.setOnDSwitchCardListener(this);

        addView(mLoggerEnableCard);
    }

    private void crcInit() {
        mCrcCard = new SwitchCardView.DSwitchCard();
        mCrcCard.setTitle(getString(R.string.crc));
        mCrcCard.setDescription(getString(R.string.crc_summary));
        mCrcCard.setChecked(Misc.isCrcActive());
        mCrcCard.setOnDSwitchCardListener(this);

        addView(mCrcCard);
    }

    private void fsyncInit() {
        if (Misc.hasFsync()) {
            mFsyncCard = new SwitchCardView.DSwitchCard();
            mFsyncCard.setTitle(getString(R.string.fsync));
            mFsyncCard.setDescription(getString(R.string.fsync_summary));
            mFsyncCard.setChecked(Misc.isFsyncActive());
            mFsyncCard.setOnDSwitchCardListener(this);

            addView(mFsyncCard);
        }

        if (Misc.hasDynamicFsync()) {
            mDynamicFsyncCard = new SwitchCardView.DSwitchCard();
            mDynamicFsyncCard.setTitle(getString(R.string.dynamic_fsync));
            mDynamicFsyncCard.setDescription(getString(R.string.dynamic_fsync_summary));
            mDynamicFsyncCard.setChecked(Misc.isDynamicFsyncActive());
            mDynamicFsyncCard.setOnDSwitchCardListener(this);

            addView(mDynamicFsyncCard);
        }
    }

    private void MotoTouchxInit() {
        if (Misc.hasMotoTouchx()) {
            mMotoTouchxCard = new SwitchCardView.DSwitchCard();
            mMotoTouchxCard.setTitle(getString(R.string.moto_touchx));
            mMotoTouchxCard.setDescription(getString(R.string.moto_touchx_summary));
            mMotoTouchxCard.setChecked(Misc.isMotoTouchxActive());
            mMotoTouchxCard.setOnDSwitchCardListener(this);

            addView(mMotoTouchxCard);
        }
    }

    private void gentlefairsleepersInit() {
        mGentleFairSleepersCard = new SwitchCardView.DSwitchCard();
        mGentleFairSleepersCard.setTitle(getString(R.string.gentlefairsleepers));
        mGentleFairSleepersCard.setDescription(getString(R.string.gentlefairsleepers_summary));
        mGentleFairSleepersCard.setChecked(Misc.isGentleFairSleepersActive());
        mGentleFairSleepersCard.setOnDSwitchCardListener(this);

        addView(mGentleFairSleepersCard);
    }

    private void powersuspendInit() {
        if (Misc.hasPowerSuspendMode()) {
            mPowerSuspendModeCard = new PopupCardView.DPopupCard(new ArrayList < > (
                Arrays.asList(getResources().getStringArray(R.array.powersuspend_items))));
            mPowerSuspendModeCard.setTitle(getString(R.string.power_suspend_mode));
            mPowerSuspendModeCard.setDescription(getString(R.string.power_suspend_mode_summary));
            mPowerSuspendModeCard.setItem(Misc.getPowerSuspendMode());
            mPowerSuspendModeCard.setOnDPopupCardListener(this);

            addView(mPowerSuspendModeCard);
        }

        if (Misc.hasOldPowerSuspendState()) {
            mOldPowerSuspendStateCard = new SwitchCardView.DSwitchCard();
            mOldPowerSuspendStateCard.setTitle(getString(R.string.power_suspend_state));
            mOldPowerSuspendStateCard.setDescription(getString(R.string.power_suspend_state_summary));
            mOldPowerSuspendStateCard.setChecked(Misc.isOldPowerSuspendStateActive());
            mOldPowerSuspendStateCard.setOnDSwitchCardListener(this);

            addView(mOldPowerSuspendStateCard);
        }

        if (Misc.hasNewPowerSuspendState()) {
            List < String > list = new ArrayList < > ();
            for (int i = 0; i < 3; i++)
                list.add(String.valueOf(i));

            mNewPowerSuspendStateCard = new SeekBarCardView.DSeekBarCard(list);
            mNewPowerSuspendStateCard.setTitle(getString(R.string.power_suspend_state));
            mNewPowerSuspendStateCard.setDescription(getString(R.string.power_suspend_state_summary));
            mNewPowerSuspendStateCard.setProgress(Misc.getNewPowerSuspendState());
            mNewPowerSuspendStateCard.setOnDSeekBarCardListener(this);

            addView(mNewPowerSuspendStateCard);
        }

    }

    private void networkInit() {
        DDivider mNetworkDividerCard = new DDivider();
        mNetworkDividerCard.setText(getString(R.string.network));
        addView(mNetworkDividerCard);

        try {
            mTcpCongestionCard = new PopupCardView.DPopupCard(Misc.getTcpAvailableCongestions(true));
            mTcpCongestionCard.setTitle(getString(R.string.tcp));
            mTcpCongestionCard.setDescription(getString(R.string.tcp_summary));
            mTcpCongestionCard.setItem(Misc.getCurTcpCongestion());
            mTcpCongestionCard.setOnDPopupCardListener(this);

            addView(mTcpCongestionCard);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Failed to read TCP");
        }

        String hostname = Misc.getHostname();
        mHostnameCard = new EditTextCardView.DEditTextCard();
        mHostnameCard.setTitle(getString(R.string.hostname));
        mHostnameCard.setDescription(hostname);
        mHostnameCard.setValue(hostname);
        mHostnameCard.setOnDEditTextCardListener(this);

        addView(mHostnameCard);


        mEnableADBOverWifiCard = new SwitchCardView.DSwitchCard();
        mEnableADBOverWifiCard.setTitle(getString(R.string.adb_over_wifi));
        mEnableADBOverWifiCard.setChecked(Misc.isADBOverWifiActive());
        mEnableADBOverWifiCard.setOnDSwitchCardListener(this);

        addView(mEnableADBOverWifiCard);
    }

    @Override
    public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mTcpCongestionCard)
            Misc.setTcpCongestion(Misc.getTcpAvailableCongestions(true).get(position), getActivity());
        else if (dPopupCard == mPowerSuspendModeCard)
            Misc.setPowerSuspendMode(position, getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mVibrationCard) {
            int max = Misc.getVibrationMax();
            int min = Misc.getVibrationMin();
            float offset = (max - min) / (float) 101;
            Misc.setVibration(Math.round(offset * position) + min, getActivity());

            // Vibrate
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        Utils.vibrate(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else if (dSeekBarCard == mNewPowerSuspendStateCard) {
            if (Misc.getPowerSuspendMode() == 1) {
                Misc.setNewPowerSuspend(position, getActivity());
            } else dSeekBarCard.setProgress(Misc.getNewPowerSuspendState());
        }
    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mSELinuxCard) Misc.activateSELinux(checked, getActivity());
        else if (dSwitchCard == mLoggerEnableCard) Misc.activateLogger(checked, getActivity());
        else if (dSwitchCard == mCrcCard) Misc.activateCrc(checked, getActivity());
        else if (dSwitchCard == mFsyncCard) Misc.activateFsync(checked, getActivity());
        else if (dSwitchCard == mDynamicFsyncCard) Misc.activateDynamicFsync(checked, getActivity());
        else if (dSwitchCard == mMotoTouchxCard) Misc.activateMotoTouchx(checked, getActivity());
        else if (dSwitchCard == mGentleFairSleepersCard) Misc.activateGentleFairSleepers(checked, getActivity());
        else if (dSwitchCard == mOldPowerSuspendStateCard) {
            if (Misc.getPowerSuspendMode() == 1) {
                Misc.activateOldPowerSuspend(checked, getActivity());
            } else dSwitchCard.setChecked(Misc.isOldPowerSuspendStateActive());
        } else if (dSwitchCard == mEnableADBOverWifiCard) Misc.activateADBOverWifi(checked, getActivity());

    }

    @Override
    public void onApply(EditTextCardView.DEditTextCard dEditTextCard, String value) {
        dEditTextCard.setDescription(value);
        if (dEditTextCard == mHostnameCard) Misc.setHostname(value, getActivity());
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    private void Update() {
        if (mSELinuxCard != null) mSELinuxCard.setDescription(getString(R.string.se_linux_summary) + " " + Misc.getSELinuxStatus());
        if (mEnableADBOverWifiCard != null) mEnableADBOverWifiCard.setDescription(Misc.isADBOverWifiActive() ? getString(R.string.adb_over_wifi_connect_summary) + Misc.getIpAddr(getActivity()) + ":5555" : getString(R.string.adb_over_wifi_summary));
    }

}
