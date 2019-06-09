package ru.job4j.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

import ru.job4j.criminalintent.database.CrimeDbSchema.CrimeTable;
import ru.job4j.criminalintent.model.Crime;

/**
 * Класс CrimeCursorWrapper - получение данных из cursor
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 07.06.2019
 * @version $Id$
 */

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String number = getString(getColumnIndex(CrimeTable.Cols.NUMBER));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);
        crime.setNumber(number);

        return crime;
    }
}
