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
package com.grarak.kerneladiutor.fragments.tools;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.fragments.kernel.BatteryFragment;
import com.grarak.kerneladiutor.fragments.kernel.CPUFragment;
import com.grarak.kerneladiutor.fragments.kernel.CPUHotplugFragment;
import com.grarak.kerneladiutor.fragments.kernel.CPUVoltageFragment;
import com.grarak.kerneladiutor.fragments.kernel.EntropyFragment;
import com.grarak.kerneladiutor.fragments.kernel.GPUFragment;
import com.grarak.kerneladiutor.fragments.kernel.IOFragment;
import com.grarak.kerneladiutor.fragments.kernel.KSMFragment;
import com.grarak.kerneladiutor.fragments.kernel.LMKFragment;
import com.grarak.kerneladiutor.fragments.kernel.MiscFragment;
import com.grarak.kerneladiutor.fragments.kernel.RamFragment;
import com.grarak.kerneladiutor.fragments.kernel.ScreenFragment;
import com.grarak.kerneladiutor.fragments.kernel.SoundFragment;
import com.grarak.kerneladiutor.fragments.kernel.ThermalFragment;
import com.grarak.kerneladiutor.fragments.kernel.VMFragment;
import com.grarak.kerneladiutor.fragments.kernel.WakeFragment;
import com.grarak.kerneladiutor.fragments.kernel.WakeLockFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.database.CommandDB;
import com.grarak.kerneladiutor.utils.root.Control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by willi on 25.04.15.
 */
public class StartUpCommandsFragment extends RecyclerViewFragment {

    private CardViewItem.DCardView mAllStartUpCommandsCard;
    private CardViewItem.DCardView[] mStartUpCommands;

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        listcommands();
    }

    public void listcommands() {
        CommandDB commandDB = new CommandDB(getActivity());
        List < CommandDB.CommandItem > commandItems = commandDB.getAllCommands();
        final List < String > applys = new ArrayList < > ();
        List < String > commands = new ArrayList < > ();

        Class[] classes = {
            BatteryFragment.class,
            CPUFragment.class,
            CPUHotplugFragment.class,
            CPUVoltageFragment.class,
            EntropyFragment.class,
            GPUFragment.class,
            IOFragment.class,
            KSMFragment.class,
            LMKFragment.class,
            MiscFragment.class,
            RamFragment.class,
            ScreenFragment.class,
            SoundFragment.class,
            ThermalFragment.class,
            VMFragment.class,
            WakeFragment.class,
            WakeLockFragment.class
        };

        for (Class mClass: classes)
            if (Utils.getBoolean(mClass.getSimpleName() + "onboot", false, getContext())) {
                applys.addAll(Utils.getApplys(mClass));
            }

        if (applys.size() > 0)
            for (CommandDB.CommandItem commandItem: commandItems) {
                for (String sys: applys) {
                    String path = commandItem.getPath();
                    if ((sys.contains(path) || path.contains(sys))) {
                        String command = commandItem.getCommand();
                        if (commands.indexOf(command) < 0)
                            commands.add(command);
                    }
                }
            }

        if (commands.size() > 0) {
            Collections.reverse(commands);
            mAllStartUpCommandsCard = new CardViewItem.DCardView();
            mAllStartUpCommandsCard.setTitle(getString(R.string.all_startup_commands));
            final String allcommands = android.text.TextUtils.join("\n", commands);

            mAllStartUpCommandsCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {

                            new AlertDialog.Builder(getActivity(),
                                    (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                                .setTitle(getString(R.string.startup_commands_delete_all_commands))
                                .setNeutralButton(getString(R.string.delete),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            new AlertDialog.Builder(getActivity(),
                                                    (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                                                .setMessage(getString(R.string.startup_commands_delete_all))
                                                .setNeutralButton(getString(R.string.delete),
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Control.deletespecificcommand(getActivity(), null, null);
                                                            RefreshFrag();
                                                            return;
                                                        }
                                                    })
                                                .setPositiveButton(getString(R.string.cancel),
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int i) {
                                                            return;
                                                        }
                                                    }).show();

                                            return;
                                        }
                                    })
                                .setPositiveButton(getString(R.string.copy),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("Startup Comnmand", allcommands);
                                            clipboard.setPrimaryClip(clip);
                                            Utils.toast(getString(R.string.startup_commands_copy_all), getActivity(), Toast.LENGTH_SHORT);
                                            return;
                                        }
                                    }).show();

                        }
                    });
                }
            });
            addView(mAllStartUpCommandsCard);

            DDivider mStartUpCommandsListDividerCard = new DDivider();
            mStartUpCommandsListDividerCard.setText(getString(R.string.startup_commands_list));
            mStartUpCommandsListDividerCard.setDescription(getString(R.string.startup_commands_list_info));
            addView(mStartUpCommandsListDividerCard);

            mStartUpCommands = new CardViewItem.DCardView[commandItems.size()];
            for (int i = 0; i < commands.size(); i++) {
                mStartUpCommands[i] = new CardViewItem.DCardView();
                mStartUpCommands[i].setDescription(commands.get(i));
                final String command = commands.get(i);
                mStartUpCommands[i].setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                    @Override
                    public void onClick(CardViewItem.DCardView dCardView) {

                        new AlertDialog.Builder(getActivity(),
                                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                            .setTitle(getString(R.string.startup_commands_delete_single))
                            .setMessage(command)
                            .setNeutralButton(getString(R.string.delete),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Control.deletespecificcommand(getActivity(), null, command);
                                        RefreshFrag();
                                        return;
                                    }
                                })
                            .setPositiveButton(getString(R.string.copy),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Startup Comnmand", command);
                                        clipboard.setPrimaryClip(clip);
                                        Utils.toast(getString(R.string.startup_commands_copy), getActivity(), Toast.LENGTH_SHORT);
                                        return;
                                    }
                                }).show();

                    }
                });
                addView(mStartUpCommands[i]);
            }
        } else {
            mAllStartUpCommandsCard = new CardViewItem.DCardView();
            mAllStartUpCommandsCard.setTitle(getString(R.string.startup_commands_none));

            addView(mAllStartUpCommandsCard);
        }
    }

    public void RefreshFrag() {
        view.invalidate();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

}
