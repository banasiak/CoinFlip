/*
 *========================================================================
 * About.java
 * Jan 24, 2013 7:14 PM | variable
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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity {

    // debugging tag
    private static final String TAG = "About";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // pull the version name from the manifest so it doesn't have to be manually updated in the strings files
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            Log.d(TAG, "versionName=" + versionName);
            TextView versionText = (TextView) findViewById(R.id.about_version_text_view);
            versionText.setText(versionName);
        } catch (NameNotFoundException e) {
            // nothing
        }

        // create a link to the Play store so users can easily rate this app
        Button rateButton = (Button) findViewById(R.id.about_rate_button);
        rateButton.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                final Intent goToMarket = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName()));
                startActivity(goToMarket);
            }
        });
    }
}
