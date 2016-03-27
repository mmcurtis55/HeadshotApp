package com.example.frank_eltank.headshot;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

/**
 * Created by Frank on 2/24/2016.
 */
public class MinFragmentPagerAdapter extends FragmentPagerAdapter {

    private FragmentPagerAdapter mAdapter;

    public MinFragmentPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void setAdapter(FragmentPagerAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public int getCount() {
        int realCount = mAdapter.getCount();
        if (realCount == 1) {
            return 4;
        } else if (realCount == 2 || realCount == 3) {
            return realCount * 2;
        } else {
            return realCount;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return mAdapter.isViewFromObject(view, object);
    }

    /**
     * Warning: If you only have 1-3 real pages, this method will create multiple, duplicate
     * instances of your Fragments to ensure wrap-around is possible. This may be a problem if you
     * have editable fields or transient state (they will not be in sync).
     *
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {
        int realCount = mAdapter.getCount();
        if (realCount == 1) {
            return mAdapter.getItem(0);
        } else if (realCount == 2 || realCount == 3) {
            return mAdapter.getItem(position % realCount);
        } else {
            return mAdapter.getItem(position);
        }
    }
}
