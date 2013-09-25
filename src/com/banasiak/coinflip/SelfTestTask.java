/*
 *========================================================================
 * SelfTestAsyncTask.java
 * Sep 25, 2013 12:54 PM | variable
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

import android.os.AsyncTask;
import android.util.Log;

public class SelfTestTask extends AsyncTask<SelfTestStatus, SelfTestStatus, SelfTestStatus> {

    // debugging tag
    private static final String TAG = "SelfTestTask";

    private final SelfTest activity;

    private final Coin theCoin;

    public SelfTestTask(final SelfTest iActivity) {
        Log.d(TAG, "SelfTestTask()");
        activity = iActivity;
        theCoin = new Coin();
    }

    @Override
    protected SelfTestStatus doInBackground(SelfTestStatus... params) {
        //Log.d(TAG, "doInBackground()");

        SelfTestStatus taskStatus;
        if (params.length < 1) {
            // must be the first time we've been called
            taskStatus = new SelfTestStatus();
        } else {
            // otherwise it's recursive! :)
            taskStatus = params[0];
            taskStatus.setStartTime(System.currentTimeMillis());
            for (int total = 0; total < activity.getMaxNumberFlips(); total++) {
                // if the self test activity has been closed, might as well terminate the self test
                if (isCancelled()) {
                    break;
                }

                // flip the coin and update the model
                if (theCoin.flip()) {
                    taskStatus.incrementHeads();
                } else {
                    taskStatus.incrementTails();
                }
                taskStatus.setEndTime(System.currentTimeMillis());

                if (total % 100 == 0) {
                    // update the UI thread with our current progress
                    publishProgress(taskStatus);

                    try {
                        // this tiny sleep smoothes out the GUI updating, particularly on fast CPUs.
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // don't care
                    }
                }
            }

            Log.d(TAG, "heads: " + taskStatus.getHeads());
            Log.d(TAG, "tails: " + taskStatus.getTails());
            Log.d(TAG, "total: " + taskStatus.getTotal());
            Log.d(TAG, "time: " + Long.toString(taskStatus.getElapsedTime()));
        }

        return taskStatus;
    }

    @Override
    protected void onProgressUpdate(SelfTestStatus... values) {
        //Log.d(TAG, "onProgressUpdate()");
        super.onProgressUpdate(values);
        if (values.length > 0) {
            // update the SelfTest activity with our current values
            activity.updateDialog(values[0]);
        }
    }

    @Override
    protected void onPostExecute(SelfTestStatus result) {
        Log.d(TAG, "onPostExecute()");
        super.onPostExecute(result);
        activity.updateDialog(result);
    }
}
