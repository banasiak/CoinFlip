/*
 *========================================================================
 * CoinFlipActivity.java
 * Jul 12, 2014 6:39 PM | variable
 * Copyright (c) 2014 Richard Banasiak
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
package com.banasiak.coinflip.wear;

import com.banasiak.coinflip.lib.Animation;
import com.banasiak.coinflip.lib.Coin;
import com.banasiak.coinflip.lib.CustomAnimationDrawable;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CoinFlipActivity extends Activity {

    // debugging tag
    private static final String TAG = CoinFlipActivity.class.getSimpleName();

    private final Coin theCoin = new Coin();

    private View.OnClickListener tapper;

    private Boolean currentResult = true;

    private Boolean previousResult = true;

    private CustomAnimationDrawable coinAnimationCustom;

    private ImageView coinImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Log.d(TAG, "Proper layout inflated");
                coinImage = (ImageView) findViewById(R.id.coin_image_view);
                tapper = new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        flipCoin();
                    }
                };
                coinImage.setOnClickListener(tapper);
                Animation.init();
                loadResources();
            }
        });
    }

    private void flipCoin() {
        Log.d(TAG, "flipCoin()");

        // we're in the process of flipping the coin
        Animation.ResultState resultState = Animation.ResultState.UNKNOWN;

        // pause the shake listener until the result is rendered
        pauseListeners();

        boolean flipResult = theCoin.flip();

        resultState = updateState(flipResult);

        // update the screen with the result of the flip
        renderResult(resultState);
    }

    private Animation.ResultState updateState(final boolean flipResult) {
        // Analyze the current coin state and the new coin state and determine
        // the proper transition between the two.
        // true = HEADS | false = TAILS

        Log.d(TAG, "updateState()");

        Animation.ResultState resultState = Animation.ResultState.UNKNOWN;
        currentResult = flipResult;

        // this is easier to read than the old code
        if (previousResult == true && currentResult == true) {
            resultState = Animation.ResultState.HEADS_HEADS;
        }
        if (previousResult == true && currentResult == false) {
            resultState = Animation.ResultState.HEADS_TAILS;
        }
        if (previousResult == false && currentResult == true) {
            resultState = Animation.ResultState.TAILS_HEADS;
        }
        if (previousResult == false && currentResult == false) {
            resultState = Animation.ResultState.TAILS_TAILS;
        }

        // update the previousResult for the next flip
        previousResult = currentResult;

        return resultState;
    }

    private void renderResult(final Animation.ResultState resultState) {
        Log.d(TAG, "renderResult()");

        AnimationDrawable coinAnimation;

        coinAnimation = Animation.getAnimation(resultState);
        coinAnimationCustom = new CustomAnimationDrawable(coinAnimation) {
            @Override
            protected void onAnimationFinish() {
                final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                resumeListeners();
            }
        };

        //coinImage.setBackgroundDrawable(coinAnimationCustom);
        coinImage.setBackground(null);
        coinImage.setImageDrawable(coinAnimationCustom);
        coinAnimationCustom.start();
    }

    private void pauseListeners() {
        Log.d(TAG, "pauseListeners()");
        if (tapper != null) {
            coinImage.setOnClickListener(null);
        }
    }

    private void resumeListeners() {
        Log.d(TAG, "resumeListeners()");

        if (tapper != null) {
            coinImage.setOnClickListener(tapper);
        }
    }

    private void loadResources() {
        Log.d(TAG, "Default coin selected");
        loadInternalResources();
    }

    private void loadInternalResources(){
        Log.d(TAG, "loadInternalResources()");

        // load the images
        final Drawable heads = getResources().getDrawable(R.drawable.heads);
        final Drawable tails = getResources().getDrawable(R.drawable.tails);
        final Drawable edge = getResources().getDrawable(R.drawable.edge);
        final Drawable background = getResources().getDrawable(R.drawable.background);

        Animation.generateAnimations(heads, tails, edge, background);
    }
}
