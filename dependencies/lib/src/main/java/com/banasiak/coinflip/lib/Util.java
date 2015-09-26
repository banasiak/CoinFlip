/*
 *========================================================================
 * Util.java
 * Sep 26, 2015 6:12 PM | variable
 * Copyright (c) 2015 Richard Banasiak
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

package com.banasiak.coinflip.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.List;
import java.util.Random;

public class Util {

    // debugging tag
    private static final String TAG = Util.class.getSimpleName();

    private Util() {
    }

    // concatenate two arrays
    public static CharSequence[] mergeArray(final CharSequence[] a, final CharSequence[] b) {
        Log.d(TAG, "mergeArray()");

        final CharSequence[] result = new CharSequence[a.length + b.length];
        int i, j;

        for (i = 0; i < a.length; i++) {
            result[i] = a[i];
        }

        for (j = 0; j < b.length; j++) {
            result[i + j] = b[j];
        }

        return result;
    }

    // build an array of all possible coins and select a random one
    public static String getRandomCoin(final Context context, final int arrayId) {
        Log.d(TAG, "getRandomCoin()");

        String coin = "default";
        int val = -1;
        final Random generator = new Random();
        final List<PackageInfo> pkgs = findExternalPackages(context);

        // load the built-in values
        CharSequence[] currentEntryValues = context.getResources().getStringArray(arrayId);

        for (final PackageInfo info : pkgs) {
            try {
                // load the resources from the add-in package
                final Resources extPkgResources = context.getPackageManager()
                        .getResourcesForApplication(info.packageName);

                // load the values in the add-in package
                final int coinsValuesId = extPkgResources.getIdentifier("coins_values", "array",
                        (info.packageName));
                final CharSequence[] newEntryValues = extPkgResources.getStringArray(coinsValuesId);

                // merge the two values
                currentEntryValues = mergeArray(currentEntryValues, newEntryValues);
            } catch (final NameNotFoundException e) {
                // shouldn't happen because we already verified the package exists
                Log.e(TAG, "NameNotFoundException", e);
            }
        }

        // choose a random item from the array
        val = generator.nextInt(currentEntryValues.length);
        coin = (String) currentEntryValues[val];

        Log.d(TAG, "result=" + coin);
        return coin;
    }

    // load a list of packages installed on the system
    public static List<PackageInfo> findExternalPackages(Context context) {
        Log.d(TAG, "findExternalPackages()");

        final List<PackageInfo> packageInfo = context.getPackageManager().getInstalledPackages(0);

        for (int i = 0, n = packageInfo.size() - 1; i <= n; --n) {
            final PackageInfo info = packageInfo.get(n);
            if (!isExternalCoinPackage(info)) {
                packageInfo.remove(n);
            }
        }

        return packageInfo;
    }

    // if any part of the package name contains "coinflipext" then assume it
    // is a valid add-on package for this application.
    private static boolean isExternalCoinPackage(final PackageInfo info) {
        Log.d(TAG, "isExternalCoinPackage()");

        boolean isValid = false;
        final String[] parts = info.packageName.split("\\.");

        for (String part : parts) {
            if (part.contentEquals("coinflipext")) {
                isValid = true;
                break;
            }
        }
        Log.d(TAG, "packageName=" + info.packageName + " | result=" + isValid);
        return isValid;
    }

    // find external coin packages and verify it contains valid resources
    public static String findExternalResourcePackage(final Context context, final String coinPrefix) {
        Log.d(TAG, "findExternalResourcePackage() | coinPrefix=" + coinPrefix);

        final List<PackageInfo> pkgs = findExternalPackages(context);
        for (final PackageInfo pkg : pkgs) {
            try {
                final Resources res = context.getPackageManager().getResourcesForApplication(
                        pkg.packageName);

                // see if the package contains a heads/tails/edge image resource for the requested prefix
                if (getExternalResourceEdge(pkg.packageName, res, coinPrefix) != 0
                        || getExternalResourceHeads(pkg.packageName, res, coinPrefix) != 0
                        || getExternalResourceTails(pkg.packageName, res, coinPrefix) != 0) {
                    // if all three resources exist, return the package name
                    Log.d(TAG, "package found | name=" + pkg.packageName);
                    return pkg.packageName;
                }
            } catch (final NameNotFoundException e) {
                // Ignore.  The resources probably aren't in this package anyway.
            }
        }
        Log.w(TAG, "package not found!");
        return null;
    }

    public static int getExternalResourceHeads(final String packageName, final Resources pkg,
            final String prefix) {
        Log.d(TAG, "getExternalResourceHeads()");
        return pkg.getIdentifier(prefix + "_heads", "drawable", packageName);
    }

    public static int getExternalResourceTails(final String packageName, final Resources pkg,
            final String prefix) {
        Log.d(TAG, "getExternalResourceTails()");
        return pkg.getIdentifier(prefix + "_tails", "drawable", packageName);
    }

    public static int getExternalResourceEdge(final String packageName, final Resources pkg,
            final String prefix) {
        Log.d(TAG, "getExternalResourceEdge()");
        return pkg.getIdentifier(prefix + "_edge", "drawable", packageName);
    }

    public static int getScreenWidth(final Context context) {
        Log.d(TAG, "getScreenWidth()");

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int width = display.getWidth();
        Log.d(TAG, "screenWidth=" + width);
        return width;
    }

    @SuppressLint("NewApi")
    public static <T> AsyncTask<T, ?, ?> execute(final AsyncTask<T, ?, ?> task, final T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            return task.execute(params);
        }
    }

}
