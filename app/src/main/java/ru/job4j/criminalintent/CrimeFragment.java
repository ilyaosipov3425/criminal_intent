package ru.job4j.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
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
    private static final String DIALOG_PHOTO = "DialogPhoto";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    private Crime mCrime;
    private Date date;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mCallButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private CheckBox mSolvedCheckBox;
    private ImageView mPhotoView;
    private ImageButton mPhotoButton;
    private Callbacks mCallbacks;
    private FragmentManager mManager;

    public interface Callbacks {
        void onCrimeUpdate(Crime crime);
    }

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
        if (getArguments() == null) return;
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
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
            updateCrime();
            updateDate();
        }
        if (requestCode == REQUEST_TIME) {
            date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateCrime();
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID};
            if (getActivity() == null) return;
            try (Cursor cursorContact = getActivity()
                    .getContentResolver()
                    .query(Objects.requireNonNull(contactUri),
                            queryFields,
                            null, null, null)) {
                if (Objects.requireNonNull(cursorContact).getCount() == 0) {
                    return;
                }
                cursorContact.moveToFirst();
                String suspect = cursorContact.getString(0);
                String contactId = cursorContact.getString(1);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);

                try (Cursor cursorPhone = getActivity()
                        .getContentResolver()
                        .query(Phone.CONTENT_URI, null,
                                String.format("%s = %s", Phone.CONTACT_ID, contactId),
                                null, null)) {
                    if (Objects.requireNonNull(cursorPhone).getCount() == 0) {
                        return;
                    }
                    cursorPhone.moveToFirst();
                    String number = cursorPhone.getString(cursorPhone.getColumnIndex(Phone.NUMBER));
                    if (number != null) {
                        mCallButton.setText(number);
                        updateCrime();
                        mCrime.setNumber(number);
                    } else {
                        mCallButton.setText(R.string.no_number);
                    }
                }
            }
        } else if (requestCode == REQUEST_PHOTO) {
            if (getActivity() == null) return;
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "ru.job4j.criminalintent.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateCrime();
            updatePhotoView();
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
                updateCrime();
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
                (buttonView, isChecked) -> {
                    mCrime.setSolved(isChecked);
                    updateCrime();
                });

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

        PackageManager packageManager = Objects.requireNonNull(getActivity()).getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
            mCallButton.setEnabled(false);
        }

        mPhotoView = v.findViewById(R.id.crime_photo);
        mPhotoView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),
                                mPhotoView.getWidth(), mPhotoView.getHeight());
                        mPhotoView.setImageBitmap(bitmap);
                        mPhotoView.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
        updatePhotoView();
        mPhotoView.setOnClickListener(this::onClickPhoto);

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(
                (view) -> {
                    Uri uri = FileProvider.getUriForFile(getActivity(),
                            "ru.job4j.criminalintent.fileprovider",
                            mPhotoFile);
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    List<ResolveInfo> cameraActivities = getActivity()
                            .getPackageManager().queryIntentActivities(captureImage,
                                    PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo activity : cameraActivities) {
                        getActivity().grantUriPermission(activity.activityInfo.packageName,
                                uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    startActivityForResult(captureImage, REQUEST_PHOTO);
                });

        return v;
    }

    private void onClickPhoto(View v) {
        DialogFragment dialog = CrimePhotoFragment.newInstance(mPhotoFile);
        if (getActivity() == null) return;
        dialog.show(getActivity().getSupportFragmentManager(), DIALOG_PHOTO);
    }

    private void onClickCall(View v) {
        Uri number = Uri.parse(String
                .format(getString(R.string.call_tel), mCallButton.getText()));
        if (getActivity() == null) return;
        Intent intentCall = ShareCompat.IntentBuilder.from(getActivity())
                .getIntent()
                .setAction(Intent.ACTION_DIAL)
                .setData(number);
        startActivity(intentCall);
    }

    private void onClickReport(View v) {
        if (getActivity() == null) return;
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
        mManager = getFragmentManager();
        DatePickerFragment dialog = DatePickerFragment
                .newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
        dialog.show(mManager, DIALOG_DATE);
    }

    private void onClickTime(View v) {
        mManager = getFragmentManager();
        TimePickerFragment dialog = TimePickerFragment
                .newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
        dialog.show(mManager, DIALOG_TIME);
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance(DateFormat.FULL)
                .format(mCrime.getDate()));
    }

    private void updateTime() {
        mTimeButton.setText(DateFormat.getTimeInstance(DateFormat.MEDIUM)
                .format(mCrime.getDate()));
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdate(mCrime);
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

    private void updatePhotoView() {
        if (mPhotoView == null || !mPhotoFile.exists()) {
            Objects.requireNonNull(mPhotoView).setImageDrawable(null);
            mPhotoView.setContentDescription(
                    getString(R.string.crime_photo_no_image_description));
        } else {
            if (getActivity() == null) return;
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(
                    getString(R.string.crime_photo_image_description));
        }
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}
