/*
 *========================================================================
 * CoinFlipWearActivity.java
 * Apr 13, 2015 8:55 AM | variable
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

package com.banasiak.coinflip;

import com.banasiak.coinflip.lib.Animation;
import com.banasiak.coinflip.lib.Coin;
import com.banasiak.coinflip.lib.CustomAnimationDrawable;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class CoinFlipWearActivity extends Activity {

    // debugging tag
    private static final String TAG = CoinFlipWearActivity.class.getSimpleName();

    private Drawable heads = null;

    private Drawable tails = null;

    private Drawable edge = null;

    private Drawable background = null;

    private final Coin theCoin = new Coin();

    private View.OnClickListener tapper;

    private Boolean currentResult = true;

    private Boolean previousResult = true;

    private CustomAnimationDrawable coinAnimation;

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
                        if (heads == null || tails == null || edge == null || background == null) {
                            if (loadResources()) {
                                flipCoin();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.no_coins,
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            flipCoin();
                        }
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

        coinAnimation = Animation.getAnimation(resultState);
        coinAnimation.setAnimationCallback(new CustomAnimationDrawable.AnimationCallback() {
            @Override public void onAnimationFinish() {
                final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                resumeListeners();
            }
        });

        //coinImage.setBackgroundDrawable(coinAnimation);
        coinImage.setBackground(null);
        coinImage.setImageDrawable(coinAnimation);
        coinAnimation.stop();
        coinAnimation.start();
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

    private boolean loadResources() {
        Log.d(TAG, "loadResources()");

        heads = loadFromStorage("heads");
        tails = loadFromStorage("tails");
        edge = loadFromStorage("edge");
        background = loadFromStorage("background");

        if (heads == null || tails == null || edge == null || background == null) {
            Log.w(TAG, "Images unavailable. Loading backup resources.");
            loadFallbackResources();
        }

        Animation.generateAnimations(heads, tails, edge, background);
        return true;

    }

    private Drawable loadFromStorage(String fileName) {
        Log.d(TAG, "loadFromStorage(" + fileName + ")");
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getDir("coins", Context.MODE_PRIVATE);

        return Drawable.createFromPath(dir + "/" + fileName + ".png");
    }

    private boolean loadFallbackResources() {
        heads = getResources().getDrawable(R.drawable.heads);
        tails = getResources().getDrawable(R.drawable.tails);
        edge = getResources().getDrawable(R.drawable.edge);
        background = getResources().getDrawable(R.drawable.background);

        return heads != null && tails != null && edge != null && background != null;

    }
}
