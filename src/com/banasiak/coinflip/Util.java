/*
 * ========================================================================
 * Util.java Dec 30, 2011 12:46:05 PM | variable Copyright (c) 2011 Richard
 * Banasiak
 * ======================================================================== This
 * file is part of CoinFlip.
 * 
 * CoinFlip is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * CoinFlip is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * CoinFlip. If not, see <http://www.gnu.org/licenses/>.
 */

package com.banasiak.coinflip;

import java.util.List;
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

    public Util(final Context context)
    {
        mContext = context;
    }

    // check to see if an extension package is installed
    public boolean isExtPkgInstalled(final String extPkg)
    {
        Log.d(TAG, "isExtPkgInstalled()");
        Log.d(TAG, "extPkg=" + extPkg);
        boolean isInstalled = false;
        try
        {
            final PackageInfo packageInfo = mContext.getPackageManager()
                .getPackageInfo(extPkg, 0);
            if (packageInfo != null)
            {
                isInstalled = true;
            }
        }
        catch (final NameNotFoundException e)
        {
            Log.d(TAG, "NameNotFoundException");
            // e.printStackTrace();
        }

        Log.d(TAG, "result=" + isInstalled);
        return isInstalled;

    }

    // concatenate two arrays
    public CharSequence[] mergeArray(final CharSequence[] a,
        final CharSequence[] b)
    {
        Log.d(TAG, "mergeArray()");
        final CharSequence[] result = new CharSequence[a.length + b.length];
        int i, j;

        for (i = 0; i < a.length; i++)
        {
            result[i] = a[i];
        }

        for (j = 0; j < b.length; j++)
        {
            result[i + j] = b[j];
        }

        return result;
    }

    // build an array of all possible coins and select a random one
    public String getRandomCoin()
    {
        Log.d(TAG, "getRandomCoin()");
        String coin = "default";
        int val = -1;
        final Random generator = new Random();
        final List<PackageInfo> pkgs = findExternalPackages();

        // load the built-in values
        CharSequence[] currentEntryValues = mContext
            .getResources().getStringArray(R.array.coins_values);

        for (final PackageInfo info : pkgs)
        {
            try
            {
                // load the resources from the add-in package
                final Resources extPkgResources = mContext.getPackageManager()
                    .getResourcesForApplication(info.packageName);

                // load the values in the add-in package
                final int coinsValuesId = extPkgResources.getIdentifier(
                    "coins_values", "array", (info.packageName));
                final CharSequence[] newEntryValues = extPkgResources
                    .getStringArray(coinsValuesId);

                // merge the two values
                currentEntryValues = mergeArray(currentEntryValues, newEntryValues);
            }
            catch (final NameNotFoundException e)
            {
                // shouldn't happen because we already verified the package
                // exists
                Log.e(TAG, "NameNotFoundException", e);
            }
        }

        // choose a random item from the array
        val = generator.nextInt(currentEntryValues.length);
        coin = (String) currentEntryValues[val];

        Log.d(TAG, "result=" + coin);
        return coin;
    }

    public List<PackageInfo> findExternalPackages()
    {
        final List<PackageInfo> packageInfo = mContext.getPackageManager().getInstalledPackages(0);

        for (int i = 0, n = packageInfo.size() - 1; i <= n; --n)
        {
            final PackageInfo info = packageInfo.get(n);
            if (!isExternalCoinPackage(info))
            {
                packageInfo.remove(n);
            }
        }
        return packageInfo;
    }

    private boolean isExternalCoinPackage(final PackageInfo info)
    {
        final String[] parts = info.packageName.split("\\.");
        return parts[parts.length - 1].startsWith("coin") && parts[parts.length - 1].endsWith("ext");
    }

    public String findExternalResourcePackage(final String coinPrefix)
    {
        final List<PackageInfo> pkgs = findExternalPackages();
        for (final PackageInfo pkg : pkgs)
        {
            try
            {
                final Resources res = mContext.getPackageManager().getResourcesForApplication(pkg.packageName);

                if (getExternalResourceEdge(pkg.packageName, res, coinPrefix) != 0 ||
                    getExternalResourceHeads(pkg.packageName, res, coinPrefix) != 0 ||
                    getExternalResourceEdge(pkg.packageName, res, coinPrefix) != 0)
                {
                    return pkg.packageName;
                }
            }
            catch (final NameNotFoundException e)
            {
                // The resources probably wasn't in there anyway
            }
        }
        return null;
    }

    public int getExternalResourceHeads(final String packageName, final Resources pkg, final String prefix)
    {
        return pkg.getIdentifier(prefix + "_heads", "drawable", packageName);
    }

    public int getExternalResourceTails(final String packageName, final Resources pkg, final String prefix)
    {
        return pkg.getIdentifier(prefix + "_tails", "drawable", packageName);
    }

    public int getExternalResourceEdge(final String packageName, final Resources pkg, final String prefix)
    {
        return pkg.getIdentifier(prefix + "_edge", "drawable", packageName);
    }

}
