package com.example.xyy.showstep;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorStep;

    TextView tv_step = null;
    ListView listView_step_rec = null;

    private StepStateSaver stepState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorStep = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // the number of steps taken by the user since the last reboot while the sensor was activated

        stepState = new StepStateSaver(MainActivity.this);
        tv_step = (TextView) findViewById(R.id.tv_step);
        if (mSensorStep == null) {
            // No this sensor
            tv_step.setText("No this sensor");
            // Then ??
        }

        listView_step_rec = (ListView) findViewById(R.id.listView_step_rec);
        listView_step_rec.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getStepList()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume", "() - 1");
        mSensorManager.registerListener(this, mSensorStep, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "()");
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "()");
    }

    // For view update
    CharSequence ISOTime(long tm) {
        return android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss", tm);
    }

    private List<String> getStepList() {
        List<String> data = new ArrayList<String>();
        for (int i = 0; i < stepState.MAX_STEP_SAVE; i++) {
            if (stepState.sStep[i].stop_time == 0) {
                continue;
            }
            data.add("S=" + stepState.sStep[i].count
                    + "  " + ISOTime(stepState.sStep[i].start_time)
                    + "\n   To " + ISOTime(stepState.sStep[i].stop_time));
        }
        return data;
    }

    public void updateView() {
        // Maybe use notifyDataSetChanged()?
        listView_step_rec.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getStepList()));
        listView_step_rec.invalidate();
        tv_step.setText("Step = " + stepState.stepSince() + "\nSince " + ISOTime(stepState.lastStepDateTime));
    }

    // Sensor events process
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    int event_cnt = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            event_cnt++;
            stepState.putStep((long) event.values[0]);
            updateView();
        }
    }

    // Button event
    public void counterButton(View view) {
        switch (view.getId()) {
            case R.id.button_reset_counter:
                Log.w("counterButton", "resetCounter Pressed");
                stepState.resetCounter();
                updateView();
                break;
            case R.id.button_restart_counter:
                Log.w("counterButton", "restartCounter Pressed");
                stepState.restartCounter();
                updateView();
                break;
            default:
                Log.e("counterButton", "Pressed unknown button");
        }
    }
}
