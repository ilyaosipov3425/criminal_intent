package ru.job4j.criminalintent.database;

/**
 * Класс CrimeDbSchema - опеределяет схему таблицы базы данных
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 06.06.2019
 * @version $Id$
 */

public class CrimeDbSchema {

    public static final class CrimeTable {
        public static final String NAME = "crimes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
            public static final String NUMBER = "number";
        }
    }
}
