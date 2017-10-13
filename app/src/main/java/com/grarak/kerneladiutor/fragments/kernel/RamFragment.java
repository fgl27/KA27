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

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Bundle;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.Ram;
import com.grarak.kerneladiutor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 26.12.14.
 */
public class RamFragment extends RecyclerViewFragment implements PopupCardView.DPopupCard.OnDPopupCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener {

    private CardViewItem.DCardView mCurFreqCard, mRamUsedCard;
    private PopupCardView.DPopupCard mMaxFreqCard, mMinFreqCard;
    private SeekBarCardView.DSeekBarCard mPollMsCard;

    private List < String > freqs;
    private List < String > freqs_dev;

    private ActivityManager mActivityManager;
    private MemoryInfo mMemoryInfo = new MemoryInfo();
    private long mMemoryDivider = 1048576L; // 1024*1024
    private int mFreeRAM, mTotalRAM;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        mActivityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        mActivityManager.getMemoryInfo(mMemoryInfo);
        mTotalRAM = (int)(mMemoryInfo.totalMem / mMemoryDivider);

        freqs = Ram.getFreqs();
        if (Ram.device("quark", "8084")) {
            freqs_dev = new ArrayList < > ();
            for (String freq: Ram.devFreqs())
                freqs_dev.add(freq + getString(R.string.mhz));
        } else freqs_dev = Ram.getFreqs();
        RamInit();
    }

    private void RamInit() {

            mRamUsedCard = new CardViewItem.DCardView();
            mRamUsedCard.setTitle(getString(R.string.memosize_used));

            addView(mRamUsedCard);

        mCurFreqCard = new CardViewItem.DCardView();
        mCurFreqCard.setTitle(getString(R.string.ram_cur_freq));
        addView(mCurFreqCard);

        if (Ram.hasRamMaxFreq()) {
            mMaxFreqCard = new PopupCardView.DPopupCard(freqs_dev);
            mMaxFreqCard.setTitle(getString(R.string.ram_max_freq));
            mMaxFreqCard.setDescription(getString(R.string.ram_max_freq_summary));
            mMaxFreqCard.setItem(FreqValue(Ram.getRamMaxFreq()));
            mMaxFreqCard.setOnDPopupCardListener(this);

            addView(mMaxFreqCard);
        }

        if (Ram.hasRamMinFreq()) {
            mMinFreqCard = new PopupCardView.DPopupCard(freqs_dev);
            mMinFreqCard.setTitle(getString(R.string.ram_min_freq));
            mMinFreqCard.setDescription(getString(R.string.ram_min_freq_summary));
            mMinFreqCard.setItem(FreqValue(Ram.getRamMinFreq()));
            mMinFreqCard.setOnDPopupCardListener(this);

            addView(mMinFreqCard);
        }

        List < String > list = new ArrayList < > ();
        for (int i = 1; i < 201; i++) list.add((i * 10) + getString(R.string.ms));

        mPollMsCard = new SeekBarCardView.DSeekBarCard(list);
        mPollMsCard.setTitle(getString(R.string.poll));
        mPollMsCard.setDescription(getString(R.string.poll_ram_summary));
        mPollMsCard.setProgress((Ram.getRamPoll() / 10) - 1);
        mPollMsCard.setOnDSeekBarCardListener(this);

        addView(mPollMsCard);

        Update();
    }

    @Override
    public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mMaxFreqCard)
            Ram.setRamMaxFreq(freqs.get(position), getActivity());
        else if (dPopupCard == mMinFreqCard)
            Ram.setRamMinFreq(freqs.get(position), getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mPollMsCard) Ram.setRamPoll((position + 1) * 10, getActivity());
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    public void Update() {
        if (mCurFreqCard != null)
            mCurFreqCard.setDescription(FreqValue(Ram.getRamCurFreq()));
        if (mMaxFreqCard != null)
            mMaxFreqCard.setItem(FreqValue(Ram.getRamMaxFreq()));
        if (mMinFreqCard != null)
            mMinFreqCard.setItem(FreqValue(Ram.getRamMinFreq()));
        if (mPollMsCard != null)
            mPollMsCard.setProgress((Ram.getRamPoll() / 10) - 1);
        if (mRamUsedCard != null) {
            mActivityManager.getMemoryInfo(mMemoryInfo);
            mFreeRAM = (int)(mMemoryInfo.availMem / mMemoryDivider);
            mRamUsedCard.setDescription(mTotalRAM + getString(R.string.mb) + " | " +
                mFreeRAM + getString(R.string.mb) + " | " + Utils.percentage(mTotalRAM, mFreeRAM, getActivity()));
        }
    }

    private String FreqValue(String value) {
        return freqs_dev.get(getListPos(freqs, value));
    }

    private int getListPos(List list, String value) {
        return list.indexOf(value);
    }

}
