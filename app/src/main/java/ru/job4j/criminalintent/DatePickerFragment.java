package ru.job4j.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * Класс DatePickerFragment - фрагмент для ввода даты
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 02.06.2019
 * @version $Id$
 */

public class DatePickerFragment extends DialogFragment {

    public static final String EXTRA_DATE = "ru.job4j.criminalintent.date";
    private static final String ARG_DATE = "date";
    private DatePicker mDatePicker;

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date date = (Date) Objects.requireNonNull(getArguments())
                .getSerializable(ARG_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_date, null);

        mDatePicker = v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            int yearDialog = mDatePicker.getYear();
                            int monthDialog = mDatePicker.getMonth();
                            int dayDialog = mDatePicker.getDayOfMonth();
                            Date dateDialog = new GregorianCalendar
                                    (yearDialog, monthDialog, dayDialog).getTime();
                            sendResult(Activity.RESULT_OK, dateDialog);
                        })
                .create();
    }

    private void sendResult(int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);
        getTargetFragment().
                onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
