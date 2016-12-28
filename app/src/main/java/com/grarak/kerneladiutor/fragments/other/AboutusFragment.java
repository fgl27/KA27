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

import android.os.Bundle;
import android.view.View;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Utils;

/**
 * Created by willi on 27.12.14.
 */
public class AboutusFragment extends RecyclerViewFragment {

    private final String APP_SOURCE = "https://github.com/FrancescoCG/CrazySuperKernelAdiutor/";
    private final String XDA_LINK = "http://forum.xda-developers.com/galaxy-s5/orig-development/kernel-crazysuperkernel-v1-t3485246";

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        ModififactionInit();
        ModififactionVersionInit();
        licenseInit();
        appSourceInit();
        featureRequestInit();
        xdaInit();
    }

    private void ModififactionInit() {
            CardViewItem.DCardView mModificationCard = new CardViewItem.DCardView();
            mModificationCard.setTitle(getString(R.string.modification));
            mModificationCard.setDescription(getString(R.string.modification_summary));

            addView(mModificationCard);
        }

    private void ModififactionVersionInit() {
        CardViewItem.DCardView mModificationVersionCard = new CardViewItem.DCardView();
        mModificationVersionCard.setTitle(getString(R.string.modification_version));
        mModificationVersionCard.setDescription(getString(R.string.modification_version_number));

        addView(mModificationVersionCard);
    }

    private void licenseInit() {
        CardViewItem.DCardView mLicenseCard = new CardViewItem.DCardView();
        mLicenseCard.setTitle(getString(R.string.license));

        View view = inflater.inflate(R.layout.app_license_view, container, false);

        mLicenseCard.setView(view);
        addView(mLicenseCard);
    }

    private void appSourceInit() {
        CardViewItem.DCardView mAppSourceCard = new CardViewItem.DCardView();
        mAppSourceCard.setTitle(getString(R.string.open_source));
        mAppSourceCard.setDescription(getString(R.string.open_source_summary));
        mAppSourceCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                Utils.launchUrl(getActivity(), APP_SOURCE);
            }
        });

        addView(mAppSourceCard);
    }

    private void featureRequestInit() {
        CardViewItem.DCardView mFeatureRequestCard = new CardViewItem.DCardView();
        mFeatureRequestCard.setTitle(getString(R.string.feature_request));
        mFeatureRequestCard.setDescription(getString(R.string.feature_requestb_summary));
        mFeatureRequestCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                Utils.launchUrl(getActivity(), XDA_LINK);
            }
        });

        addView(mFeatureRequestCard);
    }

    private void xdaInit() {
        CardViewItem.DCardView mXDACard = new CardViewItem.DCardView();
        mXDACard.setTitle(getString(R.string.xda));
        mXDACard.setDescription(getString(R.string.xda_summary));
        mXDACard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                Utils.launchUrl(getActivity(), XDA_LINK);
            }
        });

        addView(mXDACard);
    }
}
