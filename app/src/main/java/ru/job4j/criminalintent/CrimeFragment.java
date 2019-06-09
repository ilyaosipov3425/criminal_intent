package ru.job4j.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import ru.job4j.criminalintent.model.Crime;
import ru.job4j.criminalintent.database.CrimeLab;

/**
 * Класс CrimeFragment - выдает подробную информацию о конкретном преступлении и ее обновление при модификации пользователем
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 30.05.2019
 * @version $Id$
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;

    private Crime mCrime;
    private Date date;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mCallButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private CheckBox mSolvedCheckBox;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }
        if (requestCode == REQUEST_TIME) {
            date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID};
            Cursor cursorContact = getActivity()
                    .getContentResolver()
                    .query(Objects.requireNonNull(contactUri),
                            queryFields,
                            null, null, null);
            try {
                if (cursorContact.getCount() == 0) {
                    return;
                }
                cursorContact.moveToFirst();
                String suspect = cursorContact.getString(0);
                String contactId = cursorContact.getString(1);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);

                Cursor cursorPhone = getActivity()
                        .getContentResolver()
                        .query(Phone.CONTENT_URI, null,
                                String.format("%s = %s", Phone.CONTACT_ID, contactId),
                                null, null);
                try {
                    if (cursorPhone.getCount() == 0) {
                        return;
                    }
                    cursorPhone.moveToFirst();
                    String number = cursorPhone.getString(cursorPhone.getColumnIndex(Phone.NUMBER));
                    if (number != null) {
                        mCallButton.setText(number);
                        mCrime.setNumber(number);
                    } else {
                        mCallButton.setText(R.string.no_number);
                    }
                } finally {
                    cursorPhone.close();
                }
            } finally {
                cursorContact.close();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(this::onClickDate);

        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(this::onClickTime);

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> mCrime.setSolved(isChecked));

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(this::onClickReport);

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(
                (view) -> startActivityForResult(pickContact, REQUEST_CONTACT)
        );

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mCallButton = v.findViewById(R.id.crime_call);
        mCallButton.setOnClickListener(this::onClickCall);
        if (mCrime.getNumber() != null) {
            mCallButton.setText(mCrime.getNumber());
        } else {
            mCallButton.setText(R.string.no_number);
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
            mCallButton.setEnabled(false);
        }

        return v;
    }

    private void onClickCall(View v) {
        Uri number = Uri.parse(String
                .format(getString(R.string.call_tel), mCallButton.getText()));
        Intent intentCall = ShareCompat.IntentBuilder.from(getActivity())
                .getIntent()
                .setAction(Intent.ACTION_DIAL)
                .setData(number);
        startActivity(intentCall);
    }

    private void onClickReport(View v) {
        Intent intentShare = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .getIntent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
        intentShare = Intent.createChooser(intentShare, getString(R.string.send_report));
        startActivity(intentShare);
    }

    private void onClickDate(View v) {
        FragmentManager manager = getFragmentManager();
        DatePickerFragment dialog = DatePickerFragment
                .newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
        dialog.show(manager, DIALOG_DATE);
    }

    private void onClickTime(View v) {
        FragmentManager manager = getFragmentManager();
        TimePickerFragment dialog = TimePickerFragment
                .newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
        dialog.show(manager, DIALOG_TIME);
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance(DateFormat.FULL)
                .format(mCrime.getDate()));
    }

    private void updateTime() {
        mTimeButton.setText(DateFormat.getTimeInstance(DateFormat.MEDIUM)
                .format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateFormat.getDateInstance(DateFormat.FULL)
                .format(mCrime.getDate());

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                Objects.requireNonNull(getActivity()).finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }
}
