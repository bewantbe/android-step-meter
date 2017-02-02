package com.example.xyy.showstep;

import android.content.Context;

/**
 * Step state process.
 * Created by xyy on 1/17/17.
 */
class StepStateSaver {

    // class for step state
    static class StepItem {
        long count;
        long start_time;
        long stop_time;

        StepItem(long _count, long _start_time, long _stop_time) {
            this.count = _count;
            this.start_time = _start_time;
            this.stop_time = _stop_time;
        }
    }

    private StepFileAccess stepFile = null;
    private StepItem lastStepState;
    private StepItem currentStepState;   // Current step and time
    final int MAX_STEP_SAVE = 10;   // Max number of step state saved and shown
    StepItem sStep[] = null;

    StepStateSaver(Context context) {
        // Usually context = MainActivity.this
        stepFile = new StepFileAccess(context);

        // For caching the recent step data
        sStep = new StepItem[MAX_STEP_SAVE];
        for (int i = MAX_STEP_SAVE-1; i >= 0; i--) {
            sStep[i] = new StepItem(0, 0, 0);
        }
        stepFile.loadRecords(sStep, MAX_STEP_SAVE);

        long bootTime = java.lang.System.currentTimeMillis()
                - android.os.SystemClock.elapsedRealtime();
        lastStepState = new StepItem(0,0,0);
        stepFile.getLastStepState(lastStepState, bootTime);

        currentStepState = new StepItem(0, bootTime, bootTime);
    }

    /**
     * Cache the step counter.
     * @param _step: step since last boot
     */
    void putStep(long _step, long _time) {
        currentStepState.count     = _step;
        currentStepState.stop_time = _time;
    }

    /**
     * Reset lastStep as if step counter been reset
     */
    void resetCounter() {
        lastStepState.count     = currentStepState.count;
        lastStepState.stop_time = java.lang.System.currentTimeMillis();
        stepFile.saveLastStepState(lastStepState);
    }

    // Start a new step record
    void restartCounter() {
        if (stepSince() == 0) {
            // No new step
            if (sStep[0].count == 0) {
                // merge record
                sStep[0].stop_time = java.lang.System.currentTimeMillis();
                resetCounter();
                stepFile.overwriteLastRecord(sStep[0]);
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
        sStep[0].start_time = lastStepState.stop_time;
        sStep[0].stop_time  = currentStepState.stop_time;
        resetCounter();

        stepFile.addOneRecord(sStep[0]);
    }

    long stepSince() {
        return currentStepState.count - lastStepState.count;
    }
}
