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
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.LMK;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 27.12.14.
 */
public class LMKFragment extends RecyclerViewFragment implements Constants, SwitchCardView.DSwitchCard.OnDSwitchCardListener, SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener {

    private SeekBarCardView.DSeekBarCard[] mMinFreeCard;
    private SeekBarCardView.DSeekBarCard mVmPressureFileMinCard;
    private CardViewItem.DCardView[] mProfileCard;
    private CardViewItem.DCardView mLMKcount;
    private SwitchCardView.DSwitchCard mAdaptiveCard;

    private final List < String > values = new ArrayList < > (), modifiedvalues = new ArrayList < > ();

    private final String[] mProfileValues7 = new String[] {
        "60,80,100,120,160,200,210",
        "172,190,208,226,244,280,283",
        "2,4,5,8,12,16,17",
        "4,8,10,16,24,32,33",
        "4,8,16,32,48,64,66",
        "8,16,32,64,96,128,130",
        "16,32,64,128,192,256,259"
    };

    private final String[] mProfileValues6 = new String[] {
        "60,80,100,120,160,200",
        "172,190,208,226,244,280",
        "2,4,5,8,12,16",
        "4,8,10,16,24,32",
        "4,8,16,32,48,64",
        "8,16,32,64,96,128",
        "16,32,64,128,192,256"
    };

    private static String[] mProfileValues;

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        values.clear();
        modifiedvalues.clear();
        for (int x = 0; x < 1025; x++) {
            modifiedvalues.add(x + getString(R.string.mb));
            values.add(String.valueOf(x * 256));
        }

        if (LMK.hasLMKcount()) {
            mLMKcount = new CardViewItem.DCardView();
            mLMKcount.setTitle(getString(R.string.lmk_count));
            addView(mLMKcount);
        }

        if (LMK.hasAdaptive()) {
            mAdaptiveCard = new SwitchCardView.DSwitchCard();
            mAdaptiveCard.setTitle(getString(R.string.adaptive));
            mAdaptiveCard.setDescription(getString(R.string.adaptive_summary));
            mAdaptiveCard.setChecked(LMK.getAdaptive());
            mAdaptiveCard.setOnDSwitchCardListener(this);

            addView(mAdaptiveCard);
        }

        DDivider mLmkMinFreeDividerCard = new DDivider();
        mLmkMinFreeDividerCard.setText(getString(R.string.lmk_minfree));
        addView(mLmkMinFreeDividerCard);

