/*
 *========================================================================
 * CoinFlipActivity.java
 * Nov 15, 2014 4:50 PM | variable
 * Copyright (c) 2014 Richard Banasiak
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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // verify we received a ACTION_PACKAGE_ADDED intent (which is all we subscribed to in the manifest)
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            // verify the package that was installed is a package name we're interested in
            if (intent.getData() != null && intent.getDataString().contains(Settings.EXTPKG)) {
                // restart the app so new coins are loaded
                Intent launchIntent = new Intent(context, CoinFlipActivity.class);
                launchIntent.putExtra(CoinFlipActivity.OPEN_SETTINGS_FLAG, true);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
                System.exit(2);
            }
        }
    }

}
