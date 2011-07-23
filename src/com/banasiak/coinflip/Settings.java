/*
 *========================================================================
 * Settings.java
 * Jul 23, 2011 4:43:37 PM | variable
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
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity
{
    // option keys and default values
    private static final String KEY_ANIMATION = "animation";
    private static final boolean KEY_ANIMATION_DEF = true;
    private static final String KEY_SOUND = "sound";
    private static final boolean KEY_SOUND_DEF = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    // get the current value of the animation preference
    public static boolean getAnimationPref(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ANIMATION, KEY_ANIMATION_DEF);
    }

    // get the current value of the sound preference
    public static boolean getSoundPref(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_SOUND, KEY_SOUND_DEF);
    }
}
