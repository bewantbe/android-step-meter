package com.example.xyy.showstep;

/**
 * Created by xyy on 1/19/17.
 * Custom ArrayAdapter
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StepListAdapter extends ArrayAdapter<StepStateSaver.StepItem> {
    private LayoutInflater mInflater;
    private int resource;
    private int len = 0;
    private StepStateSaver.StepItem[] s_step_date = null;

    @Override
    //public int getCount() { return len; }
    public int getCount() {
        while (len < s_step_date.length && s_step_date[len].stop_time != 0) {
            len ++;
        }
        while (len > 0 && s_step_date[len-1].stop_time == 0) {
            len --;
        }
        return len;
    }

    public StepListAdapter(Context context, int _resource, StepStateSaver.StepItem[] _s_step_date) {
        super(context, _resource, _s_step_date);
        mInflater = LayoutInflater.from(context);
        resource = _resource;
        s_step_date = _s_step_date;
    }

    CharSequence ISOTime(long tm) {
        return android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss", tm);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StepStateSaver.StepItem step = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText("S = " + step.count);
        ((TextView) convertView.findViewById(R.id.text2)).setText(
                "From " + ISOTime(step.start_time) + "\n  To " + ISOTime(step.stop_time));
        return convertView;
    }
}
