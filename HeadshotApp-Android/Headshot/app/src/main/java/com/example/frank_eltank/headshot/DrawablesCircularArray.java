package com.example.frank_eltank.headshot;

import java.util.ArrayList;

/**
 * Created by Frank on 1/6/2016.
 */
public class DrawablesCircularArray {

    private ArrayList<Integer> mDrawables;
    private static int currentCutoutPointer = 0;

    public DrawablesCircularArray(){
        mDrawables = new ArrayList<Integer>();
        mDrawables.add(R.drawable.suit_nd);
        mDrawables.add(R.drawable.placeholder_overlay_blue);
    }

    public void addDrawable(int id){
        mDrawables.add(id);
    }

    public int getPreviousDrawable(){
        currentCutoutPointer--;
        if(currentCutoutPointer < 0){
            currentCutoutPointer = mDrawables.size()-1;
        }
        return mDrawables.get(currentCutoutPointer);
    }

    public int getNextDrawable(){
        currentCutoutPointer++;
        if(currentCutoutPointer >  mDrawables.size()-1){
            currentCutoutPointer = 0;
        }
        return mDrawables.get(currentCutoutPointer);
    }
}
