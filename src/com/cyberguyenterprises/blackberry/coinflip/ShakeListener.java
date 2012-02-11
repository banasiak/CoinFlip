/*
 *========================================================================
 * ShakeListener.java
 * Dec 30, 2011 2:19:15 PM | variable
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

package com.cyberguyenterprises.blackberry.coinflip;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeListener implements SensorEventListener
{
    // debugging tag
    private static final String TAG = "ShakeListener";

    // number of shakes required to trigger callback
    private static final int SHAKE_COUNT = 4;
    // timeout before resetting detected shakes in milliseconds
    private static final int SHAKE_TIMEOUT = 1000;
    // minimum registered movement before shake detected
    private static final float SHAKE_FORCE = 2.5f;

    private SensorManager mSensorManager;
    private OnShakeListener mShakeListener;
    private final Context mContext;

    private long lastTime = -1;
    private int shakeCount = 0;

    private float lastX = -1.0f;
    private float lastY = -1.0f;
    private float lastZ = -1.0f;

    private int mForceMultiplier = -1;

    public interface OnShakeListener
    {
        public void onShake();
    }

    public ShakeListener(Context context)
    {
        mContext = context;
        resume(mForceMultiplier);
    }

    public void setOnShakeListener(OnShakeListener listener)
    {
        Log.d(TAG, "setOnShakeListener()");
        mShakeListener = listener;
    }

    public void resume(int forceMultiplier)
    {
        Log.d(TAG, "resume()");

        // the force multiplier may have changed in the settings
        mForceMultiplier = forceMultiplier;

        // check to see if we can access the system's sensors
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null)
        {
            // if we can, register a listener for the accelerometer
            try
            {
                Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
            catch (Exception e)
            {
                Log.w(TAG, "Accelerometer not supported", e);
                mSensorManager.unregisterListener(this);
            }
        }
        else
        {
            Exception e = new UnsupportedOperationException();
            Log.w(TAG, "Sensors not supported", e);
        }
    }

    public void pause()
    {
        Log.d(TAG, "pause()");

        if (mSensorManager != null)
        {
            // we have manually unregister the listener on pause()
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        //Log.d(TAG, "onSensorChanged()");

        // make sure its the accelerometer that has changed
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            long now = System.currentTimeMillis();

            // if the shake timeout has elapsed, reset the shake count
            if ((now-lastTime) > SHAKE_TIMEOUT)
            {
                shakeCount = 0;
            }

            // get the current axis values - the SensorManager constants are
            // deprecated and we don't really care which-axis-is-which anyway
            float curX = event.values[0];
            float curY = event.values[1];
            float curZ = event.values[2];

            // calculate a pseudo-scalar-velocity measurement
            float movement = Math.abs(curX + curY + curZ - lastX - lastY - lastZ);

            // if a movement over the force threshold was detected, count it
            if (movement > SHAKE_FORCE*mForceMultiplier)
            {
                lastTime = now;
                shakeCount++;
            }

            // trigger the callback if the shake count is reached
            if (shakeCount >= SHAKE_COUNT)
            {
                if (mShakeListener != null)
                {
                    mShakeListener.onShake();
                }
                shakeCount = 0;
            }

            // update the last values
            lastTime = now;
            lastX = curX;
            lastY = curY;
            lastZ = curZ;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // we dont' really care about this, but its been inherited...
    }

}