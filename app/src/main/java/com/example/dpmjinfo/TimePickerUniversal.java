package com.example.dpmjinfo;

import android.app.TimePickerDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimePickerUniversal implements View.OnFocusChangeListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private EditText mEditText;
    private Calendar mCalendar;
    private SimpleDateFormat mFormat;

    /**
     * Constructor
     *
     * @param editText your EditText
     * @param format   give your format in which you want time like HH:mm
     */
    public TimePickerUniversal(EditText editText, String format) {
        this.mEditText = editText;
        mEditText.setOnFocusChangeListener(this);
        mEditText.setOnClickListener(this);
        mFormat = new SimpleDateFormat(format, Locale.getDefault());
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            showPicker(view);
        }
    }

    @Override
    public void onClick(View view) {
        showPicker(view);
    }

    private void showPicker(View view) {
        if (mCalendar == null)
            mCalendar = Calendar.getInstance();

        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        new TimePickerDialog(view.getContext(), this, hour, minute, true).show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

        this.mEditText.setText(mFormat.format(mCalendar.getTime()));
    }
}
