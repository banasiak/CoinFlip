/*
 *========================================================================
 * SelfTest.java
 * May 11, 2011 7:31:08 PM | variable
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

import java.text.NumberFormat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SelfTest extends Activity
{
    // Debugging tag.
    private static final String TAG = "SelfTest";
    
    Coin theCoin = new Coin();
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selftest);
        selfTest();
    }
    
    private void selfTest()
    {
        Log.i(TAG, "selfTest()");

        final TextView headsValue = (TextView) findViewById(R.id.heads_value_tv);
        final TextView headsRatio = (TextView) findViewById(R.id.heads_ratio_tv);
        final TextView tailsValue = (TextView) findViewById(R.id.tails_value_tv);
        final TextView tailsRatio = (TextView) findViewById(R.id.tails_ratio_tv);
        final TextView totalValue = (TextView) findViewById(R.id.total_value_tv);
        final TextView totalRatio = (TextView) findViewById(R.id.total_ratio_tv);
        final TextView elapsedTime = (TextView) findViewById(R.id.elapsed_time_tv);
               
        int heads = 0;
        int tails = 0;
        int total = 0;       

        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(1);
        
        long startTimeStamp = System.currentTimeMillis();
        for(total=0; total<10000; total++)
        {
            if( theCoin.flip() )
                heads++;
            else
                tails++;
        }
        long endTimeStamp = System.currentTimeMillis();
        
        Log.d(TAG, "heads: " + heads);
        Log.d(TAG, "tails: " + tails);
        Log.d(TAG, "total: " + total);
        Log.d(TAG, "time: " + Long.toString(endTimeStamp - startTimeStamp));
        
        headsValue.setText(Integer.toString(heads));
        headsRatio.setText("(" + percentFormat.format((double) heads / (double) total) + ")");
        tailsValue.setText(Integer.toString(tails));
        tailsRatio.setText("(" + percentFormat.format((double) tails / (double) total) + ")");
        totalValue.setText(Integer.toString(total));
        totalRatio.setText("(" + percentFormat.format((double) total / (double) total) + ")");
        
        elapsedTime.setText(Long.toString(endTimeStamp - startTimeStamp));
        
    }
    
}
