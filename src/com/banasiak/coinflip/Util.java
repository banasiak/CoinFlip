/*
 *========================================================================
 * Util.java
 * Dec 30, 2011 12:46:05 PM | variable
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

import java.util.Random;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.Log;

public class Util
{
    // debugging tag
    private static final String TAG = "Util";

    private final Context mContext;

    public Util(Context context)
    {
        mContext = context;
    }

    // check to see if an extension package is installed
    public boolean isExtPkgInstalled(String extPkg)
    {
        Log.d(TAG, "isExtPkgInstalled()");
        Log.d(TAG, "extPkg="+ extPkg);
        boolean isInstalled = false;
        try
        {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(extPkg, 0);
            if (packageInfo != null)
            {
                isInstalled = true;
            }
        }
        catch (NameNotFoundException e)
        {
            Log.d(TAG, "NameNotFoundException");
            //e.printStackTrace();
        }

        Log.d(TAG, "result=" + isInstalled);
        return isInstalled;

    }

    // concatenate two arrays
    public CharSequence[] mergeArray(CharSequence[] a, CharSequence[] b)
    {
        Log.d(TAG, "mergeArray()");
        CharSequence[] result = new CharSequence[a.length + b.length];
        int i, j;

        for(i=0; i<a.length; i++)
        {
            result[i] = a[i];
        }

        for(j=0; j<b.length; j++)
        {
            result[i+j] = b[j];
        }

        return result;
    }

    // build an array of all possible coins and select a random one
    public String getRandomCoin(String extPkg)
    {
        Log.d(TAG, "getRandomCoin()");
        String coin = "default";
        int val = -1;
        Random generator = new Random();
        if( isExtPkgInstalled(extPkg))
        {
            try
            {
                // load the built-in values
                CharSequence[] currentEntryValues = mContext.getResources().getStringArray(R.array.coins_values);

                // load the resources from the add-in package
                Resources extPkgResources = mContext.getPackageManager().getResourcesForApplication(extPkg);

                // load the values in the add-in package
                int coinsValuesId = extPkgResources.getIdentifier("coins_values", "array", extPkg);
                CharSequence[] newEntryValues = extPkgResources.getStringArray(coinsValuesId);

                // merge the two values
                CharSequence[] combinedEntryValues = mergeArray(currentEntryValues, newEntryValues);

                // choose a random item from the array
                val = generator.nextInt(combinedEntryValues.length);
                coin = (String) combinedEntryValues[val];

            }
            catch (NameNotFoundException e)
            {
                // shouldn't happen because we already verified the package exists
                Log.e(TAG, "NameNotFoundException", e);
            }
        }
        Log.d(TAG, "result=" + coin);
        return coin;
    }

}
