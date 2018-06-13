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
import android.util.SparseLongArray;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

/*
This rewrite is re-using code that Grarak had originally used in his fragment. Credits go to the original source.

 */
public class FrequencyTableFragment extends RecyclerViewFragment implements Constants {

    private String this_freq, this_pct;

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

        int wasoffline = 0, total_time_offset = 10;
        for (int i = 0; i < CPU.getCoreCount(); i++) {
            if (!CPU.isCoreOnline(i)) {
                wasoffline = 1;
                CPU.activateCore(i, true, getContext());
            }
            // <Freq, time>
            long total_time = 0;
            SparseLongArray freq_use_list = new SparseLongArray();
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
                        freq_use_list.put(Integer.parseInt(linePieces[0]), Long.parseLong(linePieces[1]));
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

            // Multiple the time_in_state time value by 10 as it is stored in UserTime Units (10ms) when CONFIG_HZ=100
            // Multiple the time_in_state time value by 3.33 as it is stored in UserTime Units (3.33ms) when CONFIG_HZ=300
            // Multiple the time_in_state time value by 1 as it is stored in UserTime Units (1ms) when CONFIG_HZ=1000

            //Core 0 never goes to sleep so it's time is almost equal to Awake time aka uptimeMillis
            //if the total_time * total_time_offset is bigger then awake time set total_time_offset to 1
            if (i == 0 && ((total_time * total_time_offset) > SystemClock.uptimeMillis())) total_time_offset = 1;
            //Another check that can be added here is to do total_time_offset = 3.33 before set it to 1, as HZ can be set to 300
            //But this app kernel doesn't uses it so check yours if using this changes

            frequencyCard.setTitle(String.format(getString(R.string.core_time_in_state), i) + " " + getDurationBreakdown(total_time * total_time_offset));
            frequencyCard.setView(uiStatesView);
            frequencyCard.setFullSpan(true);
            for (int x = 0; x < freq_use_list.size(); x++) {
                if (allfreqs.size() < x || allfreqs.get(x) == null) continue;

                Long time = freq_use_list.get(allfreqs.get(x));
                if (time == null) continue;
                long freq_time = time;
                int pct = total_time > 0 ? (int)(0.5 + ((freq_time * 100) / total_time)) : 0;
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
                    durText.setText(getDurationBreakdown((freq_use_list.get(allfreqs.get(x))) * total_time_offset));
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
