/*
 *========================================================================
 * Settings.java
 * Feb 10, 2012 6:24:44 PM | variable
 * Copyright (c) 2012 Richard Banasiak
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity
{
    // debugging tag
    private static final String TAG = "Settings";

    // option keys and default values
    private static final String KEY_ANIMATION = "animation";
    private static final boolean KEY_ANIMATION_DEF = true;
    private static final String KEY_COIN = "coin";
    private static final String KEY_COIN_DEF = "minnesota";
    private static final String KEY_SHAKE = "shake";
    private static final int KEY_SHAKE_DEF = 2;
    private static final String KEY_SOUND = "sound";
    private static final boolean KEY_SOUND_DEF = true;
    private static final String KEY_TEXT = "text";
    private static final boolean KEY_TEXT_DEF = true;

    private static final String KEY_FLIPCOUNT = "flipCount";
    private static final int KEY_FLIPCOUNT_DEF = 0;

    private static final String KEY_SCHEMA_VERSION = "schemaVersion";
    private static final int KEY_SCHEMA_VERSION_DEF = -1;


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    // get the current value of the animation preference
    public static boolean getAnimationPref(final Context context)
    {
        Log.d(TAG, "getAnimationPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
            context).getBoolean(KEY_ANIMATION, KEY_ANIMATION_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the sound preference
    public static boolean getSoundPref(final Context context)
    {
        Log.d(TAG, "getSoundPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
            context).getBoolean(KEY_SOUND, KEY_SOUND_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the text preference
    public static boolean getTextPref(final Context context)
    {
        Log.d(TAG, "getTextPref()");
        final Boolean result = PreferenceManager.getDefaultSharedPreferences(
            context).getBoolean(KEY_TEXT, KEY_TEXT_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the shake sensitivity preference
    public static int getShakePref(final Context context)
    {
        Log.d(TAG, "getShakePref()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
            context).getInt(KEY_SHAKE, KEY_SHAKE_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // get the current value of the coin preference
    public static String getCoinPref(final Context context)
    {
        Log.d(TAG, "getCoinPref()");
        final String result = PreferenceManager.getDefaultSharedPreferences(
            context).getString(KEY_COIN, KEY_COIN_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // reset the value of the coin preference
    public static void resetCoinPref(final Context context)
    {
        Log.w(TAG, "resetCoinPref()");
        final SharedPreferences settings = PreferenceManager
            .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putString(KEY_COIN, KEY_COIN_DEF);
        editor.commit();
    }

    // get the persisted flip counter
    public static int getFlipCount(final Context context)
    {
        Log.d(TAG, "getFlipCount()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
            context).getInt(KEY_FLIPCOUNT, KEY_FLIPCOUNT_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // persist the flip counter for later use
    public static void setFlipCount(final Context context, final int flipCounter)
    {
        Log.d(TAG, "setFlipCount()");
        Log.d(TAG, "flipCounter=" + flipCounter);
        final SharedPreferences settings = PreferenceManager
            .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_FLIPCOUNT, flipCounter);
        editor.commit();
    }

    // get the settings schema used by this codebase
    public static int getSchemaVersion(final Context context)
    {
        Log.d(TAG, "getSchemaVersion()");
        final int result = PreferenceManager.getDefaultSharedPreferences(
            context).getInt(KEY_SCHEMA_VERSION, KEY_SCHEMA_VERSION_DEF);
        Log.d(TAG, "result=" + result);
        return result;
    }

    // set the settings schema used by this codebase
    public static void setSchemaVersion(final Context context,
        final int schemaVersion)
    {
        Log.w(TAG, "setSchemaVersion()");
        Log.w(TAG, "schemaVersion=" + schemaVersion);
        final SharedPreferences settings = PreferenceManager
            .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putInt(KEY_SCHEMA_VERSION, schemaVersion);
        editor.commit();
    }

    // reset all saved preferences so their default is loaded next time
    public static void resetAllPrefs(final Context context)
    {
        Log.w(TAG, "resetAllPrefs()");
        final SharedPreferences settings = PreferenceManager
            .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.commit();
    }
}
