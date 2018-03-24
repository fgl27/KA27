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
import android.text.InputType;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.EditTextCardView;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.VM;
import com.grarak.kerneladiutor.utils.Utils;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by willi on 27.12.14.
 */

public class VMFragment extends RecyclerViewFragment implements PopupCardView.DPopupCard.OnDPopupCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener {

    private CardViewItem.DCardView mPRPressureCard, mPRAcgEffCard;

    private CardViewItem.DCardView mZramDiskCard, mZramSwapUsedCard, mZramRWCard, mZramDataSizeCard;

    private EditTextCardView.DEditTextCard mMinFreeKbytesCard, mExtraFreeKbytesCard;

    private SeekBarCardView.DSeekBarCard mPRPerSwapSizeCard, mPRSwapWinCard, mPRSwapOptEffCard, mPRPressureMaxCard, mPRPressureMinCard, mDirtyRatioCard, mDirtyBackgroundRatioCard, mDirtyExpireCard, mDirtyWritebackCard, mOverCommitRatioCard, mSwappinessCard, mVFSCachePressureCard, mZRAMDisksizeCard, mZRAMMaxCompStreamsCard;
    private SeekBarCardView.DSeekBarCard mDirty_Writeback_SuspendCard, mDirty_Writeback_ActiveCard;

    private PopupCardView.DPopupCard mZRAMCompAlgosCard;

    private SwitchCardView.DSwitchCard mProcessReclaimCard, mLaptopModeCard, mDynamic_Dirty_WritebackCard;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        if (VM.hasProcessReclaim()) processreclaimInit();
        if (VM.hasDirtyRatio()) dirtyratioInit();
        if (VM.hasDirtyBackgroundRatio()) dirtybackgroundratioInit();
        if (VM.hasDirtyExpire()) dirtyexpireInit();
        if (VM.hasDirtyWriteback() && !VM.isDynamicDirtyWritebackActive()) dirtywritebackInit();
        if (VM.hasDynamicDirtyWriteback()) dynamicdirtywritebackInit();
        if (VM.hasOverCommitRatio()) overcommitratioInit();
        if (VM.hasSwappiness()) swappinessInit();
        if (VM.hasVFSCachePressure()) vfscachepressureInit();

        if (VM.hasLaptopMode()) laptopmodeInit();

        if (VM.hasMinFreeKbytes()) minfreekbytesInit();
        if (VM.hasExtraFreeKbytes()) extrafreekbytesInit();

