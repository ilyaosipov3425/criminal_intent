package ru.job4j.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Класс CrimePhotoFragment - выводит на экран увеличенное изображение
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 17.06.2019
 * @version $Id$
 */

public class CrimePhotoFragment extends DialogFragment {

    private static final String FILE_KEY = "photo_file";

    public static CrimePhotoFragment newInstance(File file) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(FILE_KEY, file);

        CrimePhotoFragment photoFragment = new CrimePhotoFragment();
        photoFragment.setArguments(bundle);
        return photoFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        File photoFile = (File) getArguments().getSerializable(FILE_KEY);

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_crime_photo, null);
        Bitmap bitmap = PictureUtils.getScaledBitmap(
                photoFile.getPath(), getActivity());

        ImageView photoView = view.findViewById(R.id.detail_view);
        photoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}
