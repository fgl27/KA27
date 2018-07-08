/*
 * Copyright (C) Felipe de Leon <fglfgl27@gmail.com>
 *
 * This file is part of iSu.
 *
 * iSu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iSu.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.tileservice;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.service.quicksettings.Tile;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.Constants;
import com.kerneladiutor.library.root.RootUtils;

@TargetApi(24)
public class QuickTileCPUInfo extends TileService {

    private String serviceName = "com.android.systemui/.CPUInfoService";
    private Tile mTile;
    private boolean state;

    @Override
    public void onStartListening() {
        super.onStartListening();
        mTile = getQsTile();
        if (RootUtils.rootAccess()) {
            Utils.WriteSettings(this);
            state = Utils.GlobalIntGet(this, Constants.SHOW_CPU);
            if (state && !Utils.ServiceRunning()) {
                TileUavailable();
                return;
            }
            mTile.setLabel(this.getString(R.string.show_cpu_info) + (state ? " ON" : " OFF"));
            mTile.setState(state ? mTile.STATE_ACTIVE : mTile.STATE_INACTIVE);
            mTile.updateTile();
        } else TileUavailable();
    }

    @Override
    public void onStopListening() {
        RootUtils.closeSU();
    }

    @Override
    public void onClick() {
        if (RootUtils.rootAccess()) {
            mTile = getQsTile();
            state = !Utils.GlobalIntGet(this, Constants.SHOW_CPU);
            Utils.GlobalIntSet(state, this, Constants.SHOW_CPU);
            Utils.StartAppService(state, serviceName);
            mTile.setLabel(this.getString(R.string.show_cpu_info) + (state ? " ON" : " OFF"));
            mTile.setState(state ? mTile.STATE_ACTIVE : mTile.STATE_INACTIVE);
            mTile.updateTile();

            if (state && !Utils.ServiceRunning()) TileUavailable();
        } else TileUavailable();
    }

    private void TileUavailable() {
        mTile = getQsTile();
        mTile.setLabel(this.getString(R.string.not_available));
        mTile.setState(mTile.STATE_UNAVAILABLE);
        mTile.updateTile();
    }

}
