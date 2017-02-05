package com.example.xyy.showstep;

/**
 * Created by xyy on 1/19/17.
 * Custom ArrayAdapter
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class StepListAdapter extends ArrayAdapter<StepStateSaver.StepItem> {
    private LayoutInflater mInflater;
    private int resource;
    private int len = 0;
    private StepStateSaver.StepItem[] s_step_data = null;
    private Context context;

    StepListAdapter(Context _context, int _resource, StepStateSaver.StepItem[] _s_step_date) {
        super(_context, _resource, _s_step_date);
        context = _context;
        mInflater = LayoutInflater.from(context);
        resource = _resource;
        s_step_data = _s_step_date;
    }

    // Consider also override ArrayAdapter(Context context, int resource, List<T> objects)
    // So the ListView can have dynamical length?

    @Override
    public int getCount() {
        if (s_step_data == null) {
            Log.e("StepListAdapter", "s_step_data == null: should not happen!!");
            return 0;
        }
        // Is there simpler way to skip empty record?
        // Will this help notifyDataSetChanged() ?
        while (len < s_step_data.length && s_step_data[len].stop_time != 0) {
            len ++;
        }
        while (len > 0 && s_step_data[len-1].stop_time == 0) {
            len --;
        }
        return len;
    }

    private CharSequence ISOTime(long tm) {
        return android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss", tm);
    }

    private int TimeDiffPositionRaw(CharSequence st1, CharSequence st2) {
        int i = 0;
        // st1 and st2 are the same up-to i.
        while (i < st1.length() && i < st2.length()) {
            if (st1.charAt(i) != st2.charAt(i)) {
                break;
            }
            i++;
        }
        return i;
    }

    private int TimeDiffPosition(CharSequence st1, CharSequence st2) {
        int i = TimeDiffPositionRaw(st1, st2);
        if (i < 5) i = 0;
        else if (i < 8) i = 5;
        else if (i < 11) i = 8;
        else i = 11;
        return i;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        StepStateSaver.StepItem step = getItem(len - position - 1);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = mInflater.inflate(resource, parent, false);
        }
        if (step != null) {
            CharSequence cs1 = ISOTime(step.start_time);
            CharSequence cs2 = ISOTime(step.stop_time);
            int pos = TimeDiffPosition(cs1, cs2);
            ((TextView) convertView.findViewById(R.id.text1)).setText(
                    context.getString(R.string.listview_step_template, step.count));
            ((TextView) convertView.findViewById(R.id.text2)).setText(
                    context.getString(R.string.listview_step_time_template,
                            cs1, cs2.subSequence(pos, cs2.length())));
        } else {
            ((TextView) convertView.findViewById(R.id.text1)).setText("");
            ((TextView) convertView.findViewById(R.id.text2)).setText("");
        }
        return convertView;
    }
}
