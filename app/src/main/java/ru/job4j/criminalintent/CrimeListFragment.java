package ru.job4j.criminalintent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ru.job4j.criminalintent.model.Crime;
import ru.job4j.criminalintent.database.CrimeLab;

/**
 * Класс CrimeListFragment - выдает списковое представление преступление
 * @author Ilya Osipov (mailto:il.osipov.ya@yandex.ru)
 * @since 30.05.2019
 * @version $Id$
 */

public class CrimeListFragment extends Fragment {

    private final static String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private TextView mViewEmpty;
    private ImageButton mButtonEmpty;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    private OnDeleteCrimeListener mDeleteCallback;

    public interface OnDeleteCrimeListener {
        void onCrimeIdSelected(UUID crimeId);
    }

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCrimeRecyclerView.setHasFixedSize(true);
        setCrimeItemTouchListener();

        mViewEmpty = view.findViewById(R.id.empty_list_view);
        mButtonEmpty = view.findViewById(R.id.empty_add_button);
        mButtonEmpty.setOnClickListener(
                (v) -> onAddClick());

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final ImageView mSolvedImageView;
        private Crime mCrime;

        private CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        private void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM)
                    .format(mCrime.getDate()));
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends  RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        private CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);

            if ((position % 2) == 0) {
                holder.itemView.setBackgroundColor(Color.parseColor("#fff8e1"));
            }
        }

        @Override
        public int getItemCount() {
            if (mCrimes.size() == 0) {
                mCrimeRecyclerView.setVisibility(View.INVISIBLE);
                mViewEmpty.setVisibility(View.VISIBLE);
                mButtonEmpty.setVisibility(View.VISIBLE);
            } else {
                mCrimeRecyclerView.setVisibility(View.VISIBLE);
                mViewEmpty.setVisibility(View.INVISIBLE);
                mButtonEmpty.setVisibility(View.INVISIBLE);
            }
            return mCrimes.size();
        }

        private void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                onAddClick();
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                Objects.requireNonNull(getActivity())
                        .invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setCrimeItemTouchListener() {
        ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder viewHolder1) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                        int position = viewHolder.getAdapterPosition();
                        Crime crime = mAdapter.mCrimes.get(position);
                        mDeleteCallback.onCrimeIdSelected(crime.getId());
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY, int actionState,
                                            boolean isCurrentlyActive) {
                        View itemView = viewHolder.itemView;

                        Drawable deleteIcon = ContextCompat
                                .getDrawable(Objects.requireNonNull(getContext()),
                                R.drawable.ic_menu_delete);
                        float iconHeight = Objects.requireNonNull(deleteIcon).getIntrinsicHeight();
                        float iconWidth = deleteIcon.getMinimumWidth();
                        float itemHeight = itemView.getBottom() - itemView.getTop();

                        Resources resources = getResources();
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setColor(resources.getColor(R.color.colorAccent));
                        RectF layout = new RectF(itemView.getLeft(), itemView.getTop(),
                                itemView.getRight(), itemView.getBottom());
                        c.drawRect(layout, paint);

                        int deleteIconTop = (int) (itemView.getTop() + (itemHeight - iconHeight) / 2);
                        int deleteIconBottom = (int) (deleteIconTop + iconHeight);
                        int deleteIconMargin = (int) ((itemHeight - iconHeight) / 2);
                        int deleteIconLeft = (int) (itemView.getRight() - deleteIconMargin - iconWidth);
                        int deleteIconRight = itemView.getRight() - deleteIconMargin;

                        deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight,
                                deleteIconBottom);
                        deleteIcon.draw(c);

                        getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView,
                                dX, dY, actionState, isCurrentlyActive);
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }



    public void deleteCrime(UUID crimeId) {
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
    }

    private void onAddClick() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
        updateUI();
        mCallbacks.onCrimeSelected(crime);
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeSize = crimeLab.getCrimes().size();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural, crimeSize, crimeSize);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        Objects.requireNonNull(activity.getSupportActionBar())
                .setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        mDeleteCallback = (OnDeleteCrimeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mDeleteCallback = null;
    }
}
