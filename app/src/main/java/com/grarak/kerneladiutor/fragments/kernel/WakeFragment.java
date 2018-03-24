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

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.Wake;
import com.grarak.kerneladiutor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

/**
 * Created by willi on 02.01.15.
 */
public class WakeFragment extends RecyclerViewFragment implements PopupCardView.DPopupCard.OnDPopupCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener {

    private SwitchCardView.DSwitchCard mDt2wCard;
    private PopupCardView.DPopupCard mS2wCard;
    private SwitchCardView.DSwitchCard mLenientCard;
    private PopupCardView.DPopupCard mT2wCard;
    private PopupCardView.DPopupCard mWakeMiscCard;
    private PopupCardView.DPopupCard mSleepMiscCard;
    private PopupCardView.DPopupCard mDt2sCard;
    private SwitchCardView.DSwitchCard[] mGestureCards;
    private SwitchCardView.DSwitchCard mCameraGestureCard;
    private SwitchCardView.DSwitchCard mPocketModeCard;

    private SeekBarCardView.DSeekBarCard mWakeTimeoutCard, mS2WTimeCard, mDT2WTimeBetweenTapsCard, mDT2WFeatherXCard, mDT2WFeatherYCard, mWakeGesturesVibStrengthCard;
    private SwitchCardView.DSwitchCard mPowerKeySuspendCard;

    private boolean isDt2wActive;
    private boolean isS2wActive;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        isDt2wActive = Wake.isDt2wActive();
        isS2wActive = (Wake.getS2wValue() > 0);

        if ((isDt2wActive || isS2wActive) && Wake.hasVibStrength()) vibstrengthInit();
        if (Wake.hasDt2w()) dt2wInit();
        if (Wake.hasS2w()) s2wInit();
        if (Wake.hasSleepMisc()) sleepMiscInit();
        if (Wake.hasCameraGesture()) cameraGestureInit();

