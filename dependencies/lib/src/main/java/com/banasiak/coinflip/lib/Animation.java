/*
 *========================================================================
 * Animation.java
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.EnumMap;

/**
 * Created by variable on 7/12/14.
 */
public class Animation {

    // debugging tag
    private static final String TAG = Animation.class.getSimpleName();

    // enumerator of all possible transition states
    public enum ResultState {
        HEADS_HEADS,
        HEADS_TAILS,
        TAILS_HEADS,
        TAILS_TAILS,
        UNKNOWN
    }

    private static EnumMap<ResultState, CustomAnimationDrawable> coinAnimationsMap;

    private Animation() {
        //singleton
    }

    public static void init() {
        coinAnimationsMap = new EnumMap<ResultState, CustomAnimationDrawable>(
                Animation.ResultState.class);
    }

    public static EnumMap<ResultState, CustomAnimationDrawable> getAllAnimations() {
        return coinAnimationsMap;
    }

    public static CustomAnimationDrawable getAnimation(ResultState state) {
        return coinAnimationsMap.get(state);
    }

    public static void clearAnimations() {
        coinAnimationsMap.clear();
    }

    public static EnumMap<ResultState, CustomAnimationDrawable> generateAnimations(
            final Drawable imageA, final Drawable imageB,
            final Drawable edge, final Drawable background) {
        Log.d(TAG, "generateAnimations()");

        Animation.ResultState resultState;
        CustomAnimationDrawable coinAnimation;

        final int widthA = ((BitmapDrawable) imageA).getBitmap().getWidth();
        final int heightA = ((BitmapDrawable) imageA).getBitmap().getHeight();
        final int widthB = ((BitmapDrawable) imageB).getBitmap().getWidth();
        final int heightB = ((BitmapDrawable) imageB).getBitmap().getHeight();

        BitmapDrawable bg = (BitmapDrawable) background;

        // create the individual animation frames for the heads side
        final BitmapDrawable imageA_8 = (BitmapDrawable) imageA;
        final BitmapDrawable imageA_6 = resizeBitmapDrawable(imageA_8, (int) (widthA * 0.75),
                heightA, bg);
        final BitmapDrawable imageA_4 = resizeBitmapDrawable(imageA_8, (int) (widthA * 0.50),
                heightA, bg);
        final BitmapDrawable imageA_2 = resizeBitmapDrawable(imageA_8, (int) (widthA * 0.25),
                heightA, bg);

        // create the individual animation frames for the tails side
        final BitmapDrawable imageB_8 = (BitmapDrawable) imageB;
        final BitmapDrawable imageB_6 = resizeBitmapDrawable(imageB_8, (int) (widthB * 0.75),
                heightB, bg);
        final BitmapDrawable imageB_4 = resizeBitmapDrawable(imageB_8, (int) (widthB * 0.50),
                heightB, bg);
        final BitmapDrawable imageB_2 = resizeBitmapDrawable(imageB_8, (int) (widthB * 0.25),
                heightB, bg);

        // the temporary bitmaps have already been cleared, this might be a good place for a garbage collection
        System.gc();

        // create the appropriate animation depending on the result state
        resultState = Animation.ResultState.HEADS_HEADS;
        coinAnimation = generateAnimatedDrawable(imageA_8, imageA_6, imageA_4, imageA_2,
                imageB_8, imageB_6, imageB_4, imageB_2,
                (BitmapDrawable) edge, resultState);
        coinAnimationsMap.put(resultState, coinAnimation);

        resultState = Animation.ResultState.HEADS_TAILS;
        coinAnimation = generateAnimatedDrawable(imageA_8, imageA_6, imageA_4, imageA_2,
                imageB_8, imageB_6, imageB_4, imageB_2,
                (BitmapDrawable) edge, resultState);
        coinAnimationsMap.put(resultState, coinAnimation);

        resultState = Animation.ResultState.TAILS_HEADS;
        coinAnimation = generateAnimatedDrawable(imageA_8, imageA_6, imageA_4, imageA_2,
                imageB_8, imageB_6, imageB_4, imageB_2,
                (BitmapDrawable) edge, resultState);
        coinAnimationsMap.put(resultState, coinAnimation);

        resultState = Animation.ResultState.TAILS_TAILS;
        coinAnimation = generateAnimatedDrawable(imageA_8, imageA_6, imageA_4, imageA_2,
                imageB_8, imageB_6, imageB_4, imageB_2,
                (BitmapDrawable) edge, resultState);
        coinAnimationsMap.put(resultState, coinAnimation);

        return coinAnimationsMap;

    }

    private static BitmapDrawable resizeBitmapDrawable(final BitmapDrawable image,
            final int width, final int height, final BitmapDrawable background) {
        Log.d(TAG, "resizeBitmapDrawable()");

        // TODO: resize the bitmaps proportional to 80% of the screen size

        // load the transparent background and convert to a bitmap
        Bitmap background_bm = background.getBitmap();

        // convert the passed in image to a bitmap and resize according to parameters
        Bitmap image_bm = Bitmap.createScaledBitmap(image.getBitmap(), width, height, true);

        // create a new canvas to combine the two images on
        Bitmap comboImage_bm = Bitmap.createBitmap(background_bm.getWidth(),
                background_bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(comboImage_bm);

        // add the background as well as the new image to the horizontal center
        // of the image
        comboImage.drawBitmap(background_bm, 0f, 0f, null);
        comboImage.drawBitmap(image_bm, (background_bm.getWidth() - image_bm.getWidth()) / 2, 0f,
                null);

        // convert the new combo image bitmap to a BitmapDrawable
        final BitmapDrawable comboImage_bmd = new BitmapDrawable(comboImage_bm);

        // I don't know if this is the right thing to do, but this method
        // usually always blows out the heap on pre-Froyo devices.  Clearing
        // the temporary resources and recommending a GC seems to help.
        background_bm = null;
        image_bm = null;
        comboImage_bm = null;
        comboImage = null;

        return comboImage_bmd;
    }

    private static CustomAnimationDrawable generateAnimatedDrawable(final BitmapDrawable imageA_8,
            final BitmapDrawable imageA_6,
            final BitmapDrawable imageA_4,
            final BitmapDrawable imageA_2,
            final BitmapDrawable imageB_8,
            final BitmapDrawable imageB_6,
            final BitmapDrawable imageB_4,
            final BitmapDrawable imageB_2,
            final BitmapDrawable edge,
            final Animation.ResultState resultState) {
        Log.d(TAG, "generateAnimatedDrawable()");

        final int duration = 20;
        final CustomAnimationDrawable animation = new CustomAnimationDrawable();

        // create the appropriate animation depending on the result state
        switch (resultState) {
            case HEADS_HEADS:
                // Begin Flip 1
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 2
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 3
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                break;
            case HEADS_TAILS:
                // Begin Flip 1
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 2
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 3 (half flip)
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                break;
            case TAILS_HEADS:
                // Begin Flip 1
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 2
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 3 (half flip)
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                break;
            case TAILS_TAILS:
                // Begin Flip 1
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 2
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 3
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                break;
            default:
                Log.w(TAG, "Invalid state. Resetting coin.");
                //resetCoin();
                break;
        }

        animation.setOneShot(true);

        return animation;
    }
}
