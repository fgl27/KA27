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

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.Constants;
import com.kerneladiutor.library.root.RootUtils;

@TargetApi(24)
public class QuickTileCPUInfo extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        Utils.WriteSettings(this);
        if ((Utils.HasGlobalInt(this, Constants.SHOW_CPU, 10) == 10) || !RootUtils.rootAccess())
            getQsTile().setLabel(this.getString(R.string.not_available));
        getQsTile().updateTile();
    }

    @Override
    public void onClick() {
        if ((Utils.HasGlobalInt(this, Constants.SHOW_CPU, 10) != 10) && RootUtils.rootAccess()) {
            boolean state = !Utils.GlobalIntGet(this, Constants.SHOW_CPU);
            Utils.GlobalIntSet(state, this, Constants.SHOW_CPU);
            Utils.StartAppService(state, "com.android.systemui/.CPUInfoService");
        } else Utils.toast(this.getString(R.string.not_available), this);
    }

}
