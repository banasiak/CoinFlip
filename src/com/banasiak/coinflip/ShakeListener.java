/*
 *========================================================================
 * ShakeListener.java
 * May 16, 2011 11:19:06 PM | variable
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
import android.widget.Toast;

public class ShakeListener
        implements
            SensorEventListener
{
    private static final int FORCE_THRESHOLD = 300;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 1000;
    private static final int SHAKE_COUNT = 3;

    private SensorManager mSensorMgr;
    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private long  mLastTime;
    private OnShakeListener mShakeListener;
    private Context mContext;
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
        mShakeListener = listener;
    }

    public void resume()
    {
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null)
        {
            throw new UnsupportedOperationException("Sensors not supported!");
        }
        boolean supported = false;
        try
        {
            supported = mSensorMgr
                    .registerListener(this,
                                      mSensorMgr
                                              .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                      SensorManager.SENSOR_DELAY_GAME);
        }
        catch (Exception e)
        {
            Toast.makeText(mContext,
                           "Shaking not supported!",
                           Toast.LENGTH_LONG).show();
        }

        if ((!supported) && (mSensorMgr != null))
            mSensorMgr.unregisterListener(this);
    }

    public void pause()
    {
        if (mSensorMgr != null)
        {
            mSensorMgr.unregisterListener(this);
            mSensorMgr = null;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        
        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT)
        {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD)
        {
            long diff = now - mLastTime;
            float speed = Math.abs(event.values[SensorManager.DATA_X] +
                                   event.values[SensorManager.DATA_Y] +
                                   event.values[SensorManager.DATA_Z] -
                                   mLastX -
                                   mLastY -
                                   mLastZ) /
                          diff *
                          10000;
            if (speed > FORCE_THRESHOLD)
            {
                if ((++mShakeCount >= SHAKE_COUNT) &&
                    (now - mLastShake > SHAKE_DURATION))
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
            mLastZ = event.values[SensorManager.DATA_Z];
        }
    }
}