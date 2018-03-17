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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.kernel.CPU;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.Battery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by willi on 03.01.15.
 */
public class BatteryFragment extends RecyclerViewFragment implements
PopupCardView.DPopupCard.OnDPopupCardListener, SwitchCardView.DSwitchCard.OnDSwitchCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener {

    private int bclFreqCount = 0, bclArraylist = 60;

    private CardViewItem.DCardView mBatteryLevelCard, mBatteryVoltageCard, mBatteryTemperature, mBatteryChargingCurrentCard, mBatteryChargingTypeCard, mBatteryHealthCard;

    private SwitchCardView.DSwitchCard mForceFastChargeCard, mBatteryLedCard;
    private SwitchCardView.DSwitchCard mBclCard, mBclHotplugCard;

    private PopupCardView.DPopupCard mBclMaxFreqCard;
    private PopupCardView.DPopupCard mBclHotmask;
    private SeekBarCardView.DSeekBarCard mBclVphLowCard, mBclVphHighCard;

    private SeekBarCardView.DSeekBarCard mBlxCard;

    private SwitchCardView.DSwitchCard mCustomChargeRateEnableCard;
    private SeekBarCardView.DSeekBarCard mChargingRateCard;

    private SwitchCardView.DSwitchCard mC0StateCard, mC1StateCard, mC2StateCard, mC3StateCard;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        if (Battery.hasBatteryLed()) BatteryLedInit();
        batteryLevelInit();
        batteryHealthInit();
        batteryVoltageInit();
        batteryChargingCurrentInit();
        batteryTemperatureInit();
        batteryChargingTypeInit();
        if (Battery.hasForceFastCharge()) forceFastChargeInit();
        if (Battery.hasBlx()) blxInit();
        if (Battery.hasChargeRate()) chargerateInit();

        if (Battery.hasBcl()) bclInit();
        if (Battery.hasBclFreq()) bclMaxFreqInit();
        if (Battery.hasBclHotMask()) bclHotmask();
        if (Battery.hasBclVphLow()) BclVphLowInit();
        if (Battery.hasBclVphHigh()) BclVphHighInit();
        if (Battery.hasBclHotplug()) bclHotplugInit();

        cstatesInit();
        Update();
    }

    @Override
    public void postInit(Bundle savedInstanceState) {
        super.postInit(savedInstanceState);
        if (getCount() < 4) showApplyOnBoot(false);
    }

    private void batteryLevelInit() {
        mBatteryLevelCard = new CardViewItem.DCardView();
        mBatteryLevelCard.setTitle(getString(R.string.battery_level));

        addView(mBatteryLevelCard);
    }

    private void BatteryLedInit() {
        mBatteryLedCard = new SwitchCardView.DSwitchCard();
        mBatteryLedCard.setTitle(getString(R.string.battery_led));
        mBatteryLedCard.setDescription(getString(R.string.battery_led_summary));
        mBatteryLedCard.setChecked(Battery.getBatteryLed());
        mBatteryLedCard.setOnDSwitchCardListener(this);

        addView(mBatteryLedCard);
    }

    private void batteryVoltageInit() {
        mBatteryVoltageCard = new CardViewItem.DCardView();
        mBatteryVoltageCard.setTitle(getString(R.string.battery_voltage));

        addView(mBatteryVoltageCard);
    }

    private void batteryTemperatureInit() {
        mBatteryTemperature = new CardViewItem.DCardView();
        mBatteryTemperature.setTitle(getString(R.string.battery_temperature));

        addView(mBatteryTemperature);
    }

    private void batteryChargingCurrentInit() {
        mBatteryChargingCurrentCard = new CardViewItem.DCardView();
        mBatteryChargingCurrentCard.setTitle(getString(R.string.battery_charging_current));

        addView(mBatteryChargingCurrentCard);
    }

    private void batteryChargingTypeInit() {
        mBatteryChargingTypeCard = new CardViewItem.DCardView();
        mBatteryChargingTypeCard.setTitle(getString(R.string.battery_charging_mode));

        addView(mBatteryChargingTypeCard);
    }

    private void batteryHealthInit() {
        mBatteryHealthCard = new CardViewItem.DCardView();
        mBatteryHealthCard.setTitle(getString(R.string.battery_health));

        addView(mBatteryHealthCard);
    }

    private void forceFastChargeInit() {
        mForceFastChargeCard = new SwitchCardView.DSwitchCard();
        mForceFastChargeCard.setTitle(getString(R.string.usb_fast_charge));
        mForceFastChargeCard.setDescription(getString(R.string.usb_fast_charge_summary));
        mForceFastChargeCard.setChecked(Battery.isForceFastChargeActive());
        mForceFastChargeCard.setOnDSwitchCardListener(this);

        addView(mForceFastChargeCard);
    }

    private void blxInit() {
        List < String > list = new ArrayList < > ();
        for (int i = 0; i < 101; i++) list.add(String.valueOf(i));

        mBlxCard = new SeekBarCardView.DSeekBarCard(list);
        mBlxCard.setTitle(getString(R.string.blx));
        mBlxCard.setDescription(getString(R.string.blx_summary));
        mBlxCard.setProgress(Battery.getCurBlx());
        mBlxCard.setOnDSeekBarCardListener(this);

        addView(mBlxCard);
    }

    private void chargerateInit() {

        if (Battery.hasCustomChargeRateEnable()) {
            mCustomChargeRateEnableCard = new SwitchCardView.DSwitchCard();
            mCustomChargeRateEnableCard.setDescription(getString(R.string.custom_charge_rate));
            mCustomChargeRateEnableCard.setChecked(Battery.isCustomChargeRateActive());
            mCustomChargeRateEnableCard.setOnDSwitchCardListener(this);

            addView(mCustomChargeRateEnableCard);
        }

        if (Battery.hasChargingRate()) {
            List < String > list = new ArrayList < > ();
            for (int i = 10; i < 151; i++) list.add((i * 10) + getString(R.string.ma));

            mChargingRateCard = new SeekBarCardView.DSeekBarCard(list);
            mChargingRateCard.setTitle(getString(R.string.charge_rate));
            mChargingRateCard.setDescription(getString(R.string.charge_rate_summary));
            mChargingRateCard.setProgress((Battery.getChargingRate() / 10) - 10);
            mChargingRateCard.setOnDSeekBarCardListener(this);

            addView(mChargingRateCard);
        }
    }

    private void bclInit() {
        DDivider mBclDivider = new DDivider();
        mBclDivider.setText(getString(R.string.bcl));
        addView(mBclDivider);

        mBclCard = new SwitchCardView.DSwitchCard();
        mBclCard.setTitle(getString(R.string.bcl));
        mBclCard.setDescription(getString(R.string.bcl_summary));
        mBclCard.setChecked(Battery.isBclActive());
        mBclCard.setOnDSwitchCardListener(this);

        addView(mBclCard);
    }

    private void bclMaxFreqInit() {
        bclFreqCount = 0;
        List < String > freqs = new ArrayList < > ();
        for (int freq: CPU.getFreqs()) {
            if (freq >= Battery.getBclLimitFreq()) {
                bclFreqCount++;
                freqs.add(freq / 1000 + getString(R.string.mhz));
            }
        }
        mBclMaxFreqCard = new PopupCardView.DPopupCard(freqs);
        mBclMaxFreqCard.setTitle(getString(R.string.bcl_max_freq));
        mBclMaxFreqCard.setDescription(getString(R.string.bcl_max_freq_summary));
        mBclMaxFreqCard.setItem(Battery.getBclFreq() / 1000 + getString(R.string.mhz));
        mBclMaxFreqCard.setOnDPopupCardListener(this);

        addView(mBclMaxFreqCard);
    }

    private void bclHotmask() {
        mBclHotmask = new PopupCardView.DPopupCard(new ArrayList < > (
            Arrays.asList(getResources().getStringArray(R.array.bcl_hot_plug))));
        mBclHotmask.setTitle(getString(R.string.bcl_cores));
        mBclHotmask.setDescription(getString(R.string.bcl_cores_summary));
        mBclHotmask.setItem(Battery.getBclHotMask());
        mBclHotmask.setOnDPopupCardListener(this);

        addView(mBclHotmask);
    }

    private void BclVphLowInit() {
        int position = 0;
        List < String > list = new ArrayList < > ();
        for (int i = 0; i < 30; i++) {
            position = ((i + bclArraylist) * 50);
            list.add(String.valueOf(position + getString(R.string.mv)));
        }

        mBclVphLowCard = new SeekBarCardView.DSeekBarCard(list);
        mBclVphLowCard.setTitle(getString(R.string.bcl_voltage_low));
        mBclVphLowCard.setDescription(getString(R.string.bcl_voltage_low_summary));
        mBclVphLowCard.setProgress((((Battery.getBclVphLow()) / 1000) - (bclArraylist * 50)) / 50);
        mBclVphLowCard.setOnDSeekBarCardListener(this);

        addView(mBclVphLowCard);
    }

    private void BclVphHighInit() {
        int position = 0;
        List < String > list = new ArrayList < > ();
        for (int i = 0; i < 30; i++) {
            position = ((i + bclArraylist) * 50);
            list.add(String.valueOf(position + getString(R.string.mv)));
        }

        mBclVphHighCard = new SeekBarCardView.DSeekBarCard(list);
        mBclVphHighCard.setTitle(getString(R.string.bcl_voltage_high));
        mBclVphHighCard.setDescription(getString(R.string.bcl_voltage_high_summary));
        mBclVphHighCard.setProgress((((Battery.getBclVphHigh()) / 1000) - (bclArraylist * 50)) / 50);
        mBclVphHighCard.setOnDSeekBarCardListener(this);

        addView(mBclVphHighCard);
    }

    private void bclHotplugInit() {
        mBclHotplugCard = new SwitchCardView.DSwitchCard();
        mBclHotplugCard.setTitle(getString(R.string.bcl_hotplug));
        mBclHotplugCard.setDescription(getString(R.string.bcl_hotplug_summary));
        mBclHotplugCard.setChecked(Battery.isBclHotplugActive());
        mBclHotplugCard.setOnDSwitchCardListener(this);

        addView(mBclHotplugCard);
    }

    private void cstatesInit() {
        List < DAdapter.DView > views = new ArrayList < > ();

        if (Battery.hasC0State()) {
            mC0StateCard = new SwitchCardView.DSwitchCard();
            mC0StateCard.setTitle(getString(R.string.c0state));
            mC0StateCard.setDescription(getString(R.string.c0state_summary));
            mC0StateCard.setChecked(Battery.isC0StateActive());
            mC0StateCard.setOnDSwitchCardListener(this);

            views.add(mC0StateCard);
        }

        if (Battery.hasC1State()) {
            mC1StateCard = new SwitchCardView.DSwitchCard();
            mC1StateCard.setTitle(getString(R.string.c1state));
            mC1StateCard.setDescription(getString(R.string.c1state_summary));
            mC1StateCard.setChecked(Battery.isC1StateActive());
            mC1StateCard.setOnDSwitchCardListener(this);

            views.add(mC1StateCard);
        }

        if (Battery.hasC2State()) {
            mC2StateCard = new SwitchCardView.DSwitchCard();
            mC2StateCard.setTitle(getString(R.string.c2state));
            mC2StateCard.setDescription(getString(R.string.c2state_summary));
            mC2StateCard.setChecked(Battery.isC2StateActive());
            mC2StateCard.setOnDSwitchCardListener(this);

            views.add(mC2StateCard);
        }

        if (Battery.hasC3State()) {
            mC3StateCard = new SwitchCardView.DSwitchCard();
            mC3StateCard.setTitle(getString(R.string.c3state));
            mC3StateCard.setDescription(getString(R.string.c3state_summary));
            mC3StateCard.setChecked(Battery.isC3StateActive());
            mC3StateCard.setOnDSwitchCardListener(this);

            views.add(mC3StateCard);
        }

        if (views.size() > 0) {
            DDivider mCstatesCard = new DDivider();
            mCstatesCard.setText(getString(R.string.cstates));
            addView(mCstatesCard);

            addAllViews(views);
        }
    }

    @Override
    public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
        if (dPopupCard == mBclMaxFreqCard)
            Battery.setBclFreq(CPU.getFreqs().get((CPU.getFreqs().size() - bclFreqCount) + position), getActivity());
        if (dPopupCard == mBclHotmask)
            Battery.setBclHotMask(position, getActivity());
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    public void Update() {
        if (mBclMaxFreqCard != null) mBclMaxFreqCard.setItem(Battery.getBclFreq() / 1000 + getString(R.string.mhz));
        if (mBclHotmask != null) mBclHotmask.setItem(Battery.getBclHotMask());
        if (mBatteryLevelCard != null) mBatteryLevelCard.setDescription(Battery.getBatteryLevel() + getString(R.string.percent));
        if (mBatteryChargingCurrentCard != null) {
            double amperage = (double) Battery.getChargingCurrent() / 1000;
            if (amperage < 0) mBatteryChargingCurrentCard.setDescription(amperage + getString(R.string.ma));
            else mBatteryChargingCurrentCard.setDescription("+" + amperage + getString(R.string.ma));
        }
        if (mBatteryVoltageCard != null) {
            double voltage_now = (double) Battery.getBatteryVoltageNow() / 1000;
            mBatteryVoltageCard.setDescription(voltage_now + getString(R.string.mv));
        }
        if (mBatteryTemperature != null) {
            double celsius = (double) Battery.getBatteryTemp() / 10;
            mBatteryTemperature.setDescription(Utils.formatCelsius(celsius) + " " + Utils.celsiusToFahrenheit(celsius));
        }
        if (mBatteryChargingTypeCard != null) {
            if (Battery.getChargingType().equals("None"))
                mBatteryChargingTypeCard.setDescription(getString(R.string.battery_charging_mode_none));
            else if (Battery.getChargingType().equals("Weak"))
                mBatteryChargingTypeCard.setDescription(getString(R.string.battery_charging_mode_weak));
            else mBatteryChargingTypeCard.setDescription(Battery.getChargingType());
        }
        if (mBatteryLedCard != null)
            mBatteryLedCard.setChecked(Battery.getBatteryLed());
        if (mBatteryHealthCard != null)
            mBatteryHealthCard.setDescription(Battery.getHealth());
        if (mBclCard != null)
            mBclCard.setChecked(Battery.isBclActive());
        if (mBclHotplugCard != null)
            mBclHotplugCard.setChecked(Battery.isBclHotplugActive());
        if (mBclVphHighCard != null)
            mBclVphHighCard.setProgress((((Battery.getBclVphHigh()) / 1000) - (bclArraylist * 50)) / 50);
        if (mBclVphLowCard != null)
            mBclVphLowCard.setProgress((((Battery.getBclVphLow()) / 1000) - (bclArraylist * 50)) / 50);
    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mForceFastChargeCard)
            Battery.activateForceFastCharge(checked, getActivity());
        else if (dSwitchCard == mCustomChargeRateEnableCard)
            Battery.activateCustomChargeRate(checked, getActivity());
        else if (dSwitchCard == mC0StateCard)
            Battery.activateC0State(checked, getActivity());
        else if (dSwitchCard == mC1StateCard)
            Battery.activateC1State(checked, getActivity());
        else if (dSwitchCard == mC2StateCard)
            Battery.activateC2State(checked, getActivity());
        else if (dSwitchCard == mC3StateCard)
            Battery.activateC3State(checked, getActivity());
        else if (dSwitchCard == mBatteryLedCard)
            Battery.setBatteryLed(checked, getActivity());
        else if (dSwitchCard == mBclCard)
            Battery.activateBcl(checked, getActivity());
        else if (dSwitchCard == mBclHotplugCard)
            Battery.activateBclHotplug(checked, getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mBlxCard)
            Battery.setBlx(position, getActivity());
        else if (dSeekBarCard == mChargingRateCard)
            Battery.setChargingRate((position * 10) + 100, getActivity());
        else if (dSeekBarCard == mBclVphLowCard)
            Battery.setBclVphLow((position + bclArraylist) * 50000, getActivity());
        else if (dSeekBarCard == mBclVphHighCard)
            Battery.setBclVphHigh((position + bclArraylist) * 50000, getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
