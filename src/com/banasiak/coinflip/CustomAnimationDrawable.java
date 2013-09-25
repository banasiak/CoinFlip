/*
 *========================================================================
 * CustomAnimationDrawable.java
 * Sep 25, 2013 11:43 AM | variable
 * Copyright (c) 2013 Richard Banasiak
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

package com.banasiak.coinflip;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;

// AnimationDrawable does not provide a callback after the animation is
// finished.  This abstract class raises an onAnimationFinish() method
// after the final frame of the animation is rendered.
// http://stackoverflow.com/questions/2214735/android-animationdrawable-and-knowing-when-animation-ends

public abstract class CustomAnimationDrawable extends AnimationDrawable {

    // debugging tag
    private static final String TAG = "CustomAnimationDrawable";

    Handler mAnimationHandler;

    public CustomAnimationDrawable(AnimationDrawable aniDraw) {
        // Add each frame to this CustomAnimationDrawable
        for (int i = 0; i < aniDraw.getNumberOfFrames(); i++) {
            this.addFrame(aniDraw.getFrame(i), aniDraw.getDuration(i));
        }
    }

    @Override
    public void start() {
        super.start();

        Log.d(TAG, "start()");

        // Call super.start() to call the base class start animation method.
        // Then add a handler to call onAnimationFinish() when the total
        // duration for the animation has passed.

        mAnimationHandler = new Handler();
        mAnimationHandler.postDelayed(new Runnable() {
            public void run() {
                onAnimationFinish();
            }
        }, getTotalDuration());

    }

    public int getTotalDuration() {
        Log.d(TAG, "getTotalDuration()");

        int iDuration = 0;

        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            iDuration += this.getDuration(i);
        }

        return iDuration;
    }

    // called when the animation finishes
    abstract void onAnimationFinish();
}
