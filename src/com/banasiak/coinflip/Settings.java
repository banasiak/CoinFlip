/*
 *========================================================================
 * Settings.java
 * Aug 4, 2011 8:41:14 PM | variable
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity
{
    // debugging tag
    private static final String TAG = "CoinFlip";

    // add-on package name
    private static final String EXTPKG = "com.banasiak.coinflipext";

    // option keys and default values
    private static final String KEY_ANIMATION = "animation";
    private static final boolean KEY_ANIMATION_DEF = true;
    private static final String KEY_COIN = "coin";
    private static final String KEY_COIN_DEF = "default";
    private static final String KEY_FORCE = "force";
    private static final int KEY_FORCE_DEF = 50;
    private static final String KEY_SOUND = "sound";
    private static final boolean KEY_SOUND_DEF = true;
    private static final String KEY_VIBRATE = "vibrate";
    private static final boolean KEY_VIBRATE_DEF = true;

    private static final String KEY_FLIPCOUNT = "flipCount";
    private static final int KEY_FLIPCOUNT_DEF = 0;

    private final Util util = new Util(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        Preference downloadPref = getPreferenceManager().findPreference("download");
        downloadPref.setOnPreferenceClickListener(
            new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + EXTPKG));
                    startActivity(goToMarket);
                    return true;
                }
            });

        if (util.isExtPkgInstalled(EXTPKG))
        {
            getPreferenceScreen().removePreference(downloadPref);
            loadExtPkgCoins();
        }
    }

    // Load the "coins" available in the add-on package.
    //
    // The theory here is that the add-on package will contain an array of
    // "coins" and "coin_values".  The coin_value should be the prefix to the
    // associated resources.  For example, if coin_values[1]="washington", all
    // resources should be named "washington_".  This gives CoinFlip a
    // mechanism for loading resources from CoinFlipExt without having to be
    // aware of all the contents of CoinFlipExt in advance.
    private void loadExtPkgCoins()
    {
        Log.d(TAG, "loadExtPkgCoins()");

        try
        {
            // load the built-in values
            CharSequence[] currentEntries = getResources().getStringArray(R.array.coins);
            CharSequence[] currentEntryValues = getResources().getStringArray(R.array.coins_values);

            // load the resources from the add-in package
            Resources extPkgResources = getPackageManager().getResourcesForApplication(EXTPKG);

            // load the values in the add-in package
            int coinsId = extPkgResources.getIdentifier("coins", "array", EXTPKG);
            int coinsValuesId = extPkgResources.getIdentifier("coins_values", "array", EXTPKG);
            CharSequence[] newEntries = extPkgResources.getStringArray(coinsId);
            CharSequence[] newEntryValues = extPkgResources.getStringArray(coinsValuesId);

            // merge the two values
            CharSequence[] combinedEntries = mergeArray(currentEntries, newEntries);
            CharSequence[] combinedEntryValues = mergeArray(currentEntryValues, newEntryValues);

            // update the ListPreference with the combined results
            ListPreference coinPref = (ListPreference) findPreference("coin");
            coinPref.setEntries(combinedEntries);
            coinPref.setEntryValues(combinedEntryValues);
        }
        catch (NameNotFoundException e)
        {
            // shouldn't happen because we already verified the package exists
            Log.e(TAG, "NameNotFoundException");
            e.printStackTrace();
        }
    }

    // concatenate two arrays
    private CharSequence[] mergeArray(CharSequence[] a, CharSequence[] b)
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

    // get the current value of the animation preference
    public static boolean getAnimationPref(Context context)
    {
        Log.d(TAG, "getAnimationPref()");
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ANIMATION, KEY_ANIMATION_DEF);
    }

    // get the current value of the sound preference
    public static boolean getSoundPref(Context context)
    {
        Log.d(TAG, "getSoundPref()");
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_SOUND, KEY_SOUND_DEF);
    }

    // get the current value of the vibrate preference
    public static boolean getVibratePref(Context context)
    {
        Log.d(TAG, "getVibratePref()");
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_VIBRATE, KEY_VIBRATE_DEF);
    }

    // get the current value of the shake sensitivity preference
    public static int getForcePref(Context context)
    {
        Log.d(TAG, "getForcePref()");
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(KEY_FORCE, KEY_FORCE_DEF);
    }

    // get the current value of the coin preference
    public static String getCoinPref(Context context)
    {
        Log.d(TAG, "getCoinPref()");
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_COIN, KEY_COIN_DEF);
    }

    // reset the value of the coin preference
    public static void resetCoinPref(Context context)
    {
        Log.d(TAG, "resetCoinPref()");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(KEY_COIN, KEY_COIN_DEF);
        editor.commit();
    }

    // get the persisted flip counter
    public static int getFlipCount(Context context)
    {
        Log.d(TAG, "getFlipCount");
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(KEY_FLIPCOUNT, KEY_FLIPCOUNT_DEF);
    }

    // persist the flip counter for later use
    public static void setFlipCount(Context context, int flipCounter)
    {
        Log.d(TAG, "setFlipCount()");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_FLIPCOUNT, flipCounter);
        editor.commit();
    }
}
