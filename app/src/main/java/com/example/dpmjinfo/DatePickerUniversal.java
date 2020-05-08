package com.example.dpmjinfo;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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
    //private Calendar mCalendar;
    //private SimpleDateFormat mFormat;

    String dateFormat;

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
        //mFormat = new SimpleDateFormat(format, Locale.getDefault());

        dateFormat = format;
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
        DateTime d = DateTime.parse(mEditText.getText().toString(), DateTimeFormat.forPattern(dateFormat));

        int day = d.getDayOfMonth();
        int month = d.getMonthOfYear() - 1;
        int year = d.getYear();

        new DatePickerDialog(view.getContext(), this, year, month, day).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        DateTime d = new DateTime()
                .withYear(year)
                .withMonthOfYear(month + 1)
                .withDayOfMonth(dayOfMonth);

        this.mEditText.setText(d.toString(dateFormat));
    }
}