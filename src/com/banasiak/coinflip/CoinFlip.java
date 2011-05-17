/*
 *========================================================================
 * CoinFlip.java
 * May 16, 2011 11:07:27 PM | variable
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

import com.banasiak.coinflip.About;
import com.banasiak.coinflip.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class CoinFlip
    extends Activity
{
    private static final String TAG = "CoinFlip";

    private Coin theCoin = new Coin();
    private ShakeListener shaker;
    private int buttonState = 0;
    private int flipCounter = 0;

    /**
     * Called when the user presses the menu button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onCreateOptionsMenu()");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
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
                Intent i = new Intent(this, About.class);
                startActivity(i);
                return true;
            case R.id.selftest_menu:
                Intent j = new Intent(this, SelfTest.class);
                startActivity(j);
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
        shaker.resume();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause()");
        shaker.pause();
        super.onPause();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        shaker = new ShakeListener(this);
        shaker.setOnShakeListener(new ShakeListener.OnShakeListener()
        {
            public void onShake()
            {
                flipOrResetCoin();
            }
        });

        final ImageView coinImage = (ImageView) findViewById(R.id.coin_image_view);
        coinImage.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                flipOrResetCoin();
            }
        });
    }

    private void flipOrResetCoin()
    {
        Log.d(TAG, "flipOrResetCoin()");
        
        if (buttonState == 0)
        {
            flipCoin();
            shaker.pause();
            buttonState = 1;
        }
        else
        {
            resetCoin();
            shaker.resume();
            buttonState = 0;
        }
    }

    private void flipCoin()
    {
        Log.d(TAG, "flipCoin()");
        
        final Vibrator viberator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        viberator.vibrate(100);
        flipCounter++;
        renderResult(theCoin.flip());
    }

    private void resetCoin()
    {
        Log.d(TAG, "resetCoin()");
        
        final ImageView coinImage = (ImageView) findViewById(R.id.coin_image_view);
        final TextView resultText = (TextView) findViewById(R.id.result_text_view);
        final TextView instructionsText = (TextView) findViewById(R.id.instructions_text_view);

        coinImage.setImageDrawable(getResources().getDrawable(R.drawable.unknown));
        resultText.setText("");
        instructionsText.setText(R.string.flip_coin_tv);
    }

    private void renderResult(boolean result)
    {
        Log.d(TAG, "renderResult()");
        
        final ImageView coinImage = (ImageView) findViewById(R.id.coin_image_view);
        final TextView resultText = (TextView) findViewById(R.id.result_text_view);
        final TextView instructionsText = (TextView) findViewById(R.id.instructions_text_view);

        if (result == true)
        {
            coinImage.setImageDrawable(getResources().getDrawable(R.drawable.heads));
            resultText.setText(R.string.heads);
            resultText.setTextColor(Color.parseColor("green"));
        }
        else
        {
            coinImage.setImageDrawable(getResources().getDrawable(R.drawable.tails));
            resultText.setText(R.string.tails);
            resultText.setTextColor(Color.parseColor("red"));
        }
        instructionsText.setText(R.string.reset_coin_tv);
    }
}