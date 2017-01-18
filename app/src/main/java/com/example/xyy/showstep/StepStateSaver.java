package com.example.xyy.showstep;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
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
    public long s_step[] = null;
    public long s_start_time[] = null;
    public long s_stop_time[] = null;

    final int BYTE_OF_LONG = 64;

    private Context context = null;

    // context = MainActivity.this
    public StepStateSaver(Context _context) {
        context = _context;

        // By default show step since boot
        long lastStepDefault         = 0;
        long lastStepDateTimeDefault = java.lang.System.currentTimeMillis()
                - android.os.SystemClock.elapsedRealtime();

        // Save last step and boot time and last step time
        SharedPreferences sharedPref = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
        lastStep         = sharedPref.getLong("lastStep", lastStepDefault);
        lastStepDateTime = sharedPref.getLong("lastStepDateTime", lastStepDateTimeDefault);

        if (lastStepDateTime != lastStepDateTimeDefault) {
            // The phone was rebooted. In the future, we might use absolute step since app installed.
            lastStep = 0;
            lastStepDateTime = lastStepDateTimeDefault;
        }

        s_step = new long[MAX_STEP_SAVE];
        s_start_time = new long[MAX_STEP_SAVE];
        s_stop_time = new long[MAX_STEP_SAVE];
        for (int i = MAX_STEP_SAVE-1; i >= 0; i--) {
            s_step[i]       = 0;
            s_start_time[i] = 0;
            s_stop_time[i]  = 0;
        }

        /*
        File stepRecFilePath = new File(context.getFilesDir(), "step_record");
        RandomAccessFile stepRecFile = null;
        try {
            stepRecFile = new RandomAccessFile(stepRecFilePath, "rw");
            long l = stepRecFile.length();
            Log.i("StepStateSaver", "Found old record with length " + l);
            if (l % (3*BYTE_OF_LONG) != 0) {  // Try fix
                stepRecFile.setLength(l - l % (3*BYTE_OF_LONG));
                l = stepRecFile.length();
            }
            int m = 0;
            if (l/(3*BYTE_OF_LONG) < MAX_STEP_SAVE) {
                m = MAX_STEP_SAVE - (int)(l/(3*BYTE_OF_LONG));
                stepRecFile.seek(0);
            } else {
                stepRecFile.seek((3*BYTE_OF_LONG)*(l/(3*BYTE_OF_LONG) - MAX_STEP_SAVE));
            }

            // Load saved data
            for (int i = MAX_STEP_SAVE-1; i >= m; i--) {
                s_step      [i - m] = stepRecFile.readLong();
                s_start_time[i - m] = stepRecFile.readLong();
                s_stop_time [i - m] = stepRecFile.readLong();
            }
            stepRecFile.close();
        } catch (FileNotFoundException e) {
            // Nothing have to be done
            Log.i("StepStateSaver", "No old record found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    public void saveState() {
        // Save last step and boot time and last step time
        SharedPreferences sharedPref = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("lastStep", lastStep);
        editor.commit();
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
    }

    public void restartCounter() {
        if (stepSince() == 0) {
            // No new step. So do nothing
            Log.i("StepStateSaver", "No new step. So do nothing");
            return;
        }
        Log.i("StepStateSaver", "restartCounter()");
        // save new value and shift old values
        for (int i = MAX_STEP_SAVE-1; i > 0; i--) {
            s_step[i]       = s_step[i-1];
            s_start_time[i] = s_start_time[i-1];
            s_stop_time[i]  = s_stop_time[i-1];
        }
        Log.i("StepStateSaver", "restartCounter(): shift");
        s_step[0] = stepSince();
        s_start_time[0] = lastStepDateTime;
        s_stop_time[0] = java.lang.System.currentTimeMillis();
        Log.i("StepStateSaver", "restartCounter(): set step");

        // Save to file
        File stepRecFilePath = new File(context.getFilesDir(), "step_record.dat");
        Log.i("StepStateSaver", "restartCounter(): getFilesDir: " + stepRecFilePath.getAbsolutePath());
        RandomAccessFile stepRecFile = null;
        try {
            stepRecFile = new RandomAccessFile(stepRecFilePath, "w");
            Log.i("StepStateSaver", "restartCounter(): file open");
            stepRecFile.seek(stepRecFile.length());
            stepRecFile.writeLong(s_step[0]);
            stepRecFile.writeLong(s_start_time[0]);
            stepRecFile.writeLong(s_stop_time[0]);
            stepRecFile.close();
            Log.i("StepStateSaver", "restartCounter(): data written");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("StepStateSaver", "restartCounter(): IOException");
        }

        /*
        String FILENAME = "hello_file";
        String string = "hello world!";
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        resetCounter();
        Log.i("StepStateSaver", "restartCounter(): resetCounter()");
    }

    public long stepSince() {
        return step - lastStep;
    }
}