        if (VM.hasZRAM()) {
            if (VM.hasZRAMReadOnly()) zramROInit();
            else zramInit();
            ExtraZramInit();
        }
        Update();
    }

    private void processreclaimInit() {

        DDivider mProcessReclaimDividerCard = new DDivider();
        mProcessReclaimDividerCard.setText(getString(R.string.process_reclaim));
        addView(mProcessReclaimDividerCard);

        mProcessReclaimCard = new SwitchCardView.DSwitchCard();
        mProcessReclaimCard.setTitle(getString(R.string.process_reclaim_enable));
        mProcessReclaimCard.setDescription(getString(R.string.process_reclaim_enable_summary));
        mProcessReclaimCard.setChecked(VM.isProcessReclaimActive());
        mProcessReclaimCard.setOnDSwitchCardListener(this);

        addView(mProcessReclaimCard);

        if (VM.isProcessReclaimActive()) {
            // short things here PR = Process Reclaim
            if (VM.hasPRPressure()) {
                int pressure = VM.getPRPressure();

                mPRPressureCard = new CardViewItem.DCardView();
                mPRPressureCard.setTitle(getString(R.string.process_reclaim_pressure));
                mPRPressureCard.setDescription(String.valueOf(pressure));

                addView(mPRPressureCard);
            }
            if (VM.hasPRAvgEff()) {
                int avg = VM.getPRAvgEff();

                mPRAcgEffCard = new CardViewItem.DCardView();
                mPRAcgEffCard.setTitle(getString(R.string.process_reclaim_avg_eff));
                mPRAcgEffCard.setDescription(String.valueOf(avg));

                addView(mPRAcgEffCard);
            }

            if (VM.hasPRPerSwapSize()) {
                List < String > list = new ArrayList < > ();
                for (int i = 1; i <= 128; i++)
                    list.add(String.valueOf(i * 64));

                mPRPerSwapSizeCard = new SeekBarCardView.DSeekBarCard(list);
                mPRPerSwapSizeCard.setTitle(getString(R.string.process_reclaim_per_swap_size));
                mPRPerSwapSizeCard.setDescription(getString(R.string.process_reclaim_per_swap_size_summary));
                mPRPerSwapSizeCard.setProgress((VM.getPRPerSwapSize() / 64) - 1);
                mPRPerSwapSizeCard.setOnDSeekBarCardListener(this);

                addView(mPRPerSwapSizeCard);
            }

            if (VM.hasPRSwapWin()) {
                List < String > list = new ArrayList < > ();
                for (int i = 1; i <= 10; i++)
                    list.add(String.valueOf(i));

                mPRSwapWinCard = new SeekBarCardView.DSeekBarCard(list);
                mPRSwapWinCard.setTitle(getString(R.string.process_reclaim_swap_eff_win));
                mPRSwapWinCard.setDescription(getString(R.string.process_reclaim_swap_eff_win_summary));
                mPRSwapWinCard.setProgress((VM.getPRSwapWin()) - 1);
                mPRSwapWinCard.setOnDSeekBarCardListener(this);

                addView(mPRSwapWinCard);
            }

            if (VM.hasPRSwapOptEff()) {
                List < String > list = new ArrayList < > ();
                for (int i = 1; i <= 100; i++)
                    list.add(String.valueOf(i));

                mPRSwapOptEffCard = new SeekBarCardView.DSeekBarCard(list);
                mPRSwapOptEffCard.setTitle(getString(R.string.process_reclaim_swap_opt_eff));
                mPRSwapOptEffCard.setDescription(getString(R.string.process_reclaim_swap_opt_eff_summary));
                mPRSwapOptEffCard.setProgress((VM.getPRSwapOptEff()) - 1);
                mPRSwapOptEffCard.setOnDSeekBarCardListener(this);

                addView(mPRSwapOptEffCard);
            }

            if (VM.hasPRPressureMax()) {
                List < String > list = new ArrayList < > ();
                for (int i = 1; i <= 100; i++)
                    list.add(String.valueOf(i));

                mPRPressureMaxCard = new SeekBarCardView.DSeekBarCard(list);
                mPRPressureMaxCard.setTitle(getString(R.string.process_reclaim_pressure_max));
                mPRPressureMaxCard.setDescription(getString(R.string.process_reclaim_pressure_max_summary));
                mPRPressureMaxCard.setProgress((VM.getPRPressureMax()) - 1);
                mPRPressureMaxCard.setOnDSeekBarCardListener(this);

                addView(mPRPressureMaxCard);
            }

            if (VM.hasPRPressureMin()) {
                List < String > list = new ArrayList < > ();
                for (int i = 1; i <= 100; i++)
                    list.add(String.valueOf(i));

                mPRPressureMinCard = new SeekBarCardView.DSeekBarCard(list);
                mPRPressureMinCard.setTitle(getString(R.string.process_reclaim_pressure_min));
                mPRPressureMinCard.setDescription(getString(R.string.process_reclaim_pressure_min_summary));
                mPRPressureMinCard.setProgress((VM.getPRPressureMin()) - 1);
                mPRPressureMinCard.setOnDSeekBarCardListener(this);

                addView(mPRPressureMinCard);
            }
        }
    }

    private void dirtyratioInit() {

        DDivider mVMDividerCard = new DDivider();
        mVMDividerCard.setText(getString(R.string.virtual_memory));
        addView(mVMDividerCard);

        List < String > list = new ArrayList < > ();
        list.add(getString(R.string.disabled));
        for (int i = 1; i <= 100; i++)
            list.add(i + getString(R.string.percent));

        mDirtyRatioCard = new SeekBarCardView.DSeekBarCard(list);
        mDirtyRatioCard.setTitle(getString(R.string.dirty_ratio));
        mDirtyRatioCard.setDescription(getString(R.string.dirty_ratio_summary));
        mDirtyRatioCard.setProgress(VM.getDirtyRatio());
        mDirtyRatioCard.setOnDSeekBarCardListener(this);

        addView(mDirtyRatioCard);
    }
    private void dirtybackgroundratioInit() {
        List < String > list = new ArrayList < > ();
        list.add(getString(R.string.disabled));
        for (int i = 1; i <= 100; i++)
            list.add(i + getString(R.string.percent));

        mDirtyBackgroundRatioCard = new SeekBarCardView.DSeekBarCard(list);
        mDirtyBackgroundRatioCard.setTitle(getString(R.string.dirty_background_ratio));
        mDirtyBackgroundRatioCard.setDescription(getString(R.string.dirty_background_ratio_summary));
        mDirtyBackgroundRatioCard.setProgress(VM.getDirtyBackgroundRatio());
        mDirtyBackgroundRatioCard.setOnDSeekBarCardListener(this);

        addView(mDirtyBackgroundRatioCard);
    }
    private void dirtyexpireInit() {
        List < String > list = new ArrayList < > ();
        for (int i = 1; i <= 500; i++)
            list.add(i * 10 + getString(R.string.cs));

        mDirtyExpireCard = new SeekBarCardView.DSeekBarCard(list);
        mDirtyExpireCard.setTitle(getString(R.string.dirty_expire_centisecs));
        mDirtyExpireCard.setDescription(getString(R.string.dirty_expire_centisecs_summary));
        mDirtyExpireCard.setProgress((VM.getDirtyExpire() / 10) - 1);
        mDirtyExpireCard.setOnDSeekBarCardListener(this);

        addView(mDirtyExpireCard);
    }
    private void dirtywritebackInit() {
        List < String > list = new ArrayList < > ();
        for (int i = 1; i <= 900; i++)
            list.add(i * 10 + getString(R.string.cs));

        mDirtyWritebackCard = new SeekBarCardView.DSeekBarCard(list);
        mDirtyWritebackCard.setTitle(getString(R.string.dirty_writeback_centisecs));
        mDirtyWritebackCard.setDescription(getString(R.string.dirty_writeback_centisecs_summary));
        mDirtyWritebackCard.setProgress((VM.getDirtyWriteback()) - 1);
        mDirtyWritebackCard.setOnDSeekBarCardListener(this);

        addView(mDirtyWritebackCard);
    }

    private void dynamicdirtywritebackInit() {

        if (VM.hasDynamicDirtyWriteback()) {
            mDynamic_Dirty_WritebackCard = new SwitchCardView.DSwitchCard();
            mDynamic_Dirty_WritebackCard.setTitle(getString(R.string.dynamic_dirty_writeback_centisecs));
            mDynamic_Dirty_WritebackCard.setDescription(getString(R.string.dynamic_dirty_writeback_centisecs_summary));
            mDynamic_Dirty_WritebackCard.setChecked(VM.isDynamicDirtyWritebackActive());
            mDynamic_Dirty_WritebackCard.setOnDSwitchCardListener(this);

            addView(mDynamic_Dirty_WritebackCard);
        }

        if (VM.isDynamicDirtyWritebackActive()) {

            List < String > list = new ArrayList < > ();
            for (int i = 1; i <= 900; i++)
                list.add(i * 10 + getString(R.string.cs));

            if (VM.hasDirtySuspendWriteback()) {

                mDirty_Writeback_SuspendCard = new SeekBarCardView.DSeekBarCard(list);
                mDirty_Writeback_SuspendCard.setTitle(getString(R.string.dirty_writeback_suspend_centisecs));
                mDirty_Writeback_SuspendCard.setDescription(getString(R.string.dirty_writeback_suspend_centisecs_summary));
                mDirty_Writeback_SuspendCard.setProgress((VM.getDirtySuspendWriteback()) - 1);
                mDirty_Writeback_SuspendCard.setOnDSeekBarCardListener(this);

                addView(mDirty_Writeback_SuspendCard);
            }

            if (VM.hasDirtyActiveWriteback()) {

                mDirty_Writeback_ActiveCard = new SeekBarCardView.DSeekBarCard(list);
                mDirty_Writeback_ActiveCard.setTitle(getString(R.string.dirty_writeback_active_centisecs));
                mDirty_Writeback_ActiveCard.setDescription(getString(R.string.dirty_writeback_active_centisecs_summary));
                mDirty_Writeback_ActiveCard.setProgress((VM.getDirtySuspendWriteback()) - 1);
                mDirty_Writeback_ActiveCard.setOnDSeekBarCardListener(this);

                addView(mDirty_Writeback_ActiveCard);
            }
        }
    }

    private void overcommitratioInit() {
        List < String > list = new ArrayList < > ();
        list.add(getString(R.string.disabled));
        for (int i = 1; i <= 100; i++)
            list.add(i + getString(R.string.percent));

        mOverCommitRatioCard = new SeekBarCardView.DSeekBarCard(list);
        mOverCommitRatioCard.setTitle(getString(R.string.overcommit_ratio));
        mOverCommitRatioCard.setDescription(getString(R.string.overcommit_ratio_summary));
        mOverCommitRatioCard.setProgress(VM.getOverCommitRatio());
        mOverCommitRatioCard.setOnDSeekBarCardListener(this);

        addView(mOverCommitRatioCard);

    }
    private void swappinessInit() {
        List < String > list = new ArrayList < > ();
        list.add(getString(R.string.disabled));
        for (int i = 1; i <= 100; i++)
            list.add(i + getString(R.string.percent));

        mSwappinessCard = new SeekBarCardView.DSeekBarCard(list);
        mSwappinessCard.setTitle(getString(R.string.swappiness));
        mSwappinessCard.setDescription(getString(R.string.swappiness_summary));
        mSwappinessCard.setProgress(VM.getSwappiness());
        mSwappinessCard.setOnDSeekBarCardListener(this);

        addView(mSwappinessCard);
    }
    private void vfscachepressureInit() {
        List < String > list = new ArrayList < > ();
        list.add(getString(R.string.disabled));
        for (int i = 1; i <= 150; i++)
            list.add(String.valueOf(i));

        mVFSCachePressureCard = new SeekBarCardView.DSeekBarCard(list);
        mVFSCachePressureCard.setTitle(getString(R.string.vfs_cache_pressure));
        mVFSCachePressureCard.setDescription(getString(R.string.vfs_cache_pressure_summary));
        mVFSCachePressureCard.setProgress(VM.getVFSCachePressure() - 1);
        mVFSCachePressureCard.setOnDSeekBarCardListener(this);

        addView(mVFSCachePressureCard);
    }
    private void laptopmodeInit() {
        mLaptopModeCard = new SwitchCardView.DSwitchCard();
        mLaptopModeCard.setTitle(getString(R.string.laptop_mode));
        mLaptopModeCard.setDescription(getString(R.string.laptop_mode_summary));
        mLaptopModeCard.setChecked(VM.isLaptopModeActive());
        mLaptopModeCard.setOnDSwitchCardListener(this);

        addView(mLaptopModeCard);
    }
    private void minfreekbytesInit() {
        DDivider mMinFreeKbytesDividerCard = new DDivider();
        mMinFreeKbytesDividerCard.setText(getString(R.string.min_free_kbytes));
        mMinFreeKbytesDividerCard.setDescription(getString(R.string.min_free_kbytes_summary));
        addView(mMinFreeKbytesDividerCard);

        String value = VM.getMinFreeKbytes();
        mMinFreeKbytesCard = new EditTextCardView.DEditTextCard();
        mMinFreeKbytesCard.setDescription(value + " kb");
        mMinFreeKbytesCard.setValue(value);
        mMinFreeKbytesCard.setInputType(InputType.TYPE_CLASS_NUMBER);
        mMinFreeKbytesCard.setOnDEditTextCardListener(new EditTextCardView.DEditTextCard.OnDEditTextCardListener() {
            @Override
            public void onApply(EditTextCardView.DEditTextCard dEditTextCard, String value) {
                VM.setMinFreeKbytes(value.replace(" kb", ""), getActivity());
                dEditTextCard.setDescription(value + " kb");
            }
        });

        addView(mMinFreeKbytesCard);

    }

    private void extrafreekbytesInit() {
        DDivider mExtraFreeKbytesDividerCard = new DDivider();
        mExtraFreeKbytesDividerCard.setText(getString(R.string.extra_free_kbytes));
        mExtraFreeKbytesDividerCard.setDescription(getString(R.string.extra_free_kbytes_summary));
        addView(mExtraFreeKbytesDividerCard);

        String value = VM.getExtraFreeKbytes();
        mExtraFreeKbytesCard = new EditTextCardView.DEditTextCard();
        mExtraFreeKbytesCard.setDescription(value + " kb");
        mExtraFreeKbytesCard.setValue(value);
        mExtraFreeKbytesCard.setInputType(InputType.TYPE_CLASS_NUMBER);
        mExtraFreeKbytesCard.setOnDEditTextCardListener(new EditTextCardView.DEditTextCard.OnDEditTextCardListener() {
            @Override
            public void onApply(EditTextCardView.DEditTextCard dEditTextCard, String value) {
                VM.setExtraFreeKbytes(value.replace(" kb", ""), getActivity());
                dEditTextCard.setDescription(value + " kb");
            }
        });

        addView(mExtraFreeKbytesCard);
    }

    private void zramROInit() {
        DDivider mZRAMDividerCard = new DDivider();
        mZRAMDividerCard.setText(getString(R.string.zram_ro));
        addView(mZRAMDividerCard);

        String Swap = VM.getFreeSwap(getActivity());
        if (Swap != null) {
            String[] swap_split = Swap.split("[ ]+");

            mZramSwapUsedCard = new CardViewItem.DCardView();
            mZramSwapUsedCard.setTitle(getString(R.string.disksize_used));

            addView(mZramSwapUsedCard);
        } else {
            mZramDiskCard = new CardViewItem.DCardView();
            mZramDiskCard.setTitle(getString(R.string.disksize));

            addView(mZramDiskCard);
        }

    }

    private void zramInit() {
        DDivider mZRAMDividerCard = new DDivider();
        mZRAMDividerCard.setText(getString(R.string.zram));
        addView(mZRAMDividerCard);

        List < String > list = new ArrayList < > ();
        for (int i = 0; i < 101; i++)
            list.add((i * 10) + getString(R.string.mb));

        mZRAMDisksizeCard = new SeekBarCardView.DSeekBarCard(list);
        mZRAMDisksizeCard.setTitle(getString(R.string.disksize));
        mZRAMDisksizeCard.setDescription(getString(R.string.disksize_summary));
        mZRAMDisksizeCard.setProgress(VM.getZRAMDisksize() / 10);
        mZRAMDisksizeCard.setOnDSeekBarCardListener(this);

        addView(mZRAMDisksizeCard);

        if (VM.hasZRAMCompAlgos()) {
            mZRAMCompAlgosCard = new PopupCardView.DPopupCard(VM.getZRAMCompAlgos());
            mZRAMCompAlgosCard.setTitle(getString(R.string.zram_comp_algo));
            mZRAMCompAlgosCard.setDescription(getString(R.string.zram_comp_algo_summary));
            mZRAMCompAlgosCard.setItem(VM.getZRAMCompAlgo());
            mZRAMCompAlgosCard.setOnDPopupCardListener(this);

            addView(mZRAMCompAlgosCard);
        }

        if (VM.hasZRAMMaxCompStreams()) {
            List < String > listCS = new ArrayList < > ();
            for (int i = 1; i <= 4; i++)
                listCS.add(i + "");

            mZRAMMaxCompStreamsCard = new SeekBarCardView.DSeekBarCard(listCS);
            mZRAMMaxCompStreamsCard.setTitle(getString(R.string.zram_comp_streams));
            mZRAMMaxCompStreamsCard.setProgress(VM.getZRAMMaxCompStreams() - 1);
            mZRAMMaxCompStreamsCard.setOnDSeekBarCardListener(this);

            addView(mZRAMMaxCompStreamsCard);
        }
    }

    private void ExtraZramInit() {
        if (VM.hasZRAMDataSize()) {
            mZramDataSizeCard = new CardViewItem.DCardView();
            mZramDataSizeCard.setTitle(getString(R.string.zram_data_size));

            addView(mZramDataSizeCard);
        } 

        if (VM.hasZRAMReadWrites() && VM.hasZRAMFailReadWrites()) {
            mZramRWCard = new CardViewItem.DCardView();
            mZramRWCard.setTitle(getString(R.string.read_write));

            addView(mZramRWCard);
        } 
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mPRPerSwapSizeCard) VM.setPRPerSwapSize((position + 1) * 64, getActivity());
        else if (dSeekBarCard == mPRSwapWinCard) VM.setPRSwapWin(position + 1, getActivity());
        else if (dSeekBarCard == mPRSwapOptEffCard) VM.setPRSwapOptEff(position + 1, getActivity());
        else if (dSeekBarCard == mPRPressureMaxCard) VM.setPRPressureMax(position + 1, getActivity());
        else if (dSeekBarCard == mPRPressureMinCard) VM.setPRPressureMin(position + 1, getActivity());
        else if (dSeekBarCard == mDirtyRatioCard) VM.setDirtyRatio(position, getActivity());
        else if (dSeekBarCard == mDirtyBackgroundRatioCard) VM.setDirtyBackgroundRatio(position, getActivity());
        else if (dSeekBarCard == mDirtyExpireCard) VM.setDirtyExpire((position + 1) * 10, getActivity());
        else if (dSeekBarCard == mDirtyWritebackCard) VM.setDirtyWriteback(position + 1, getActivity());
        else if (dSeekBarCard == mDirty_Writeback_SuspendCard) VM.setDirtySuspendWriteback(position + 1, getActivity());
        else if (dSeekBarCard == mDirty_Writeback_ActiveCard) VM.setDirtyActiveWriteback(position + 1, getActivity());
        else if (dSeekBarCard == mOverCommitRatioCard) VM.setOverCommitRatio(position, getActivity());
        else if (dSeekBarCard == mSwappinessCard) VM.setSwappiness(position, getActivity());
        else if (dSeekBarCard == mVFSCachePressureCard) VM.setVFSCachePressure(position + 1, getActivity());
        else if (dSeekBarCard == mZRAMDisksizeCard) VM.setZRAM(null, String.valueOf(position * 10), null,  getActivity());
        else if (dSeekBarCard == mZRAMMaxCompStreamsCard) VM.setZRAM(null, null, String.valueOf(position + 1),  getActivity());
    }

    @Override
    public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mZRAMCompAlgosCard)
            VM.setZRAM(VM.getZRAMCompAlgos().get(position), null, null, getActivity());
    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mProcessReclaimCard) {
            VM.activateProcessReclaim(checked, getActivity());
            RefreshFrag();
        } else if (dSwitchCard == mLaptopModeCard)
            VM.activateLaptopMode(checked, getActivity());
        else if (dSwitchCard == mDynamic_Dirty_WritebackCard) {
            VM.activateDynamicDirtyWriteback(checked, getActivity());
            RefreshFrag();
        }
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    public void Update() {
        int pressure = VM.getPRPressure();
        int avg = VM.getPRAvgEff();

        if (mPRPressureCard != null) mPRPressureCard.setDescription(String.valueOf(pressure));
        if (mPRAcgEffCard != null) mPRAcgEffCard.setDescription(String.valueOf(avg));

        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (mZRAMDisksizeCard != null) mZRAMDisksizeCard.setProgress(VM.getZRAMDisksize() / 10);
        if (mZRAMMaxCompStreamsCard != null) mZRAMMaxCompStreamsCard.setProgress(VM.getZRAMMaxCompStreams() - 1);
        if (mZRAMCompAlgosCard != null) mZRAMCompAlgosCard.setItem(VM.getZRAMCompAlgo());

        if (mZramDiskCard != null)
            mZramDiskCard.setDescription(VM.getZRAMDisksize() + getString(R.string.mb));

        if (mZramSwapUsedCard != null) {
            String[] swap_split = VM.getFreeSwap(getActivity()).split("[ ]+");
            int total = Utils.stringToInt(swap_split[1]);
            int free = Utils.stringToInt(swap_split[3]);
            int used = Utils.stringToInt(swap_split[2]);
            mZramSwapUsedCard.setDescription(Utils.MbKb(total, getActivity()) + " | " +
                Utils.MbKb(free, getActivity()) + " | " + Utils.MbKb(used, getActivity()) + " | " +
                Utils.percentage(total, used, getActivity()));
        }

        if (mZramDataSizeCard != null) {
            int original = VM.getZramOrigDataSize() / 1024;
            int compressed = VM.getZramCompDataSize() / 1024;
            mZramDataSizeCard.setDescription(Utils.MbKb(original, getActivity()) + " | " +
                Utils.MbKb(compressed, getActivity()) + " | " + Utils.percentage(original / 1024, compressed / 1024, getActivity()));
        }

        if (mZramRWCard != null)
            mZramRWCard.setDescription(getString(R.string.total) + VM.getZramReadWrites() + "\n" +
                getString(R.string.fail) + VM.getZramFailReadWrites());
    }

    private void RefreshFrag() {
        view.invalidate();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

}
