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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.Manifest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grarak.kerneladiutor.FileBrowserActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.GetPermission;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.tools.Backup;
import com.kerneladiutor.library.Tools;
import com.kerneladiutor.library.root.RootFile;
import com.kerneladiutor.library.root.RootUtils;

import java.io.File;
import java.util.LinkedHashMap;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by willi on 17.05.15.
 */
public class BackupFragment extends RecyclerViewFragment {

    private TextView title;

    private RootFile boot;
    private RootFile recovery;

    @Override
    public int getSpan() {
        int orientation = Utils.getScreenOrientation(getActivity());
        if (Utils.isTablet(getActivity()))
            return orientation == Configuration.ORIENTATION_LANDSCAPE ? 5 : 4;
        return orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
    }

    @Override
    public RecyclerView getRecyclerView() {
        View view = getParentView(R.layout.backup_recyclerview);
        title = (TextView) view.findViewById(R.id.title_view);

        view.findViewById(R.id.backup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE).ask(new GetPermission.PermissionCallBack() {
                    @Override
                    public void granted() {
                        options(false, null);
                    }

                    @Override
                    public void denied() {
                        Utils.request_writeexternalstorage(getActivity());
                    }
                });
            }
        });

        view.findViewById(R.id.flash_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString(FileBrowserActivity.FILE_TYPE_ARG, "img");
                Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
                intent.putExtras(args);
                startActivityForResult(intent, 0);
            }
        });

        return (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) options(true, new RootFile(data.getExtras().getString("path")));
    }

    private void options(final boolean flashing, final RootFile file) {
        final LinkedHashMap < String, Backup.PARTITION > menu = new LinkedHashMap < > ();
        if (Backup.getBootPartition() != null)
            menu.put(getString(R.string.boot), Backup.PARTITION.BOOT);
        if (Backup.getRecoveryPartition() != null)
            menu.put(getString(R.string.recovery), Backup.PARTITION.RECOVERY);

        String[] items = new String[menu.keySet().toArray().length];
        for (int i = 0; i < items.length; i++)
            items[i] = (String) menu.keySet().toArray()[i];
        new AlertDialog.Builder(getActivity(),
            (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight)).setItems(items,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (flashing) restoreDialog(file, (Backup.PARTITION) menu.values().toArray()[which], false);
                    else backupDialog((Backup.PARTITION) menu.values().toArray()[which]);
                }
            }).show();
    }

    private void backupDialog(final Backup.PARTITION partition_type) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setPadding(30, 30, 30, 30);

        final AppCompatEditText editText = new AppCompatEditText(getActivity());
        editText.setTextColor(ContextCompat.getColor(getActivity(), 
            Utils.DARKTHEME ? R.color.textcolor_dark : R.color.black));
        editText.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (partition_type == Backup.PARTITION.BOOT) editText.setText(RootUtils.getKernelVersion());
        editText.setHint(getString(R.string.name));

        layout.addView(editText);

        new AlertDialog.Builder(getActivity(),
                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight)).setView(layout)
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String name = editText.getText().toString().trim();
                    if (name.isEmpty()) {
                        Utils.toast(getString(R.string.empty_name), getActivity());
                        return;
                    }

                    RootFile file = null;
                    switch (partition_type) {
                        case BOOT:
                            file = boot;
                            break;
                        case RECOVERY:
                            file = recovery;
                            break;
                    }
                    if (file != null && new File(file.toString() + "/" + name + ".img").exists()) {
                        Utils.toast(getString(R.string.backup_already_exists), getActivity());
                        return;
                    }

                    new AsyncTask < Void, Void, Void > () {
                        private MaterialDialog progressDialog;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            progressDialog = new MaterialDialog.Builder(getActivity())
                            .content(getString(R.string.backing_up))
                            .progress(true, 0)
                            .canceledOnTouchOutside(false)
                            .show();
                        }

                        @Override
                        protected Void doInBackground(Void...params) {
                            Backup.backup(name, partition_type);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    create();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }.execute();
                }
            }).show();
    }

    @Override
    public void preInit(Bundle savedInstanceState) {
        super.preInit(savedInstanceState);

        String kafolder = Environment.getExternalStorageDirectory().getPath() + "/KA_Backups/";
        boot = new RootFile(kafolder + "boot");
        recovery = new RootFile(kafolder + "recovery");
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        create();
    }

    private void create() {
        removeAllViews();

        final long size = viewInit(boot, Backup.PARTITION.BOOT) + viewInit(recovery, Backup.PARTITION.RECOVERY);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title.setText(getCount() > 0 ? getString(R.string.items_found, getCount()) + " (" +
                    size + getString(R.string.mb) + ")" : getString(R.string.no_backups));
            }
        });
    }

    private long viewInit(RootFile folder, final Backup.PARTITION partition_type) {
        folder.mkdir();

        long size = 0;
        String text = null;
        if (folder.toString().endsWith("boot")) text = getString(R.string.boot);
        else if (folder.toString().endsWith("recovery")) text = getString(R.string.recovery);
        if (text == null) return 0;
        for (final RootFile file: folder.listFiles())
            if (file.getName().endsWith(".img")) {
                CardViewItem.DCardView cardView = new CardViewItem.DCardView();
                cardView.setTitle(file.getName().replace(".img", ""));
                float fileSize = file.length() / 1024;
                size += fileSize;
                cardView.setDescription(text + ", " + fileSize + getString(R.string.mb));
                cardView.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                    @Override
                    public void onClick(CardViewItem.DCardView dCardView) {
                        new AlertDialog.Builder(getActivity(),
                                (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                            .setItems(getResources().getStringArray(R.array.backup_menu),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                restoreDialog(file, partition_type, true);
                                                break;
                                            case 1:
                                                deleteDialog(file);
                                                break;
                                        }
                                    }
                                }).show();
                    }
                });

                addView(cardView);
            }
        return size;
    }

    private void restoreDialog(final RootFile file, final Backup.PARTITION partition_type, final boolean restoring) {
        Utils.confirmDialog(null, getString(R.string.overwrite_question, Backup.getPartition(partition_type)), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask < Void, Void, Void > () {
                    private MaterialDialog progressDialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new MaterialDialog.Builder(getActivity())
                        .content(restoring ? R.string.restoring : R.string.flashing)
                        .progress(true, 0)
                        .canceledOnTouchOutside(false)
                        .show();
                    }

                    @Override
                    protected Void doInBackground(Void...params) {
                        Backup.restore(file, partition_type);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        progressDialog.dismiss();
                    }
                }.execute();
            }
        }, getActivity());
    }

    private void deleteDialog(final RootFile file) {
        Utils.confirmDialog(null, getString(R.string.delete_question, file.getName()), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                file.delete();
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        create();
                    }
                });
            }
        }, getActivity());
    }

}
