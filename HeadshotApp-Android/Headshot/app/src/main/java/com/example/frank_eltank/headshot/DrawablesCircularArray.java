package com.example.frank_eltank.headshot;

/***
 *
 * Author: Frank Lin
 * Email: fylin134@gmail.com
 *
 */

import java.util.ArrayList;

/**
 * Created by Frank on 1/6/2016.
 *
 * This is a helper class in the form of a circular array.
 *
 * The circular array is loaded with the cutout drawables' resource ids
 *
 * When the OnSwipeTouchListener detects a swipe, it can access this
 * array to get the next cutout of unending swipes.
 */
public class DrawablesCircularArray {

    private ArrayList<Integer> mDrawables;
    private static int currentCutoutPointer = 0;

    public DrawablesCircularArray(){
        mDrawables = new ArrayList<Integer>();
        // TODO: This can definitely be improved somehow
        // to dynamically load all the available drawables from a separate directory
        mDrawables.add(R.drawable.family_of_mice_co);
        mDrawables.add(R.drawable.suit_nd);
    }

    public void addDrawable(int id){
        mDrawables.add(id);
    }

    public int getCurrentDrawable(){
        return mDrawables.get(currentCutoutPointer);
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