        List < String > minfrees = LMK.getMinFrees();
        mMinFreeCard = new SeekBarCardView.DSeekBarCard[minfrees.size()];
        try {
            for (int i = 0; i < minfrees.size(); i++) {
                mMinFreeCard[i] = new SeekBarCardView.DSeekBarCard(modifiedvalues);
                mMinFreeCard[i].setTitle(getResources().getStringArray(R.array.lmk_names)[i]);
                mMinFreeCard[i].setProgress(modifiedvalues.indexOf(LMK.getMinFree(minfrees, i) / 256 + getString(R.string.mb)));
                mMinFreeCard[i].setOnDSeekBarCardListener(new SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener() {
                    @Override
                    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

                    @Override
                    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
                        List < String > minFrees = LMK.getMinFrees();
                        String minFree = "";

                        for (int i = 0; i < mMinFreeCard.length; i++)
                            if (dSeekBarCard == mMinFreeCard[i])
                                minFree += minFree.isEmpty() ? values.get(position) : "," + values.get(position);
                            else
                                minFree += minFree.isEmpty() ? minFrees.get(i) : "," + minFrees.get(i);

                        LMK.setMinFree(minFree, getActivity());
                        refresh();
                    }
                });

                addView(mMinFreeCard[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (LMK.hasVmPressureFileMin()) {
            List < String > list = new ArrayList < > ();
            for (int i = 0; i < 1025; i++)
                list.add(i + getString(R.string.mb));

            mVmPressureFileMinCard = new SeekBarCardView.DSeekBarCard(list);
            mVmPressureFileMinCard.setTitle(getString(R.string.vmpressure_file_min));
            mVmPressureFileMinCard.setDescription(getString(R.string.vmpressure_file_min_summary));
            mVmPressureFileMinCard.setProgress((LMK.getVmPressureFileMin()) / 256);
            mVmPressureFileMinCard.setOnDSeekBarCardListener(this);

            addView(mVmPressureFileMinCard);
        }

        DDivider mProfilesDividerCard = new DDivider();
        mProfilesDividerCard.setText(getString(R.string.lmk_profiles));
        mProfilesDividerCard.setDescription(getString(R.string.lmk_profiles_summary));
        addView(mProfilesDividerCard);

        DDivider mProfilesBHBDividerCard = new DDivider();
        mProfilesBHBDividerCard.setText(getString(R.string.lmk_bhb_profiles));
        addView(mProfilesBHBDividerCard);

        LMKFragment ProfileFinalSize = new LMKFragment();
        if (LMK.hasVmPressureFileMin())
            ProfileFinalSize.mProfileValues = mProfileValues7;
        else
            ProfileFinalSize.mProfileValues = mProfileValues6;

        mProfileCard = new CardViewItem.DCardView[mProfileValues.length];
        for (int i = 0; i < mProfileValues.length; i++) {
            mProfileCard[i] = new CardViewItem.DCardView();
            mProfileCard[i].setTitle(getResources().getStringArray(R.array.lmk_profiles)[i]);
            mProfileCard[i].setDescription(mProfileValues[i] + getString(R.string.values_in_mb));
            mProfileCard[i].setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    for (CardViewItem.DCardView profile: mProfileCard)
                        if (dCardView == profile) {
                            String description = dCardView.getDescription().toString();
                            String[] descriptionSplitted = description.split("\\(");
                            String descriptionSplittedFirstHalf = descriptionSplitted[0];
                            String[] descriptionFinal = descriptionSplittedFirstHalf.split(",");
                            int minfree_1 = Utils.stringToInt(descriptionFinal[0]) * 256;
                            int minfree_2 = Utils.stringToInt(descriptionFinal[1]) * 256;
                            int minfree_3 = Utils.stringToInt(descriptionFinal[2]) * 256;
                            int minfree_4 = Utils.stringToInt(descriptionFinal[3]) * 256;
                            int minfree_5 = Utils.stringToInt(descriptionFinal[4]) * 256;
                            int minfree_6 = Utils.stringToInt(descriptionFinal[5]) * 256;
                            String minfree = minfree_1 + "," + minfree_2 + "," + minfree_3 + "," + minfree_4 +
                                "," + minfree_5 + "," + minfree_6;
                            LMK.setMinFree(minfree, getActivity());
                            if (LMK.hasVmPressureFileMin()) {
                                int vmfileminInt = Utils.stringToInt(descriptionFinal[6]) * 256;
                                LMK.setVmPressureFileMin(vmfileminInt, getActivity());
                            }
                            refresh();
                        }
                }
            });

            DDivider mProfilesOriginalDividerCard = new DDivider();
            mProfilesOriginalDividerCard.setText(getString(R.string.lmk_profiles_original));
            if (i == 2)
                addView(mProfilesOriginalDividerCard);
            addView(mProfileCard[i]);
        }
        Update();
    }

    private void refresh() {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(500);
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            List < String > minfrees = LMK.getMinFrees();
                            if (minfrees == null) return;
                            for (int i = 0; i < minfrees.size(); i++)
                                try {
                                    mMinFreeCard[i].setProgress(modifiedvalues.indexOf(LMK.getMinFree(minfrees, i) / 256 +
                                        getString(R.string.mb)));
                                    mVmPressureFileMinCard.setProgress((LMK.getVmPressureFileMin()) / 256);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    @Override
    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
        if (dSwitchCard == mAdaptiveCard)
            LMK.setAdaptive(checked, getActivity());
    }

    @Override
    public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

    @Override
    public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
        if (dSeekBarCard == mVmPressureFileMinCard) LMK.setVmPressureFileMin(position * 256, getActivity());
    }

    @Override
    public boolean onRefresh() {
        Update();
        return true;
    }

    public void Update() {
        if (mLMKcount != null) 
            mLMKcount.setDescription(LMK.getLMKcount() + " " + getString(R.string.lmk_count_summary));
    }
}
