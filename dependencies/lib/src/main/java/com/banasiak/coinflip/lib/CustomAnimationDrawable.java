/*
 *========================================================================
 * CustomAnimationDrawable.java
 * Mar 16, 2014 2:43 PM | variable
 * Copyright (c) 2015 Richard Banasiak
 *========================================================================
 * This file is part of CoinFlip.
 *
 *    CoinFlip is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    CoinFlip is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with CoinFlip.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.banasiak.coinflip.lib;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;

// AnimationDrawable does not provide a callback after the animation is finished.  This class
// provides an interface for an onAnimationFinish() callback method which is called after the final
// frame of the animation is rendered.
//
// Loosely based on this thread: http://stackoverflow.com/a/6641321

public class CustomAnimationDrawable extends AnimationDrawable {

    // debugging tag
    private static final String TAG = CustomAnimationDrawable.class.getSimpleName();

    Handler mAnimationHandler;
    Runnable mAnimationRunable;

    public AnimationCallback getAnimationCallback() {
        return mAnimationCallback;
    }

    public void setAnimationCallback(AnimationCallback animationCallback) {
        mAnimationCallback = animationCallback;
    }

    private AnimationCallback mAnimationCallback;

    public CustomAnimationDrawable() {
        super();
    }

    @Override
    public void start() {
        super.start();

        Log.d(TAG, "start()");

        // Call super.start() to call the base class start animation method.
        // Then add a handler to call onAnimationFinish() when the total
        // duration for the animation has passed.

        mAnimationRunable = new Runnable() {
            @Override public void run() {
                if (mAnimationCallback != null) {
                    mAnimationCallback.onAnimationFinish();
                }
            }
        };

        mAnimationHandler = new Handler();
        mAnimationHandler.postDelayed(mAnimationRunable, getTotalDuration());

    }

    public int getTotalDuration() {
        Log.d(TAG, "getTotalDuration()");

        int iDuration = 0;

        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            iDuration += this.getDuration(i);
        }

        return iDuration;
    }

    // call via onPause() to shut this thread down when the app drops to the background.
    public void removeCallbacks() {
        Log.d(TAG, "removeCallbacks()");
        mAnimationHandler.removeCallbacks(mAnimationRunable);
    }

    // called when the animation finishes
    public interface AnimationCallback {

        public void onAnimationFinish();
    }
}
