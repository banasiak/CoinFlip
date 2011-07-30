/*
 *========================================================================
 * CoinFlip.java
 * Jul 24, 2011 10:18:43 AM | variable
 * Copyright (c) 2011 Richard Banasiak
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CoinFlip extends Activity
{
    // debugging tag
    private static final String TAG = "CoinFlip";

    // enumerator of all possible transition states
    private enum ResultState
    {
        HEADS_HEADS,
        HEADS_TAILS,
        TAILS_HEADS,
        TAILS_TAILS,
        UNKNOWN
    }

    // hashmaps perform poorly in Dalvik and there's a minimal number of sounds
    private static final int[] SOUNDS = new int[2];
    private static final int SOUND_COIN = 0;
    private static final int SOUND_1UP = 1;

    private final Coin theCoin = new Coin();
    private ShakeListener shaker;
    private Boolean currentResult = true;
    private Boolean previousResult = true;
    private AnimationDrawable coinAnimation;
    private CustomAnimationDrawable coinAnimationCustom;
    private ImageView coinImage;
    private TextView resultText;
    private SoundPool soundPool;
    private int flipCounter = 0;

    /**
     * Called when the user presses the menu button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onCreateOptionsMenu()");
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Called when the user selects an item from the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(TAG, "onOptionsItemSelected()");
        switch (item.getItemId())
        {
            case R.id.about_menu:
                final Intent i = new Intent(this, About.class);
                startActivity(i);
                return true;
            case R.id.selftest_menu:
                final Intent j = new Intent(this, SelfTest.class);
                startActivity(j);
                return true;
            case R.id.settings_menu:
                final Intent k = new Intent(this, Settings.class);
                startActivity(k);
                return true;
            case R.id.exit:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume()");
        resetCoin();
        shaker.resume();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause()");
        shaker.pause();
        super.onPause();

        // persist state
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("flipCounter", flipCounter);
        editor.commit();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        // restore state
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        flipCounter = preferences.getInt("flipCounter", 0);

        setContentView(R.layout.main);

        // initialize the coin image and result text views
        initCoinImageView();
        initResultTextView();

        // initialize the sounds
        initSounds();

        // initialize the shake listener
        shaker = new ShakeListener(this);
        shaker.setOnShakeListener(new ShakeListener.OnShakeListener()
        {
            public void onShake()
            {
                flipCoin();
            }
        });

        // initialize the onclick listener
        coinImage.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                flipCoin();
            }
        });
    }

    private void flipCoin()
    {
        Log.d(TAG, "flipCoin()");

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // we're in the process of flipping the coin
        ResultState resultState = ResultState.UNKNOWN;

        // vibrate if enabled
        if (Settings.getVibratePref(this))
        {
            vibrator.vibrate(100);
        }

        // flip the coin and update the state with the result
        resultState = updateState(theCoin.flip());

        // update the screen with the result of the flip
        renderResult(resultState);

        flipCounter++;
    }

    private void resetCoin()
    {
        Log.d(TAG, "resetCoin()");

        // hide the animation and draw the reset image
        displayCoinAnimation(false);
        displayCoinImage(true);
        coinImage.setImageDrawable(getResources().getDrawable(R.drawable.unknown));
        resultText.setText("");
    }

    private ResultState updateState(boolean flipResult)
    {
        // Analyze the current coin state and the new coin state and determine
        // the proper transition between the two.
        // true = HEADS | false = TAILS

        Log.d(TAG, "updateState()");

        ResultState resultState = ResultState.UNKNOWN;
        currentResult = flipResult;

        // this is easier to read than the old code
        if (previousResult == true && currentResult == true)
            resultState = ResultState.HEADS_HEADS;
        if (previousResult == true && currentResult == false)
            resultState = ResultState.HEADS_TAILS;
        if (previousResult == false && currentResult == true)
            resultState = ResultState.TAILS_HEADS;
        if (previousResult == false && currentResult == false)
            resultState = ResultState.TAILS_TAILS;

        // update the previousResult for the next flip
        previousResult = currentResult;

        return resultState;
    }

    private void renderResult(final ResultState resultState)
    {
        Log.d(TAG, "renderResult()");

        // hide the static image and clear the text
        displayCoinImage(false);
        displayCoinAnimation(false);
        resultText.setText("");

        // initialize the appropriate animation depending on the resultState
        switch (resultState)
        {
            default:
            case HEADS_HEADS:
                coinImage.setImageDrawable(getResources().getDrawable(R.drawable.heads));
                coinAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.heads_heads);
                coinAnimationCustom = new CustomAnimationDrawable(coinAnimation)
                {
                    @Override
                    void onAnimationFinish()
                    {
                        playCoinSound();
                        updateResultText(resultState);
                    }
                };
                break;
            case HEADS_TAILS:
                coinImage.setImageDrawable(getResources().getDrawable(R.drawable.tails));
                coinAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.heads_tails);
                coinAnimationCustom = new CustomAnimationDrawable(coinAnimation)
                {
                    @Override
                    void onAnimationFinish()
                    {
                        playCoinSound();
                        updateResultText(resultState);
                    }
                };
                break;
            case TAILS_HEADS:
                coinImage.setImageDrawable(getResources().getDrawable(R.drawable.heads));
                coinAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.tails_heads);
                coinAnimationCustom = new CustomAnimationDrawable(coinAnimation)
                {
                    @Override
                    void onAnimationFinish()
                    {
                        playCoinSound();
                        updateResultText(resultState);
                    }
                };
                break;
            case TAILS_TAILS:
                coinImage.setImageDrawable(getResources().getDrawable(R.drawable.tails));
                coinAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.tails_tails);
                coinAnimationCustom = new CustomAnimationDrawable(coinAnimation)
                {
                    @Override
                    void onAnimationFinish()
                    {
                        playCoinSound();
                        updateResultText(resultState);
                    }
                };
                break;
        }

        // display the result
        if (Settings.getAnimationPref(this))
        {
            // hide the static image and render the animation
            displayCoinImage(false);
            displayCoinAnimation(true);
            coinImage.setBackgroundDrawable(coinAnimationCustom);
            coinAnimationCustom.start();
            // handled by animation callback
            // playCoinSound();
            // updateResultText(resultState, resultText);
        }
        else
        {
            // hide the animation and display the static image
            displayCoinImage(true);
            displayCoinAnimation(false);
            playCoinSound();
            updateResultText(resultState);
        }
    }

    private void initSounds()
    {
        // MediaPlayer was causing ANR issues on some devices.
        // SoundPool should be more efficient.
        Log.d(TAG, "initSounds()");
        soundPool  = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        SOUNDS[SOUND_COIN] = soundPool.load(this, R.raw.coin, 1);
        SOUNDS[SOUND_1UP] = soundPool.load(this, R.raw.oneup, 1);
    }

    private void playSound(int sound)
    {
        Log.d(TAG, "playSound()");
        if (Settings.getSoundPref(this))
        {
            AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = streamVolumeCurrent / streamVolumeMax;

            soundPool.play(sound, volume, volume, 1, 0, 1f);
        }
    }

    private void playCoinSound()
    {
        Log.d(TAG, "playCoinSound()");
        playSound(SOUNDS[SOUND_COIN]);

        //Happy Easter!  (For Ryan)
        if (flipCounter == 100)
        {
            playSound(SOUNDS[SOUND_1UP]);
            flipCounter = 0;
        }
    }

    private void updateResultText(ResultState resultState)
    {
        Log.d(TAG, "updateResultText()");
        switch (resultState)
        {
            case HEADS_HEADS:
            case TAILS_HEADS:
                resultText.setText(R.string.heads);
                resultText.setTextColor(Color.parseColor("green"));
                break;
            case HEADS_TAILS:
            case TAILS_TAILS:
                resultText.setText(R.string.tails);
                resultText.setTextColor(Color.parseColor("red"));
                break;
            default:
                resultText.setText(R.string.unknown);
                resultText.setTextColor(Color.parseColor("yellow"));
                break;
        }
    }
    private void displayCoinAnimation(boolean flag)
    {
        Log.d(TAG, "displayCoinAnimation()");

        // safety first!
        if (coinAnimationCustom != null )
        {
            if (flag)
            {
                coinAnimationCustom.setAlpha(255);
            }
            else
            {
                coinAnimationCustom.setAlpha(0);
            }
        }
    }
    private void displayCoinImage(boolean flag)
    {
        Log.d(TAG, "displayCoinImage");

        // safety first!
        if (coinImage != null)
        {
            if (flag)
            {
                // get rid of the animation background
                coinImage.setBackgroundDrawable(null);
                coinImage.setAlpha(255);
            }
            else
            {
                coinImage.setAlpha(0);
            }
        }
    }
    private void initCoinImageView ()
    {
        Log.d(TAG, "initCoinImageView()");
        coinImage = (ImageView) findViewById(R.id.coin_image_view);
    }
    private void initResultTextView ()
    {
        Log.d(TAG, "initResultImageView()");
        resultText = (TextView) findViewById(R.id.result_text_view);
    }
}