/*
 *========================================================================
 * CoinFlipActivity.java
 * Oct 23, 2014 12:07 PM | variable
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.banasiak.coinflip.lib.Animation;
import com.banasiak.coinflip.lib.Coin;
import com.banasiak.coinflip.lib.CustomAnimationDrawable;
import com.banasiak.coinflip.lib.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;

public class CoinFlipActivity extends Activity {

    // debugging tag
    private static final String TAG = CoinFlipActivity.class.getSimpleName();

    // add-on package name
    // private static final String EXTPKG = "com.banasiak.coinflipext";

    // version of the settings schema used by this codebase
    private static final int SCHEMA_VERSION = 6;

    EnumMap<Animation.ResultState, Drawable> coinImagesMap;

    private Drawable heads = null;

    private Drawable tails = null;

    private Drawable edge = null;

    private Drawable background = null;

    private final Coin theCoin = new Coin();

    private ShakeListener shaker;

    private OnClickListener tapper;

    private Boolean currentResult = true;

    private Boolean previousResult = true;

    private ImageView coinImage;

    private LinearLayout mainLayout;

    private CustomAnimationDrawable coinAnimationCustom;

    private TextView resultText;

    private TextView instructionsText;

    private TextView headsStatText;

    private TextView tailsStatText;

    private Button statsResetButton;

    private LinearLayout statsLayout;

    private SoundPool soundPool;

    private int soundCoin;

    private int soundOneUp;

    private int flipCounter = 0;

    private int headsCounter = 0;

    private int tailsCounter = 0;

    private GoogleApiClient googleApiClient = null;

    /**
     * Called when the user presses the menu button.
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Called when the user selects an item from the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");

        Intent intent;

        switch (item.getItemId()) {
            case R.id.about_menu:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.selftest_menu:
                intent = new Intent(this, SelfTestActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings_menu:
                intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.exit:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");

        resetCoin();
        resetInstructions();
        loadResources();
        updateStatsText();
        resumeListeners();

        connectToWearable();

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");

        pauseListeners();

        if (coinAnimationCustom != null) {
            // shut down the animation thread, otherwise the callback will resume the shake
            // listener in the background even though the app is supposed to be suspended
            coinAnimationCustom.removeCallbacks();
        }

        // persist state
        Settings.setFlipCount(this, flipCounter);
        Settings.setHeadsCount(this, headsCounter);
        Settings.setTailsCount(this, tailsCounter);

        super.onPause();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        initializeWearable();

        // reset settings if they are from an earlier version.
        // if any setting keys have changed and we don't reset, the app
        // will force close and nasty e-mails soon follow
        if (Settings.getSchemaVersion(this) != SCHEMA_VERSION) {
            Settings.resetAllPrefs(this);
            Settings.setSchemaVersion(this, SCHEMA_VERSION);
        }

        // restore state
        flipCounter = Settings.getFlipCount(this);
        headsCounter = Settings.getHeadsCount(this);
        tailsCounter = Settings.getTailsCount(this);

        setContentView(R.layout.main);

        // initialize the coin image and result text views
        initViews();

        // initialize the sounds
        initSounds();

        // initialize the coin maps
        Animation.init();
        coinImagesMap = new EnumMap<Animation.ResultState, Drawable>(Animation.ResultState.class);

        // initialize the shake listener
        if (shaker == null) {
            shaker = new ShakeListener(this);
            shaker.pause();
            shaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
                public void onShake() {
                    flipCoin();
                }
            });
        }

        // initialize the onclick listener
        if (tapper == null) {
            tapper = new OnClickListener() {
                @Override public void onClick(View v) {
                    flipCoin();
                }
            };
        }

        statsResetButton.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                resetStatistics();
            }
        });
    }

    private void initializeWearable() {
        Log.d(TAG, "initializeWearable()");
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "Wearable connected");
                            // Now you can use the data layer API
                            pushCoinsToWearable();
                        }

                        @Override public void onConnectionSuspended(int cause) {
                            Log.d(TAG, "Wearable connection suspended");

                        }
                    })
                    .addOnConnectionFailedListener(
                            new GoogleApiClient.OnConnectionFailedListener() {
                                @Override public void onConnectionFailed(
                                        ConnectionResult result) {
                                    Log.d(TAG, "Wearable connection failed");

                                }
                            }
                    )
                    .addApi(Wearable.API)
                    .build();
        }
    }

    private void connectToWearable() {
        Log.d(TAG, "connectToWearable()");
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private void pushCoinsToWearable() {
        Util.execute(new AsyncTask<Void, Void, Void>() {
            @Override protected Void doInBackground(Void... params) {
                Log.d(TAG, "pushCoinsToWearable()");
                if (heads != null && tails != null && edge != null && background != null) {

                    Asset aHeads = createAssetForWearable(heads);
                    Asset aTails = createAssetForWearable(tails);
                    Asset aEdge = createAssetForWearable(edge);
                    Asset aBackground = createAssetForWearable(background);

                    if (googleApiClient != null && googleApiClient.isConnected()) {
                        PutDataMapRequest dataMap = PutDataMapRequest.create("/coins");
                        dataMap.getDataMap().putAsset("heads", aHeads);
                        dataMap.getDataMap().putAsset("tails", aTails);
                        dataMap.getDataMap().putAsset("edge", aEdge);
                        dataMap.getDataMap().putAsset("background", aBackground);

                        PutDataRequest request = dataMap.asPutDataRequest();

                        Log.d(TAG, "Sending assets to wearable");
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                .putDataItem(googleApiClient, request);

                        pendingResult
                                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                    @Override public void onResult(
                                            DataApi.DataItemResult dataItemResult) {
                                        if (dataItemResult.getStatus().isSuccess()) {
                                            Log.d(TAG,
                                                    "Data item set: " + dataItemResult.getDataItem()
                                                            .getUri());
                                            googleApiClient.disconnect();
                                        }

                                    }
                                });
                    }
                }
                return null;
            }
        });
    }

    private Asset createAssetForWearable(Drawable image) {
        Log.d(TAG, "createAssetForWearable()");
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        // 280dp @ 1.5x (HDPI) = 420px
        Bitmap bitmap = Bitmap.createScaledBitmap(b, 420, 420, false);
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private void flipCoin() {
        Log.d(TAG, "flipCoin()");

        flipCounter++;
        Log.d(TAG, "flipCounter=" + flipCounter);

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // we're in the process of flipping the coin
        Animation.ResultState resultState = Animation.ResultState.UNKNOWN;

        // pause the shake listener until the result is rendered
        pauseListeners();

        // vibrate if enabled
        if (Settings.getVibratePref(this)) {
            vibrator.vibrate(100);
        }

        // flip the coin and update the state with the result
        boolean flipResult = theCoin.flip();
        if (flipResult) {
            headsCounter++;
        } else {
            tailsCounter++;
        }
        Log.d(TAG, "headsCounter=" + headsCounter + " | tailsCounter=" + tailsCounter);
        resultState = updateState(flipResult);

        // update the screen with the result of the flip
        renderResult(resultState);

    }

    private void resetCoin() {
        Log.d(TAG, "resetCoin()");

        // hide the animation and draw the reset image
        displayCoinAnimation(false);
        displayCoinImage(true);
        coinImage.setImageDrawable(getResources().getDrawable(R.drawable.unknown));
        resultText.setText(" ");
        Animation.clearAnimations();
        coinImagesMap.clear();
    }

    private void resetInstructions() {
        Log.d(TAG, "resetInstructions()");

        int shakeForce = Settings.getShakePref(this);

        if (shakeForce == 0) {
            instructionsText.setText(R.string.instructions_tap_tv);
        } else {
            instructionsText.setText(R.string.instructions_tap_shake_tv);
        }
    }

    private Animation.ResultState updateState(final boolean flipResult) {
        // Analyze the current coin state and the new coin state and determine
        // the proper transition between the two.
        // true = HEADS | false = TAILS

        Log.d(TAG, "updateState()");

        Animation.ResultState resultState = Animation.ResultState.UNKNOWN;
        currentResult = flipResult;

        // this is easier to read than the old code
        if (previousResult == true && currentResult == true) {
            resultState = Animation.ResultState.HEADS_HEADS;
        }
        if (previousResult == true && currentResult == false) {
            resultState = Animation.ResultState.HEADS_TAILS;
        }
        if (previousResult == false && currentResult == true) {
            resultState = Animation.ResultState.TAILS_HEADS;
        }
        if (previousResult == false && currentResult == false) {
            resultState = Animation.ResultState.TAILS_TAILS;
        }

        // update the previousResult for the next flip
        previousResult = currentResult;

        return resultState;
    }

    // check the coin preference and determine how to load its resources
    private void loadResources() {
        Log.d(TAG, "loadResources()");

        // determine coin type to draw
        String coinPrefix = Settings.getCoinPref(this);

        if (coinPrefix.equals("random")) {
            Log.d(TAG, "Random coin selected");
            coinPrefix = Util.getRandomCoin(this, R.array.coins_values);
        }

        if (coinPrefix.equals("default")) {
            Log.d(TAG, "Default coin selected");
            loadInternalResources();
        } else if (coinPrefix.equals("custom")) {
            Log.d(TAG, "Custom coin selected");
            loadCustomResources();
        } else {
            Log.d(TAG, "Add-on coin selected");
            loadExternalResources(coinPrefix);
        }
    }

    // load resources internal to the CoinFlip package
    private void loadInternalResources() {
        Log.d(TAG, "loadInternalResources()");

        // load the images
        heads = getResources().getDrawable(R.drawable.heads);
        tails = getResources().getDrawable(R.drawable.tails);
        edge = getResources().getDrawable(R.drawable.edge);
        background = getResources().getDrawable(R.drawable.background);

        // only do all the CPU-intensive animation rendering if animations are enabled
        if (Settings.getAnimationPref(this)) {
            // render the animation for each result state and store it in the
            // animations map
            Animation.generateAnimations(heads, tails, edge, background);

        }

        // add the appropriate image for each result state to the images map
        // WTF? There's some kind of rendering bug if you use the "heads" or
        // "tails" variables here...
        coinImagesMap.put(Animation.ResultState.HEADS_HEADS, heads);
        coinImagesMap.put(Animation.ResultState.HEADS_TAILS, tails);
        coinImagesMap.put(Animation.ResultState.TAILS_HEADS, heads);
        coinImagesMap.put(Animation.ResultState.TAILS_TAILS, tails);
    }

    // load resources from the external CoinFlipExt package
    private void loadExternalResources(final String coinPrefix) {
        Log.d(TAG, "loadExternalResources()");

        try {
            // figure out which add-on package contains the resources we need for this coin prefix
            final String packageName = Util.findExternalResourcePackage(this, coinPrefix);

            if (packageName == null) {
                // the coin prefix doesn't exist in any external package
                Toast.makeText(this, R.string.toast_coin_error, Toast.LENGTH_SHORT).show();
                Settings.resetCoinPref(this);
                loadResources();
                return;
            }

            final Resources extPkgResources = getPackageManager().getResourcesForApplication(
                    packageName);

            // load the image IDs from the add-in package
            final int headsId = Util.getExternalResourceHeads(packageName, extPkgResources,
                    coinPrefix);
            final int tailsId = Util.getExternalResourceTails(packageName, extPkgResources,
                    coinPrefix);
            final int edgeId = Util.getExternalResourceEdge(packageName, extPkgResources,
                    coinPrefix);

            // load the images from the add-in package via their ID
            heads = extPkgResources.getDrawable(headsId);
            tails = extPkgResources.getDrawable(tailsId);
            edge = extPkgResources.getDrawable(edgeId);
            background = getResources().getDrawable(R.drawable.background);

            // only do all the CPU-intensive animation rendering if animations are enabled
            if (Settings.getAnimationPref(this)) {
                // render the animation for each result state and store it in the
                // animations map
                Animation.generateAnimations(heads, tails, edge, background);
            }

            // add the appropriate image for each result state to the images map
            // WTF? There's (still) some kind of rendering bug if you use the
            // "heads" or "tails" variables here...
            coinImagesMap.put(Animation.ResultState.HEADS_HEADS, heads);
            coinImagesMap.put(Animation.ResultState.HEADS_TAILS, tails);
            coinImagesMap.put(Animation.ResultState.TAILS_HEADS, heads);
            coinImagesMap.put(Animation.ResultState.TAILS_TAILS, tails);

        } catch (final NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException");
            e.printStackTrace();
        } catch (final NotFoundException e) {
            Log.e(TAG, "NotFoundException " + e.getMessage());
        }

    }

    private void loadCustomResources() {
        // TODO: one day we'll be able to load custom images from the SD card...
        // ... but not today.
        Settings.resetCoinPref(this);
        loadResources();
    }

    private void renderResult(final Animation.ResultState resultState) {
        Log.d(TAG, "renderResult()");

        AnimationDrawable coinAnimation;
        Drawable coinImageDrawable;

        // hide the static image and clear the text
        displayCoinImage(false);
        displayCoinAnimation(false);
        resultText.setText("");

        // display the result
        if (Settings.getAnimationPref(this)) {
            // load the appropriate coin animation based on the state
            coinAnimation = Animation.getAnimation(resultState);
            coinAnimationCustom = new CustomAnimationDrawable(coinAnimation) {
                @Override
                protected void onAnimationFinish() {
                    playCoinSound();
                    updateResultText(resultState);
                    resumeListeners();
                }
            };

            // hide the static image and render the animation
            displayCoinImage(false);
            displayCoinAnimation(true);
            coinImage.setBackgroundDrawable(coinAnimationCustom);
            coinAnimationCustom.start();
            // handled by animation callback
            // playCoinSound();
            // updateResultText(resultState, resultText);
        } else {
            // load the appropriate coin image based on the state
            coinImageDrawable = coinImagesMap.get(resultState);
            coinImage.setImageDrawable(coinImageDrawable);

            // hide the animation and display the static image
            displayCoinImage(true);
            displayCoinAnimation(false);
            playCoinSound();
            updateResultText(resultState);
            resumeListeners();
        }
    }

    private void initSounds() {
        // MediaPlayer was causing ANR issues on some devices.
        // SoundPool should be more efficient.

        Log.d(TAG, "initSounds()");
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        soundCoin = soundPool.load(this, R.raw.coin, 1);
        soundOneUp = soundPool.load(this, R.raw.oneup, 1);

    }

    private void playSound(final int sound) {
        Log.d(TAG, "playSound()");
        if (Settings.getSoundPref(this)) {
            final AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            final float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            final float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            final float volume = streamVolumeCurrent / streamVolumeMax;

            soundPool.play(sound, volume, volume, 1, 0, 1f);
        }
    }

    private void playCoinSound() {
        Log.d(TAG, "playCoinSound()");

        synchronized (this) {
            if (flipCounter < 100) {
                playSound(soundCoin);
            } else {
                // Happy Easter! (For Ryan)
                // Toast.makeText(this, "1-UP", Toast.LENGTH_SHORT).show();
                playSound(soundOneUp);
                flipCounter = 0;
            }
        }
    }

    private void updateResultText(final Animation.ResultState resultState) {
        Log.d(TAG, "updateResultText()");

        if (Settings.getTextPref(this)) {
            switch (resultState) {
                case HEADS_HEADS:
                case TAILS_HEADS:
                    resultText.setText(R.string.heads);
                    resultText.setTextColor(getResources().getColor(R.color.lime));
                    break;
                case HEADS_TAILS:
                case TAILS_TAILS:
                    resultText.setText(R.string.tails);
                    resultText.setTextColor(getResources().getColor(R.color.red));
                    break;
                default:
                    resultText.setText(R.string.unknown);
                    resultText.setTextColor(getResources().getColor(R.color.yellow));
                    break;
            }
        } else {
            resultText.setText("");
        }

        updateStatsText();

    }

    private void updateStatsText() {
        Log.d(TAG, "updateStatsText()");
        if (Settings.getStatsPref(this)) {
            statsLayout.setVisibility(View.VISIBLE);
        } else {
            statsLayout.setVisibility(View.INVISIBLE);
        }
        headsStatText.setText(Integer.toString(headsCounter));
        tailsStatText.setText(Integer.toString(tailsCounter));
    }

    private void resetStatistics() {
        Log.d(TAG, "resetStatistics()");
        headsCounter = 0;
        tailsCounter = 0;
        updateStatsText();
    }

    private void displayCoinAnimation(final boolean flag) {
        Log.d(TAG, "displayCoinAnimation()");

        // safety first!
        if (coinAnimationCustom != null) {
            if (flag) {
                coinAnimationCustom.setAlpha(255);
            } else {
                coinAnimationCustom.setAlpha(0);
            }
        }
    }

    private void displayCoinImage(final boolean flag) {
        Log.d(TAG, "displayCoinImage()");

        // safety first!
        if (coinImage != null) {
            if (flag) {
                // get rid of the animation background
                coinImage.setBackgroundDrawable(null);
                coinImage.setAlpha(255);
            } else {
                coinImage.setAlpha(0);
            }
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews()");

        coinImage = (ImageView) findViewById(R.id.coin_image_view);
        resultText = (TextView) findViewById(R.id.result_text_view);
        instructionsText = (TextView) findViewById(R.id.instructions_text_view);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        headsStatText = (TextView) findViewById(R.id.heads_stat_text_view);
        tailsStatText = (TextView) findViewById(R.id.tails_stat_text_view);
        statsResetButton = (Button) findViewById(R.id.stats_reset_button);
        statsLayout = (LinearLayout) findViewById(R.id.statistics_layout);
    }

    private void pauseListeners() {
        Log.d(TAG, "pauseListeners()");
        if (shaker != null) {
            shaker.pause();
        }
        if (tapper != null) {
            mainLayout.setOnClickListener(null);
        }
    }

    private void resumeListeners() {
        Log.d(TAG, "resumeListeners()");

        int shakeForce = Settings.getShakePref(this);

        if (shaker != null) {
            if (shakeForce == 0) {
                shaker.pause();
            } else {
                shaker.resume(shakeForce);
            }
        }
        if (tapper != null) {
            mainLayout.setOnClickListener(tapper);
        }
    }
}
