package com.example.frank_eltank.headshot;

import android.database.DataSetObserver;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Frank on 2/24/2016.
 */
public class InfinitePagerAdapter extends PagerAdapter{

    private PagerAdapter mAdapter;

    public InfinitePagerAdapter(PagerAdapter adapter){
        mAdapter = adapter;
    }

    @Override
    public int getCount() {
        if(getRealCount() == 0){
            return 0;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return mAdapter.isViewFromObject(view, object);
    }

    public int getRealCount(){
        return mAdapter.getCount();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int virtualPosition = position % getRealCount();

        // only expose virtual position to the inner mAdapter
        return mAdapter.instantiateItem(container, virtualPosition);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int virtualPosition = position % getRealCount();

        // only expose virtual position to the inner mAdapter
        mAdapter.destroyItem(container, virtualPosition, object);
    }

    /*
     * Delegate rest of methods directly to the inner mAdapter.
     */

    @Override
    public void finishUpdate(ViewGroup container) {
        mAdapter.finishUpdate(container);
    }

    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        mAdapter.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        return mAdapter.saveState();
    }

    @Override
    public void startUpdate(ViewGroup container) {
        mAdapter.startUpdate(container);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int virtualPosition = position % getRealCount();
        return mAdapter.getPageTitle(virtualPosition);
    }

    @Override
    public float getPageWidth(int position) {
        return mAdapter.getPageWidth(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mAdapter.setPrimaryItem(container, position, object);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return mAdapter.getItemPosition(object);
    }
}
