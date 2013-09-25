/*
 *========================================================================
 * Settings.java
 * Sep 25, 2013 11:43 AM | variable
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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

import java.util.List;

public class Settings extends PreferenceActivity {

    // debugging tag
    private static final String TAG = "Settings";

    // add-on package name
    private static final String EXTPKG = "*.coinflipext.*";

    // option keys and default values
    private static final String KEY_ANIMATION = "animation";

    private static final boolean KEY_ANIMATION_DEF = true;

    private static final String KEY_COIN = "coin";

    private static final String KEY_COIN_DEF = "default";

    private static final String KEY_SHAKE = "shake";

    private static final int KEY_SHAKE_DEF = 2;

    private static final String KEY_SOUND = "sound";

    private static final boolean KEY_SOUND_DEF = true;

    private static final String KEY_STATS = "stats";

    private static final boolean KEY_STATS_DEF = false;

    private static final String KEY_TEXT = "text";

    private static final boolean KEY_TEXT_DEF = true;

    private static final String KEY_VIBRATE = "vibrate";

    private static final boolean KEY_VIBRATE_DEF = true;

    private static final String KEY_FLIP_COUNT = "flipCount";

    private static final int KEY_FLIP_COUNT_DEF = 0;

    private static final String KEY_HEADS_COUNT = "headsCount";

    private static final int KEY_HEADS_COUNT_DEF = 0;

    private static final String KEY_TAILS_COUNT = "tailsCount";

    private static final int KEY_TAILS_COUNT_DEF = 0;

    private static final String KEY_SCHEMA_VERSION = "schemaVersion";

    private static final int KEY_SCHEMA_VERSION_DEF = -1;

    private final Util util = new Util(this);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // create a link to the market to search for additional coin packages
        final Preference downloadPref = getPreferenceManager().findPreference("download");
        downloadPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(final Preference preference) {
                final Intent goToMarket = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://search?q=" + EXTPKG));
                startActivity(goToMarket);
                return true;
            }
        });

        // load any external coin packages installed on the system
        loadExtPkgCoins();
    }

    // Load the "coins" available in the add-on package.
    //
    // The theory here is that the add-on package will contain an array of
    // "coins" and "coin_values". The coin_value should be the prefix to the
    // associated resources. For example, if coin_values[1]="washington", all
    // resources should be named "washington_". This gives CoinFlip a
    // mechanism for loading resources from CoinFlipExt without having to be
    // aware of all the contents of CoinFlipExt in advance.
    private void loadExtPkgCoins() {
        Log.d(TAG, "loadExtPkgCoins()");

        try {
            // load the built-in values
            CharSequence[] currentEntries = getResources().getStringArray(R.array.coins);
            CharSequence[] currentEntryValues = getResources().getStringArray(R.array.coins_values);

            final List<PackageInfo> externalPackages = util.findExternalPackages();

            for (final PackageInfo externalPackage : externalPackages) {
                // load the resources from the add-in package
                final Resources extPkgResources = getPackageManager()
                        .getResourcesForApplication(externalPackage.applicationInfo);

                // load the values in the add-in package
                final int coinsId = extPkgResources
                        .getIdentifier("coins", "array", externalPackage.packageName);
                final int coinsValuesId = extPkgResources
                        .getIdentifier("coins_values", "array", externalPackage.packageName);
                final CharSequence[] newEntries = extPkgResources.getStringArray(coinsId);
                final CharSequence[] newEntryValues = extPkgResources.getStringArray(coinsValuesId);

                // merge the add-in values
                currentEntries = util.mergeArray(currentEntries, newEntries);
                currentEntryValues = util.mergeArray(currentEntryValues, newEntryValues);
            }

            if (!externalPackages.isEmpty()) {
                // add a "random coin" option
                final CharSequence[] randomEntry = {"Random Coin"};
                final CharSequence[] randomEntryValue = {"random"};
                currentEntries = util.mergeArray(currentEntries, randomEntry);
                currentEntryValues = util.mergeArray(currentEntryValues, randomEntryValue);
            }

            // update the ListPreference with the combined results
            final ListPreference coinPref = (ListPreference) findPreference("coin");
            coinPref.setEntries(currentEntries);
            coinPref.setEntryValues(currentEntryValues);

        } catch (final NameNotFoundException e) {
            // shouldn't happen because we already verified the package exists
            Log.e(TAG, "NameNotFoundException");
            e.printStackTrace();
        }
    }

    // get the current value of the animation preference
    public static boolean getAnimationPref(final Context context) {
        Log.d(TAG, "getAnimationPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
                context).getBoolean(KEY_ANIMATION, KEY_ANIMATION_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the sound preference
    public static boolean getSoundPref(final Context context) {
        Log.d(TAG, "getSoundPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
                context).getBoolean(KEY_SOUND, KEY_SOUND_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the text preference
    public static boolean getTextPref(final Context context) {
        Log.d(TAG, "getTextPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
                context).getBoolean(KEY_TEXT, KEY_TEXT_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the vibrate preference
    public static boolean getVibratePref(final Context context) {
        Log.d(TAG, "getVibratePref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
                context).getBoolean(KEY_VIBRATE, KEY_VIBRATE_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the shake sensitivity preference
    public static int getShakePref(final Context context) {
        Log.d(TAG, "getShakePref()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
                context).getInt(KEY_SHAKE, KEY_SHAKE_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the statistics preference
    public static boolean getStatsPref(final Context context) {
        Log.d(TAG, "getStatsPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
                context).getBoolean(KEY_STATS, KEY_STATS_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the coin preference
    public static String getCoinPref(final Context context) {
        Log.d(TAG, "getCoinPref()");
        final String result = PreferenceManager.getDefaultSharedPreferences(
                context).getString(KEY_COIN, KEY_COIN_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // reset the value of the coin preference
    public static void resetCoinPref(final Context context) {
        Log.w(TAG, "resetCoinPref()");
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putString(KEY_COIN, KEY_COIN_DEF);
        editor.commit();
    }

    // get the persisted flip counter
    public static int getFlipCount(final Context context) {
        Log.d(TAG, "getFlipCount()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
                context).getInt(KEY_FLIP_COUNT, KEY_FLIP_COUNT_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // persist the flip counter for later use
    public static void setFlipCount(final Context context, final int flipCounter) {
        Log.d(TAG, "setFlipCount()");
        Log.d(TAG, "flipCounter=" + flipCounter);
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_FLIP_COUNT, flipCounter);
        editor.commit();
    }

    // get the persisted heads statistic
    public static int getHeadsCount(final Context context) {
        Log.d(TAG, "getHeadsCount()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
                context).getInt(KEY_HEADS_COUNT, KEY_HEADS_COUNT_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // persist the heads statistic for later use
    public static void setHeadsCount(final Context context, final int headsCounter) {
        Log.d(TAG, "setHeadsCount()");
        Log.d(TAG, "headsCounter=" + headsCounter);
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_HEADS_COUNT, headsCounter);
        editor.commit();
    }

    // get the persisted tails statistic
    public static int getTailsCount(final Context context) {
        Log.d(TAG, "getTailsCount()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
                context).getInt(KEY_TAILS_COUNT, KEY_TAILS_COUNT_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // persist the tails statistic for later use
    public static void setTailsCount(final Context context, final int tailsCounter) {
        Log.d(TAG, "setTailsCount()");
        Log.d(TAG, "tailsCounter=" + tailsCounter);
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_TAILS_COUNT, tailsCounter);
        editor.commit();
    }

    // get the settings schema used by this codebase
    public static int getSchemaVersion(final Context context) {
        Log.d(TAG, "getSchemaVersion()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
                context).getInt(KEY_SCHEMA_VERSION, KEY_SCHEMA_VERSION_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // set the settings schema used by this codebase
    public static void setSchemaVersion(final Context context,
            final int schemaVersion) {
        Log.w(TAG, "setSchemaVersion()");
        Log.w(TAG, "schemaVersion=" + schemaVersion);
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_SCHEMA_VERSION, schemaVersion);
        editor.commit();
    }

    // reset all saved preferences so their default is loaded next time
    public static void resetAllPrefs(final Context context) {
        Log.w(TAG, "resetAllPrefs()");
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.commit();
    }
}
