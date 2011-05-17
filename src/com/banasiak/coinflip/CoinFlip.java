/*
 *========================================================================
 * CoinFlip.java
 * May 10, 2011 8:48:54 PM | variable
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
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
      shaker.resume();
      super.onResume();
    }
    @Override
    public void onPause()
    {
      shaker.pause();
      super.onPause();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");
        /*
         * The onCreate() method will be called by the Android system when your
         * Activity starts - it is where you should perform all initialization
         * and UI setup. An activity is not required to have a user interface,
         * but usually will.
         */
        super.onCreate(savedInstanceState);
        // TextView tv = new TextView(this);
        // tv.setText("Hello, Android!");
        // setContentView(tv);
        setContentView(R.layout.main);
        
        final Vibrator viberator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        shaker = new ShakeListener(this);
        shaker.setOnShakeListener(new ShakeListener.OnShakeListener()
        {
           public void onShake()
           {
               if(buttonState == 0)
               {
                   viberator.vibrate(100);
                   flipCounter++;
                   renderResult( theCoin.flip() );
               }
           }
        });

        final Button flipCoinButton = (Button) findViewById(R.id.flip_coin_button);
        flipCoinButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                // Perform action on clicks          
                if(buttonState == 0)
                {
                    viberator.vibrate(100);
                    flipCounter++;
                    renderResult( theCoin.flip() );
                }
                else
                {
                    flipCoinButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);  
                    resetCoin();
                }
            }
        });
    }
    
    private void resetCoin()
    {
        final ImageView coinImage = (ImageView) findViewById(R.id.coin_image_view);
        final TextView resultText = (TextView) findViewById(R.id.result_text_view);
        final Button flipCoinButton = (Button) findViewById(R.id.flip_coin_button);
        
        coinImage.setImageDrawable(getResources()
                                   .getDrawable(R.drawable.unknown));
        resultText.setText("");
        flipCoinButton.setText(R.string.flip_coin_button);
        buttonState = 0;
                           
    }
    
    private void renderResult(boolean result)
    {
        Log.d(TAG, "renderResult()");
        final ImageView coinImage = (ImageView) findViewById(R.id.coin_image_view);
        final TextView resultText = (TextView) findViewById(R.id.result_text_view);
        final Button flipCoinButton = (Button) findViewById(R.id.flip_coin_button);
        
        if (result == true)
        {
            
            coinImage.setImageDrawable(getResources()
                    .getDrawable(R.drawable.heads));
            resultText.setText(R.string.heads);
            resultText.setTextColor(Color.parseColor("green"));
//            Toast.makeText(CoinFlip.this,
//                           getString(R.string.flip_result) +
//                                   " " +
//                                   Integer.toString(flipCounter) +
//                                   ": " +
//                                   getString(R.string.heads),
//                           Toast.LENGTH_SHORT).show();
        }
        else
        {
            coinImage.setImageDrawable(getResources()
                    .getDrawable(R.drawable.tails));
            resultText.setText(R.string.tails);
            resultText.setTextColor(Color.parseColor("red"));
//            Toast.makeText(CoinFlip.this,
//                           getString(R.string.flip_result) +
//                                   " " +
//                                   Integer.toString(flipCounter) +
//                                   ": " +
//                                   getString(R.string.tails),
//                           Toast.LENGTH_SHORT).show();
        }
        flipCoinButton.setText(R.string.reset_coin_button);
        buttonState = 1;
    }
}