package com.example.dpmjinfo;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Pratik Butani
 *
 * Main file for Date Picker.
 */
public class DatePickerUniversal implements View.OnFocusChangeListener, DatePickerDialog.OnDateSetListener, View.OnClickListener {

    private EditText mEditText;
    private Calendar mCalendar;
    private SimpleDateFormat mFormat;

    /**
     * Constructor
     *
     * @param editText your EditText
     * @param format   give your format in which you want date like dd/MM/yyyy
     */
    public DatePickerUniversal(EditText editText, String format) {
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

        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int month = mCalendar.get(Calendar.MONTH);
        int year = mCalendar.get(Calendar.YEAR);

        new DatePickerDialog(view.getContext(), this, year, month, day).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        this.mEditText.setText(mFormat.format(mCalendar.getTime()));
    }
}