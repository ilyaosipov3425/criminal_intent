package ru.job4j.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

/**
 * Класс CrimeActivity - реализует метод абстрактного класса
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 30.05.2019
 * @version $Id$
 */

public class CrimeActivity extends SingleFragmentActivity {

    private static final String EXTRA_CRIME_ID = "ru.job4j.creminalintent.crime_id";

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        return CrimeFragment.newInstance(crimeId);
    }
}
