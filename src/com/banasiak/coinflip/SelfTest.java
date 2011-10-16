/*
 *========================================================================
 * SelfTest.java
 * May 16, 2011 11:14:07 PM | variable
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
    // debugging tag
    private static final String TAG = "SelfTest";

    private TextView headsValue;
    private TextView headsRatio;
    private TextView tailsValue;
    private TextView tailsRatio;
    private TextView totalValue;
    private TextView totalRatio;
    private TextView elapsedTime;

    private SelfTestTask backgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.selftest);

        headsValue = (TextView) findViewById(R.id.heads_value_tv);
        headsRatio = (TextView) findViewById(R.id.heads_ratio_tv);
        tailsValue = (TextView) findViewById(R.id.tails_value_tv);
        tailsRatio = (TextView) findViewById(R.id.tails_ratio_tv);
        totalValue = (TextView) findViewById(R.id.total_value_tv);
        totalRatio = (TextView) findViewById(R.id.total_ratio_tv);
        elapsedTime = (TextView) findViewById(R.id.elapsed_time_tv);

        backgroundTask = new SelfTestTask(this);
        backgroundTask.execute(new SelfTestStatus());
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop()");
        super.onStop();
        backgroundTask.cancel(true);
    }

    // this method is called when the async task reports it has new information
    public void updateDialog(SelfTestStatus taskStatus)
    {
        //Log.d(TAG, "updateDialog()");

        final NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(1);

        headsValue.setText(Integer.toString(taskStatus.getHeads()));
        headsRatio.setText("("
            + percentFormat.format(taskStatus.getHeadsPercentage()) + ")");

        tailsValue.setText(Integer.toString(taskStatus.getTails()));
        tailsRatio.setText("("
            + percentFormat.format(taskStatus.getTailsPercentage()) + ")");

        totalValue.setText(Integer.toString(taskStatus.getTotal()));
        totalRatio.setText("("
            + percentFormat.format(taskStatus.getCompletionPercentage()) + ")");

        elapsedTime.setText(Long.toString(taskStatus.getElapsedTime()));

    }

}
