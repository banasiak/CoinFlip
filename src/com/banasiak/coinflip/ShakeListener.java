/*
 *========================================================================
 * ShakeListener.java
 * Jul 23, 2011 9:42:08 AM | variable
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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class ShakeListener implements SensorEventListener
{
    // debugging tag
    private static final String TAG = "ShakeListener";

    private static final int FORCE_THRESHOLD = 1000;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 1000;
    private static final int SHAKE_COUNT = 3;

    private SensorManager mSensorMgr;
    private float mLastX = -1.0f, mLastY = -1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private final Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;

    public interface OnShakeListener
    {
        public void onShake();
    }

    public ShakeListener(Context context)
    {
        mContext = context;
        resume();
    }

    public void setOnShakeListener(OnShakeListener listener)
    {
        Log.d(TAG, "setOnShakeListener()");

        mShakeListener = listener;
    }

    public void resume()
    {
        Log.d(TAG, "resume()");

        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null)
        {
            throw new UnsupportedOperationException("Sensors not supported!");
        }
        boolean supported = false;
        try
        {
            supported = mSensorMgr.registerListener(this,
                mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        }
        catch (final Exception e)
        {
            Toast.makeText(mContext, "Shaking not supported!", Toast.LENGTH_LONG).show();
        }

        if ((!supported) && (mSensorMgr != null))
        {
            mSensorMgr.unregisterListener(this);
        }
    }

    public void pause()
    {
        Log.d(TAG, "pause()");

        if (mSensorMgr != null)
        {
            mSensorMgr.unregisterListener(this);
            mSensorMgr = null;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //Log.d(TAG, "onAccuracyChanged()");
    }

    public void onSensorChanged(SensorEvent event)
    {
        //Log.d(TAG, "onSensorChanged()");

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
        {
            return;
        }

        final long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT)
        {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD)
        {
            final long diff = now - mLastTime;
            final float speed = Math.abs(
                event.values[SensorManager.DATA_X] - mLastX
                + event.values[SensorManager.DATA_Y] - mLastY)
                / diff * 10000;
            if (speed > FORCE_THRESHOLD)
            {
                if ((++mShakeCount >= SHAKE_COUNT)
                    && (now - mLastShake > SHAKE_DURATION))
                {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener != null)
                    {
                        mShakeListener.onShake();
                    }
                }
                mLastForce = now;
            }
            mLastTime = now;
            mLastX = event.values[SensorManager.DATA_X];
            mLastY = event.values[SensorManager.DATA_Y];
        }
    }
}
