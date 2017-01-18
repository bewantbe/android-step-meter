package com.example.xyy.showstep;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Save step state
 * Created by xyy on 1/17/17.
 */
public class StepStateSaver {
    // By default show step since boot
    public long lastStep = 0;
    public long lastStepDateTime = 0;

    final int MAX_STEP_SAVE = 10;

    public class StepItem {
        public long count;
        public long start_time;
        public long stop_time;

        public StepItem(long _count, long _start_time, long _stop_time) {
            this.count = _count;
            this.start_time = _start_time;
            this.stop_time = _stop_time;
        }
    }

    public StepItem sStep[] = null;

    final int BYTE_OF_LONG = 8;

    private Context context = null;
    private File stepRecFilePath = null;

    // context = MainActivity.this
    public StepStateSaver(Context _context) {
        context = _context;

        // By default show step since boot
        long lastStepDefault         = 0;
        long lastStepDateTimeDefault = java.lang.System.currentTimeMillis()
                - android.os.SystemClock.elapsedRealtime();

        SharedPreferences sharedPref = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
        lastStep         = sharedPref.getLong("lastStep", lastStepDefault);
        lastStepDateTime = sharedPref.getLong("lastStepDateTime", lastStepDateTimeDefault);

        if (lastStepDateTimeDefault - lastStepDateTime > 100) {
            // The phone was rebooted.
            // In the future, we might use absolute step since app installed.
            lastStep = 0;
            lastStepDateTime = lastStepDateTimeDefault;
        }

        sStep = new StepItem[MAX_STEP_SAVE];
        for (int i = MAX_STEP_SAVE-1; i >= 0; i--) {
            sStep[i] = new StepItem(0, 0, 0);
        }

        stepRecFilePath = new File(context.getFilesDir(),
                context.getString(R.string.step_record_file_name));
        Log.i("StepStateSaver", "restartCounter(): getFilesDir: " + stepRecFilePath.getAbsolutePath());
        RandomAccessFile stepRecFile = null;
        try {
            // Without these two lines, there is "I/Process: Sending signal. PID: ???? SIG: 9" in mate 9.
            FileOutputStream outputStream  = context.openFileOutput(stepRecFilePath.getName(), Context.MODE_APPEND);
            outputStream.close();

            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            long l = stepRecFile.length();
            Log.i("StepStateSaver", "Found old record with length " + l);
            if (l % (3*BYTE_OF_LONG) != 0) {  // Try fix
                Log.w("StepStateSaver", "StepStateSaver(): fixing step file.");
                stepRecFile.setLength(l - l % (3*BYTE_OF_LONG));
                l = stepRecFile.length();
            }
            Log.i("StepStateSaver", "StepStateSaver(): step saved: " + l/(3*BYTE_OF_LONG));
            int m = 0;
            if (l/(3*BYTE_OF_LONG) < MAX_STEP_SAVE) {
                m = MAX_STEP_SAVE - (int)(l/(3*BYTE_OF_LONG));
                stepRecFile.seek(0);
            } else {
                stepRecFile.seek((3*BYTE_OF_LONG)*(l/(3*BYTE_OF_LONG) - MAX_STEP_SAVE));
            }

            // Load saved data
            for (int i = MAX_STEP_SAVE-1; i >= m; i--) {
                sStep[i - m].count      = stepRecFile.readLong();
                sStep[i - m].start_time = stepRecFile.readLong();
                sStep[i - m].stop_time  = stepRecFile.readLong();
                Log.i("StepStateSaver", "StepStateSaver(): load step " + sStep[i - m].count);
            }
            stepRecFile.close();
        } catch (FileNotFoundException e) {
            // Nothing have to be done
            Log.i("StepStateSaver", "No old record found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveState() {
        // Save last step and boot time and last step time
        SharedPreferences sharedPref = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("lastStep",         lastStep);
        editor.putLong("lastStepDateTime", lastStepDateTime);
        editor.commit();
    }

    private void updateStepFile() {
        // most recent record the last
        RandomAccessFile stepRecFile = null;
        try {
            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            if (stepRecFile.length() < (3*BYTE_OF_LONG)) {
                // nothing to update
                return;
            }
            stepRecFile.seek(stepRecFile.length() - 3*BYTE_OF_LONG);
            stepRecFile.writeLong(sStep[0].count);
            stepRecFile.writeLong(sStep[0].start_time);
            stepRecFile.writeLong(sStep[0].stop_time);
            stepRecFile.close();
            Log.i("StepStateSaver", "restartCounter(): data updated");
        } catch (IOException e) {
            Log.e("StepStateSaver", "restartCounter(): IOException");
            e.printStackTrace();
        }
    }

    public long step;
    public long stepDateTime = 0;

    /**
     * Cache the step counter.
     * @param _step: step since last boot
     */
    public void putStep(long _step) {
        step = _step;
        stepDateTime = java.lang.System.currentTimeMillis();
    }

    /**
     * Reset lastStep as if step counter been reset
     */
    public void resetCounter() {
        lastStep = step;
        lastStepDateTime = java.lang.System.currentTimeMillis();
        saveState();
    }

    public void restartCounter() {
        if (stepSince() == 0) {
            // No new step
            if (sStep[0].count == 0) {
                // merge record
                sStep[0].stop_time = java.lang.System.currentTimeMillis();
                resetCounter();
                updateStepFile();
                return;
            }
            // else have a new record
        }

        // save new value and shift old values
        StepItem tmpStep = sStep[MAX_STEP_SAVE-1];  // shallow copy
        for (int i = MAX_STEP_SAVE-1; i > 0; i--) {
            sStep[i] = sStep[i-1];
        }
        sStep[0] = tmpStep;
        sStep[0].count      = stepSince();
        sStep[0].start_time = lastStepDateTime;
        sStep[0].stop_time  = java.lang.System.currentTimeMillis();
        resetCounter();

        // Save to file
        RandomAccessFile stepRecFile = null;
        try {
            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            Log.i("StepStateSaver", "restartCounter(): file open");
            stepRecFile.seek(stepRecFile.length());
            stepRecFile.writeLong(sStep[0].count);
            stepRecFile.writeLong(sStep[0].start_time);
            stepRecFile.writeLong(sStep[0].stop_time);

            long l = stepRecFile.length();
            Log.i("StepStateSaver", "Now record with length " + l);

            stepRecFile.close();
            Log.i("StepStateSaver", "restartCounter(): data written");
        } catch (IOException e) {
            Log.e("StepStateSaver", "restartCounter(): IOException");
            e.printStackTrace();
        }
    }

    public long stepSince() {
        return step - lastStep;
    }
}