        if (Wake.hasLenient()) lenientInit();
        if (Wake.hasT2w() && !Wake.hasDt2w()) t2wInit();
        if (Wake.hasWakeMisc()) wakeMiscInit();
        if (Wake.hasDt2s()) dt2sInit();
        if (Wake.hasGesture()) gestureInit();
        if (Wake.hasPocketMode()) pocketModeInit();
        if (Wake.hasWakeTimeout()) wakeTimeoutInit();
        if (Wake.hasPowerKeySuspend()) powerKeySuspendInit();
    }

    private void vibstrengthInit() {
        List < String > list = new ArrayList < > ();
        for (int i = 0; i < 91; i++)
            list.add(Integer.toString(i));

        mWakeGesturesVibStrengthCard = new SeekBarCardView.DSeekBarCard(list);
        mWakeGesturesVibStrengthCard.setTitle(getString(R.string.vib_strength));
        mWakeGesturesVibStrengthCard.setDescription(getString(R.string.vib_strength_summary));
        mWakeGesturesVibStrengthCard.setProgress(Wake.getvibstrength());
        mWakeGesturesVibStrengthCard.setOnDSeekBarCardListener(this);

        addView(mWakeGesturesVibStrengthCard);
    }

    private void dt2wInit() {

        mDt2wCard = new SwitchCardView.DSwitchCard();
        mDt2wCard.setTitle(getString(R.string.dt2w));
        mDt2wCard.setDescription(getString(R.string.dt2w_summary));
        mDt2wCard.setChecked(isDt2wActive);
        mDt2wCard.setOnDSwitchCardListener(this);

        addView(mDt2wCard);

        if (isDt2wActive && Wake.hasDT2WTimeBetweenTaps()) {
            List < String > list = new ArrayList < > ();
            for (int i = 1; i <= 450; i++)
                list.add(i + getString(R.string.ms));

            mDT2WTimeBetweenTapsCard = new SeekBarCardView.DSeekBarCard(list);
            mDT2WTimeBetweenTapsCard.setTitle(getString(R.string.wake_dt2w_timebetweentaps));
            mDT2WTimeBetweenTapsCard.setDescription(getString(R.string.wake_dt2w_timebetweentaps_summary));
            mDT2WTimeBetweenTapsCard.setProgress(Wake.getDT2WTimeBetweenTaps() - 1);
            mDT2WTimeBetweenTapsCard.setOnDSeekBarCardListener(this);

            addView(mDT2WTimeBetweenTapsCard);

        }

        if (isDt2wActive && Wake.hasDT2WFeatherX()) {
            List < String > list = new ArrayList < > ();
            for (int i = 1; i <= 500; i++)
                list.add(i + getString(R.string.pixel));

            mDT2WFeatherXCard = new SeekBarCardView.DSeekBarCard(list);
            mDT2WFeatherXCard.setTitle(getString(R.string.wake_dt2w_featherx));
            mDT2WFeatherXCard.setDescription(getString(R.string.wake_dt2w_featherx_summary));
            mDT2WFeatherXCard.setProgress(Wake.getDT2WFeatherX() - 1);
            mDT2WFeatherXCard.setOnDSeekBarCardListener(this);

            addView(mDT2WFeatherXCard);
        }


        if (isDt2wActive && Wake.hasDT2WFeatherY()) {
            List < String > list = new ArrayList < > ();
            for (int i = 1; i <= 500; i++)
                list.add(i + getString(R.string.pixel));

            mDT2WFeatherYCard = new SeekBarCardView.DSeekBarCard(list);
            mDT2WFeatherYCard.setTitle(getString(R.string.wake_dt2w_feathery));
            mDT2WFeatherYCard.setDescription(getString(R.string.wake_dt2w_feathery_summary));
            mDT2WFeatherYCard.setProgress(Wake.getDT2WFeatherY() - 1);
            mDT2WFeatherYCard.setOnDSeekBarCardListener(this);

            addView(mDT2WFeatherYCard);
        }
    }

    private void s2wInit() {
        mS2wCard = new PopupCardView.DPopupCard(Wake.getS2wMenu(getActivity()));
        mS2wCard.setTitle(getString(R.string.s2w_title));
        mS2wCard.setDescription(getString(R.string.s2w_summary));
        mS2wCard.setItem(Wake.getS2wValue());
        mS2wCard.setOnDPopupCardListener(this);

        addView(mS2wCard);

        if (isS2wActive && Wake.hasS2WTime()) {
            List < String > list = new ArrayList < > ();
            for (int i = 1; i <= 450; i++)
                list.add(i + getString(R.string.ms));

            mS2WTimeCard = new SeekBarCardView.DSeekBarCard(list);
            mS2WTimeCard.setTitle(getString(R.string.wake_S2W_time));
            mS2WTimeCard.setDescription(getString(R.string.wake_S2W_time_summary));
            mS2WTimeCard.setProgress(Wake.getS2WTime() - 1);
            mS2WTimeCard.setOnDSeekBarCardListener(this);

            addView(mS2WTimeCard);
        }
    }

    private void lenientInit() {
        mLenientCard = new SwitchCardView.DSwitchCard();
        mLenientCard.setTitle(getString(R.string.lenient));
        mLenientCard.setDescription(getString(R.string.lenient_summary));
        mLenientCard.setChecked(Wake.isLenientActive());
        mLenientCard.setOnDSwitchCardListener(this);

        addView(mLenientCard);
    }

    private void t2wInit() {
        mT2wCard = new PopupCardView.DPopupCard(Wake.getT2wMenu(getActivity()));
        mT2wCard.setTitle(getString(R.string.t2w));
        mT2wCard.setDescription(getString(R.string.t2w_summary));
        mT2wCard.setItem(Wake.getT2w());
        mT2wCard.setOnDPopupCardListener(this);

        addView(mT2wCard);
    }

    private void wakeMiscInit() {
        mWakeMiscCard = new PopupCardView.DPopupCard(Wake.getWakeMiscMenu(getActivity()));
        mWakeMiscCard.setDescription(getString(R.string.wake));
        mWakeMiscCard.setItem(Wake.getWakeMisc());
        mWakeMiscCard.setOnDPopupCardListener(this);

        addView(mWakeMiscCard);
    }

    private void sleepMiscInit() {
        mSleepMiscCard = new PopupCardView.DPopupCard(Wake.getSleepMiscMenu(getActivity()));
        mSleepMiscCard.setTitle(getString(R.string.s2s));
        mSleepMiscCard.setDescription(getString(R.string.sleep_summary));
        mSleepMiscCard.setItem(Wake.getSleepMisc());
        mSleepMiscCard.setOnDPopupCardListener(this);

        addView(mSleepMiscCard);
    }

    private void dt2sInit() {
        mDt2sCard = new PopupCardView.DPopupCard(Wake.getDt2sMenu(getActivity()));
        mDt2sCard.setTitle(getString(R.string.dt2s));
        mDt2sCard.setDescription(getString(R.string.dt2s_summary));
        mDt2sCard.setItem(Wake.getDt2sValue());
        mDt2sCard.setOnDPopupCardListener(this);

        addView(mDt2sCard);
    }

    private void gestureInit() {
        List < String > gestures = Wake.getGestures(getActivity());
        mGestureCards = new SwitchCardView.DSwitchCard[gestures.size()];
        for (int i = 0; i < mGestureCards.length; i++) {
            mGestureCards[i] = new SwitchCardView.DSwitchCard();
            mGestureCards[i].setDescription(gestures.get(i));
            mGestureCards[i].setChecked(Wake.isGestureActive(i));
            mGestureCards[i].setOnDSwitchCardListener(this);

            addView(mGestureCards[i]);
        }
    }

    private void cameraGestureInit() {
        mCameraGestureCard = new SwitchCardView.DSwitchCard();
        mCameraGestureCard.setTitle(getString(R.string.camera_gesture));
        mCameraGestureCard.setDescription(getString(R.string.camera_gesture_summary));
        mCameraGestureCard.setChecked(Wake.isCameraGestureActive());
        mCameraGestureCard.setOnDSwitchCardListener(this);

        addView(mCameraGestureCard);
    }

    private void pocketModeInit() {
        mPocketModeCard = new SwitchCardView.DSwitchCard();
        mPocketModeCard.setTitle(getString(R.string.pocket_mode));
        mPocketModeCard.setDescription(getString(R.string.pocket_mode_summary));
        mPocketModeCard.setChecked(Wake.isPocketModeActive());
        mPocketModeCard.setOnDSwitchCardListener(this);

        addView(mPocketModeCard);
    }

    private void wakeTimeoutInit() {
        List < String > list = new ArrayList < > ();
        list.add(getString(R.string.disabled));
        for (int i = 1; i <= Wake.getWakeTimeoutMax(); i++)
            list.add(i + getString(R.string.min));

        mWakeTimeoutCard = new SeekBarCardView.DSeekBarCard(list);
        mWakeTimeoutCard.setTitle(getString(R.string.wake_timeout));
        mWakeTimeoutCard.setDescription(getString(R.string.wake_timeout_summary));
        mWakeTimeoutCard.setProgress(Wake.getWakeTimeout());
        mWakeTimeoutCard.setOnDSeekBarCardListener(this);

        addView(mWakeTimeoutCard);
    }

    private void powerKeySuspendInit() {
        mPowerKeySuspendCard = new SwitchCardView.DSwitchCard();
        mPowerKeySuspendCard.setTitle(getString(R.string.power_key_suspend));
        mPowerKeySuspendCard.setDescription(getString(R.string.power_key_suspend_summary));
        mPowerKeySuspendCard.setChecked(Wake.isPowerKeySuspendActive());
        mPowerKeySuspendCard.setOnDSwitchCardListener(this);

        addView(mPowerKeySuspendCard);
    }

    @Override
    public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mT2wCard) Wake.setT2w(position, getActivity());
        else if (dPopupCard == mS2wCard) {
            //            isS2wActive = (Wake.isS2wActive() > 0);
            Wake.setS2w(position, getActivity());
            if (isS2wActive != (position > 0)) ReloadFragment();
        } else if (dPopupCard == mSleepMiscCard) Wake.setSleepMisc(position, getActivity());
        else if (dPopupCard == mWakeMiscCard) Wake.setWakeMisc(position, getActivity());
        else if (dPopupCard == mDt2sCard) Wake.setDt2s(position, getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mWakeGesturesVibStrengthCard) Wake.setvibstrength(position, getActivity());
        else if (dSeekBarCard == mWakeTimeoutCard) Wake.setWakeTimeout(position, getActivity());
        else if (dSeekBarCard == mS2WTimeCard) Wake.setS2WTime(position + 1, getActivity());
        else if (dSeekBarCard == mDT2WTimeBetweenTapsCard) Wake.setDT2WTimeBetweenTaps(position + 1, getActivity());
        else if (dSeekBarCard == mDT2WFeatherXCard) Wake.setDT2WFeatherX(position + 1, getActivity());
        else if (dSeekBarCard == mDT2WFeatherYCard) Wake.setDT2WFeatherY(position + 1, getActivity());
    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mDt2wCard) {
            Wake.activateDt2w(checked, getActivity());
            ReloadFragment();
        } else if (dSwitchCard == mLenientCard)
            Wake.activateLenient(checked, getActivity());
        else if (dSwitchCard == mCameraGestureCard)
            Wake.activateCameraGesture(checked, getActivity());
        else if (dSwitchCard == mPocketModeCard)
            Wake.activatePocketMode(checked, getActivity());
        else if (dSwitchCard == mPowerKeySuspendCard)
            Wake.activatePowerKeySuspend(checked, getActivity());
        else {
            for (int i = 0; i < mGestureCards.length; i++)
                if (dSwitchCard == mGestureCards[i]) {
                    Wake.activateGesture(checked, i, getActivity());
                    return;
                }
        }
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    private void Update() {
        if (mDT2WTimeBetweenTapsCard != null) mDT2WTimeBetweenTapsCard.setProgress(Wake.getDT2WTimeBetweenTaps() - 1);
        if (mS2WTimeCard != null) mS2WTimeCard.setProgress(Wake.getS2WTime() - 1);
        if (mDT2WFeatherXCard != null) mDT2WFeatherXCard.setProgress(Wake.getDT2WFeatherX() - 1);
        if (mDT2WFeatherYCard != null) mDT2WFeatherYCard.setProgress(Wake.getDT2WFeatherY() - 1);
        if (mWakeGesturesVibStrengthCard != null) mWakeGesturesVibStrengthCard.setProgress(Wake.getvibstrength());
    }

    private void ReloadFragment() {
        view.invalidate();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

}
