package com.grarak.kerneladiutor.fragments.information;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.CPU;
import com.kerneladiutor.library.root.RootUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import android.util.SparseIntArray;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

/*
This rewrite is re-using code that Grarak had originally used in his fragment. Credits go to the original source.

 */
public class FrequencyTableFragment extends RecyclerViewFragment implements Constants {

    private String wake_sources, this_freq, this_pct;

    @Override
    protected boolean pullToRefreshIsEnabled() {
        return true;
    }

    @Override
    public void refreshView() {
        new AsyncTask < Void, Void, Void > () {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                removeAllViews();
            }

            @Override
            protected Void doInBackground(Void...params) {
                generateView();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                refreshLayout.setRefreshing(false);
            }
        }.execute();
    }

    @Override
    public int getSpan() {
        return Utils.getScreenOrientation(getActivity()) == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;
    }

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        generateView();
    }

    private void generateView() {

        CardViewItem.DCardView muptimeCard = new CardViewItem.DCardView();
        muptimeCard.setTitle(getString(R.string.system_uptime));
        muptimeCard.setDescription(getSysTimers());
        addView(muptimeCard);

        wake_sources = Utils.getwakeSources();
        if (!wake_sources.isEmpty()) {
            CardViewItem.DCardView wakesourceCard = new CardViewItem.DCardView();
            wakesourceCard.setTitle(getString(R.string.wakeup_source));
            wakesourceCard.setDescription(getString(R.string.wakeup_source_summary));
            wakesourceCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout linearLayout = new LinearLayout(getActivity());
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            linearLayout.setGravity(Gravity.CENTER);
                            linearLayout.setPadding(30, 20, 30, 20);

                            TextView under_title = new TextView(getActivity());
                            under_title.setText(getString(R.string.wakeup_source_dialog_summary));
                            linearLayout.addView(under_title);

                            ScrollView scrollView = new ScrollView(getActivity());
                            scrollView.setPadding(0, 0, 0, 10);
                            linearLayout.addView(scrollView);

                            TextView final_result = new TextView(getActivity());
                            final_result.setText(wake_sources);
                            final_result.setTextIsSelectable(true);
                            scrollView.addView(final_result);

                            new AlertDialog.Builder(getActivity(),
                                    (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                                .setTitle(getString(R.string.wakeup_source))
                                .setView(linearLayout).setNegativeButton(getString(R.string.copy_clipboard),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("FreqFrag", wake_sources);
                                            clipboard.setPrimaryClip(clip);
                                            Utils.toast(getString(R.string.copy_clipboard_ok), getActivity(), Toast.LENGTH_LONG);
                                            return;
                                        }
                                    })
                                .setPositiveButton(getString(R.string.close),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {}
                                    }).show();
                        }
                    });
                }
            });
            addView(wakesourceCard);
        }

        int wasoffline = 0;
        for (int i = 0; i < CPU.getCoreCount(); i++) {
            if (!CPU.isCoreOnline(i)) {
                wasoffline = 1;
                CPU.activateCore(i, true, getContext());
            }
            // <Freq, time>
            int total_time = 0;
            SparseIntArray freq_use_list = new SparseIntArray();
            StringBuilder unusedStates = new StringBuilder();

            try {
                FileInputStream fileInputStream = new FileInputStream(Utils.getsysfspath(CPU_TIME_IN_STATE_ARRAY, i));
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader buffreader = new BufferedReader(inputStreamReader);
                if (buffreader != null) {
                    String line;
                    String[] linePieces;
                    while ((line = buffreader.readLine()) != null) {
                        linePieces = line.split(" ");
                        total_time = total_time + Integer.parseInt(linePieces[1]);
                        freq_use_list.put(Integer.parseInt(linePieces[0]), Integer.parseInt(linePieces[1]));
                    }
                    inputStreamReader.close();
                    buffreader.close();
                }
            } catch (Exception ex) {
                Log.w(TAG, "No Time In State Stats found for core: " + i);
                ex.printStackTrace();
                // No reason to continue to card generation if there weren't any stats. Let's check the next core.
                continue;
            }

            List < Integer > allfreqs = CPU.getFreqs(i);
            LinearLayout uiStatesView = new LinearLayout(getActivity());
            uiStatesView.setOrientation(LinearLayout.VERTICAL);
            CardViewItem.DCardView frequencyCard = new CardViewItem.DCardView();
            frequencyCard.setTitle(String.format(getString(R.string.core_time_in_state), i) + " " + getDurationBreakdown(total_time * 10));
            frequencyCard.setView(uiStatesView);
            frequencyCard.setFullSpan(true);
            for (int x = 0; x < freq_use_list.size(); x++) {
                if (allfreqs.size() < x || allfreqs.get(x) == null) {
                    continue;
                }

                Integer time = freq_use_list.get(allfreqs.get(x));
                if (time == null) {
                    continue;
                }
                int freq_time = time;
                int pct = total_time > 0 ? (freq_time * 100) / total_time : 0;
                //Limit the freqs shown to only anything with at least 1% use
                if (pct >= 1) {
                    FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity())
                        .inflate(R.layout.state_row, uiStatesView, false);

                    // map UI elements to objects
                    TextView freqText = (TextView) layout.findViewById(R.id.ui_freq_text);
                    TextView durText = (TextView) layout.findViewById(R.id.ui_duration_text);
                    TextView perText = (TextView) layout.findViewById(R.id.ui_percentage_text);
                    ProgressBar bar = (ProgressBar) layout.findViewById(R.id.ui_bar);

                    // modify the row
                    this_freq = allfreqs.get(x) / 1000 + getString(R.string.mhz);
                    this_pct = pct + getString(R.string.percent);
                    freqText.setText(this_freq);
                    perText.setText(this_pct);
                    // Multiple the time_in_state time value by 10 as it is stored in UserTime Units (10ms)
                    durText.setText(getDurationBreakdown((freq_use_list.get(allfreqs.get(x))) * 10));
                    bar.setProgress(pct);

                    uiStatesView.addView(layout);
                } else {
                    if (unusedStates.length() > 0) {
                        unusedStates.append(", ");
                    }
                    unusedStates.append(allfreqs.get(x) / 1000).append(getString(R.string.mhz));
                }
            }
            addView(frequencyCard);
            if (unusedStates.length() > 0) {
                CardViewItem.DCardView mUnUsedStatesCard = new CardViewItem.DCardView();
                mUnUsedStatesCard.setTitle(String.format(getString(R.string.unused_cpu_states_new), i) + getString(R.string.percent) + ":");
                mUnUsedStatesCard.setDescription(unusedStates.toString());
                addView(mUnUsedStatesCard);
            }

            if (wasoffline == 1) {
                CPU.activateCore(i, false, getContext());
                wasoffline = 0;
            }
        }

    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     *
     * Function modified from answer here: http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java
     */
    public static String getDurationBreakdown(long millis) {
        StringBuilder sb = new StringBuilder(64);
        if (millis <= 0) {
            sb.append("00m00s");
            return sb.toString();
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (days > 0) {
            sb.append(days);
            sb.append("d");
        }
        if (hours > 0) {
            sb.append(hours);
            sb.append("h");
        }
        sb.append(String.format(Locale.US, "%02d", minutes));
        sb.append("m");
        sb.append(String.format(Locale.US, "%02d", seconds));
        sb.append("s");
        return sb.toString();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private String getSysTimers() {
        long uptime = SystemClock.uptimeMillis();
        long realtime = SystemClock.elapsedRealtime();

        String timers = getString(R.string.uptime) + " " + getDurationBreakdown(realtime); //Up time string + total uptime
        timers += "\n" + getString(R.string.awake) + " " + getDurationBreakdown(uptime); // Wake time string + total wake
        timers += " (" + ((uptime * 100) / realtime) + "%)"; // Wake time %
        timers += "\n" + getString(R.string.deep_sleep) + " " + getDurationBreakdown(realtime - uptime); // Sleep time string + total sleep
        timers += " (" + (100 - ((uptime * 100) / realtime)) + "%)"; // Sleep time %

        return timers;
    }

    private String getBatTimers() {
        String timers = "";
        // workaround to get screen on time, dump output = Screen on: 2h 20m 26s 504ms (8.4%) 41x, Interactive: 2h 20m 12s 676ms (8.4%)
        // dumpsys batterystats --charged | grep -A 60 'Statistics since last charge'
        String ScreenOnTime = RootUtils.runCommand("dumpsys batterystats | grep 'Time on battery' | head -1 | cut -d':' -f2 | cut -d's' -f1");
        if ((ScreenOnTime != null) && !ScreenOnTime.isEmpty())
            timers += ScreenOnTime + "s";
        ScreenOnTime = RootUtils.runCommand("dumpsys batterystats | grep Interactive | head -1 | cut -d':' -f2 | cut -d's' -f1");
        if ((ScreenOnTime != null) && !ScreenOnTime.isEmpty())
            timers += ScreenOnTime + "s";
        return timers;
    }
}
