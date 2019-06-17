package ru.job4j.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

import ru.job4j.criminalintent.model.Crime;
import ru.job4j.criminalintent.database.CrimeLab;

/**
 * Класс CrimePagerActivity - позволяет "листать" элементы списка, проводя пальцом по экрану
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 01.06.2019
 * @version $Id$
 */

public class CrimePagerActivity extends AppCompatActivity
        implements CrimeFragment.Callbacks {

    private static final String EXTRA_CRIME_ID = "ru.job4j.criminalintent.crime_id";
    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button mFirstPage;
    private Button mLastPage;

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        mFirstPage = findViewById(R.id.first_crime_page);
        mFirstPage.setOnClickListener(this::onFirstClick);

        mLastPage = findViewById(R.id.last_crime_page);
        mLastPage.setOnClickListener(this::onLastClick);

        mViewPager = findViewById(R.id.crime_view_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    mFirstPage.setEnabled(false);
                } else {
                    mFirstPage.setEnabled(true);
                }
                if (i == mViewPager.getAdapter().getCount() - 1) {
                    mLastPage.setEnabled(false);
                } else {
                    mLastPage.setEnabled(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void onFirstClick(View v) {
        mViewPager.setCurrentItem(0);
    }

    private void onLastClick(View v) {
        mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1);
    }

    @Override
    public void onCrimeUpdate(Crime crime) {
    }
}
