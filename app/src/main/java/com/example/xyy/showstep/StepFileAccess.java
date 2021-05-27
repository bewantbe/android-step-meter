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
 * For loading and saving step state file.
 */

class StepFileAccess {
    final static String TAG = "StepFileAccess";
    private final int BYTE_OF_LONG = 8;
    private File stepRecFilePath = null;
    private Context context = null;
    private SharedPreferences sharedPref;

    StepFileAccess(Context _context) {
        context = _context;
        stepRecFilePath = new File(context.getFilesDir(),
                context.getString(R.string.step_record_file_name));
        // For saving last step data
        sharedPref = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
    }

    void getLastStepState(StepStateSaver.StepItem lastStepState, long bootTime) {
        // By default show step since boot
        long lastStep         = sharedPref.getLong("lastStep", 0);
        long lastStepDateTime = sharedPref.getLong("lastStepDateTime", bootTime);

        if (bootTime - lastStepDateTime > 100) {
            // The phone was rebooted.
            // In the future, consider use absolute step since app installed.
            lastStep = 0;
            lastStepDateTime = bootTime;
        }

        lastStepState.count      = lastStep;
        lastStepState.start_time = bootTime;
        lastStepState.stop_time  = lastStepDateTime;
    }

    void saveLastStepState(StepStateSaver.StepItem lastStepState) {
        // Save last step and boot time and last step time
        SharedPreferences sharedPref = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("lastStep",         lastStepState.count);
        editor.putLong("lastStepDateTime", lastStepState.stop_time);
        editor.apply();
    }

    // Load recent step records in file (R.string.step_record_file_name).
    void loadRecords(StepStateSaver.StepItem[] sStep, int MAX_STEP_SAVE) {
        Log.i(TAG, "loadRecords(): getFilesDir: " + stepRecFilePath.getAbsolutePath());
        RandomAccessFile stepRecFile;
        try {
            // Without these two lines, there is "I/Process: Sending signal. PID: ???? SIG: 9" in mate 9.
            FileOutputStream outputStream  = context.openFileOutput(stepRecFilePath.getName(), Context.MODE_APPEND);
            outputStream.close();

            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            long l = stepRecFile.length();
            Log.i(TAG, "Found old record with length " + l);
            if (l % (3*BYTE_OF_LONG) != 0) {  // Try fix broken file.
                Log.w(TAG, "StepStateSaver(): fixing step file.");
                stepRecFile.setLength(l - l % (3*BYTE_OF_LONG));
                l = stepRecFile.length();
            }
            Log.i(TAG, "loadRecords(): step states saved: " + l/(3*BYTE_OF_LONG));
            int m = 0;
            if (l/(3*BYTE_OF_LONG) < MAX_STEP_SAVE) {
                m = MAX_STEP_SAVE - (int)(l/(3*BYTE_OF_LONG));
                stepRecFile.seek(0);
            } else {
                stepRecFile.seek((3*BYTE_OF_LONG)*(l/(3*BYTE_OF_LONG) - MAX_STEP_SAVE));
            }

            // Load recent saved data. Filling from sStep[0] (most recent).
            for (int i = MAX_STEP_SAVE-1; i >= m; i--) {
                sStep[i - m].count      = stepRecFile.readLong();
                sStep[i - m].start_time = stepRecFile.readLong();
                sStep[i - m].stop_time  = stepRecFile.readLong();
                Log.i(TAG, "loadRecords(): load step " + sStep[i - m].count);
            }
            stepRecFile.close();
        } catch (FileNotFoundException e) {
            // Nothing have to be done
            Log.i(TAG, "No old record found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addOneRecord(StepStateSaver.StepItem stepItem) {
        // Add one more step state to file.
        RandomAccessFile stepRecFile;
        try {
            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            Log.i(TAG, "addOneRecord(): file open");
            stepRecFile.seek(stepRecFile.length());
            stepRecFile.writeLong(stepItem.count);
            stepRecFile.writeLong(stepItem.start_time);
            stepRecFile.writeLong(stepItem.stop_time);

            long l = stepRecFile.length();
            Log.i(TAG, "Now record with length " + l);

            stepRecFile.close();
            Log.i(TAG, "addOneRecord(): data written");
        } catch (IOException e) {
            Log.e(TAG, "addOneRecord(): IOException");
            e.printStackTrace();
        }
    }

    void overwriteLastRecord(StepStateSaver.StepItem stepItem) {
        // most recent record the last
        RandomAccessFile stepRecFile;
        try {
            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            if (stepRecFile.length() < (3*BYTE_OF_LONG)) {
                // nothing to update
                return;
            }
            stepRecFile.seek(stepRecFile.length() - 3*BYTE_OF_LONG);
            stepRecFile.writeLong(stepItem.count);
            stepRecFile.writeLong(stepItem.start_time);
            stepRecFile.writeLong(stepItem.stop_time);
            stepRecFile.close();
            Log.i(TAG, "overwriteLastRecord(): data updated");
        } catch (IOException e) {
            Log.e(TAG, "overwriteLastRecord(): IOException");
            e.printStackTrace();
        }
    }
}
