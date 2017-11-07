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
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.KSM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 27.12.14.
 */
public class KSMFragment extends RecyclerViewFragment implements PopupCardView.DPopupCard.OnDPopupCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener {

    private CardViewItem.DCardView[] mInfos;

    private PopupCardView.DPopupCard mCpuGovCard;

    private SwitchCardView.DSwitchCard mEnableKsmCard, mDeferredTimerCard;

    private SeekBarCardView.DSeekBarCard mPagesToScanCard, mSleepMillisecondsCard, mCpuUseCard;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        ksmInit();
        ksmInfoInit();
        Update();
    }

    private void ksmInfoInit() {
        mInfos = new CardViewItem.DCardView[KSM.getInfoLength()];
        String[] titles = getResources().getStringArray(R.array.ksm_infos);
        for (int i = 0; i < mInfos.length; i++)
            if (KSM.hasInfo(i)) {
                mInfos[i] = new CardViewItem.DCardView();
                mInfos[i].setTitle(titles[i]);

                addView(mInfos[i]);
            }
    }

    private void ksmInit() {
        mEnableKsmCard = new SwitchCardView.DSwitchCard();
        mEnableKsmCard.setTitle(getString(R.string.ksm_enable));
        mEnableKsmCard.setDescription(getString(R.string.ksm_enable_summary));
        mEnableKsmCard.setOnDSwitchCardListener(this);

        addView(mEnableKsmCard);

        if (KSM.hasDeferredTimer()) {
            mDeferredTimerCard = new SwitchCardView.DSwitchCard();
            mDeferredTimerCard.setTitle(getString(R.string.ksm_deferred_timer));
            mDeferredTimerCard.setDescription(getString(R.string.ksm_deferred_timer_summary));
            mDeferredTimerCard.setOnDSwitchCardListener(this);

            addView(mDeferredTimerCard);
        }

        if (KSM.hasPagesToScan()) {
            List < String > list = new ArrayList < > ();
            for (int i = 0; i < 1025; i++) list.add(String.valueOf(i));

            mPagesToScanCard = new SeekBarCardView.DSeekBarCard(list);
            mPagesToScanCard.setTitle(getString(R.string.ksm_pages_to_scan));
            mPagesToScanCard.setProgress(KSM.getPagesToScan());
            mPagesToScanCard.setOnDSeekBarCardListener(this);

            addView(mPagesToScanCard);
        }

        if (KSM.hasCpuGov()) {
            mCpuGovCard = new PopupCardView.DPopupCard(KSM.getCpuGovs());
            mCpuGovCard.setTitle(getString(R.string.uksm_cpu_governor));
            mCpuGovCard.setDescription(getString(R.string.uksm_cpu_governor_summary));
            mCpuGovCard.setOnDPopupCardListener(this);

            addView(mCpuGovCard);
        }

        if (KSM.hasSleepMilliseconds()) {
            List < String > list = new ArrayList < > ();
            for (int i = 0; i < 5001; i++) list.add(i + getString(R.string.ms));

            mSleepMillisecondsCard = new SeekBarCardView.DSeekBarCard(list);
            mSleepMillisecondsCard.setTitle(getString(R.string.ksm_sleep_milliseconds));
            mSleepMillisecondsCard.setOnDSeekBarCardListener(this);

            addView(mSleepMillisecondsCard);
        }

        if (KSM.hasCpuUse()) {
            List < String > list = new ArrayList < > ();
            for (int i = 0; i < 101; i++) list.add(i + getString(R.string.percent));

            mCpuUseCard = new SeekBarCardView.DSeekBarCard(list);
            mCpuUseCard.setTitle(getString(R.string.uksm_cpu_use));
            mCpuUseCard.setDescription(getString(R.string.uksm_cpu_use_summary));
            mCpuUseCard.setOnDSeekBarCardListener(this);

            addView(mCpuUseCard);
        }

    }

    @Override
    public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mCpuGovCard)
            KSM.setCpuGov(KSM.getCpuGovs().get(position), getActivity());
    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mEnableKsmCard) KSM.activateKsm(checked, getActivity());
        else if (dSwitchCard == mDeferredTimerCard)
            KSM.activateDeferredTimer(checked, getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mPagesToScanCard) KSM.setPagesToScan(position, getActivity());
        else if (dSeekBarCard == mSleepMillisecondsCard)
            KSM.setSleepMilliseconds(position, getActivity());
        else if (dSeekBarCard == mCpuUseCard)
            KSM.setCpuUse(position, getActivity());
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    public void Update() {
        if (mInfos != null) {
            for (int i = 0; i < mInfos.length; i++)
                if (mInfos[i] != null) mInfos[i].setDescription(KSM.getInfo(i));
        }

        if (mCpuUseCard != null)
            mCpuUseCard.setProgress(KSM.getCpuUse());

        if (mSleepMillisecondsCard != null)
            mSleepMillisecondsCard.setProgress(KSM.getSleepMilliseconds());

        if (mCpuGovCard != null)
            mCpuGovCard.setItem(KSM.getCpuGov());

        if (mDeferredTimerCard != null)
            mDeferredTimerCard.setChecked(KSM.isDeferredTimerActive());

        if (mEnableKsmCard != null)
            mEnableKsmCard.setChecked(KSM.isKsmActive());
    }

}
