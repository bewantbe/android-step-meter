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

    public StepListAdapter(Context context, int resource, StepStateSaver.StepItem[] step_date) {
        super(context, resource, step_date);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StepStateSaver.StepItem step = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.step_date_list_item_1, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText("S = " + step.count);
        ((TextView) convertView.findViewById(R.id.text2)).setText(
                "From " + step.start_time + "  To " + step.stop_time);
        return convertView;
    }
}
