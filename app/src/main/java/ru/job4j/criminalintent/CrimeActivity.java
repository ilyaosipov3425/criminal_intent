package ru.job4j.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Класс CrimeActivity - реализует метод абстрактного класса
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 30.05.2019
 * @version $Id$
 */

public class CrimeActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeFragment();
    }
}
