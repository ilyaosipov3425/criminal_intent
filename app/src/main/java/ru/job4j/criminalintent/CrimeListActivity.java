package ru.job4j.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Класс CrimeListActivity - реализует метод абстрактного класса
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 30.05.2019
 * @version $Id$
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
