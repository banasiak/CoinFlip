/*
 *========================================================================
 * CoinFlip.java
 * Jan 14, 2012 11:41:20 AM | variable
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

package com.banasiak.coinflip;

import java.util.EnumMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CoinFlip extends Activity
{
    // debugging tag
    private static final String TAG = "CoinFlip";

    // add-on package name
    // private static final String EXTPKG = "com.banasiak.coinflipext";

    // version of the settings schema used by this codebase
    private static final int SCHEMA_VERSION = 5;

    // enumerator of all possible transition states
    private enum ResultState
    {
        HEADS_HEADS,
        HEADS_TAILS,
        TAILS_HEADS,
        TAILS_TAILS,
        UNKNOWN
    }

    EnumMap<ResultState, AnimationDrawable> coinAnimationsMap;
    EnumMap<ResultState, Drawable> coinImagesMap;

    private final Coin theCoin = new Coin();
    private ShakeListener shaker;
    private Boolean currentResult = true;
    private Boolean previousResult = true;
    private ImageView coinImage;
    private TableLayout tableLayout;
    private CustomAnimationDrawable coinAnimationCustom;
    private TextView resultText;
    private TextView instructionsText;
    private SoundPool soundPool;
    private int soundCoin;
    private int soundOneUp;
    private int flipCounter = 0;
    private int shakeForce = 1;

    private final Util util = new Util(this);

    /**
     * Called when the user presses the menu button.
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
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
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        Log.d(TAG, "onOptionsItemSelected()");

        Intent intent;

        switch (item.getItemId())
        {
            case R.id.about_menu:
                intent = new Intent(this, About.class);
                startActivity(intent);
                return true;
            case R.id.selftest_menu:
                intent = new Intent(this, SelfTest.class);
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
    public void onResume()
    {
        Log.d(TAG, "onResume()");

        shakeForce = Settings.getShakePref(this);

        resetCoin();
        resetInstructions(shakeForce);

        if (shakeForce == 0)
        {
            shaker.pause();
        }
        else
        {
            shaker.resume(shakeForce);
        }

        loadResources();

        super.onResume();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause()");
        shaker.pause();
        super.onPause();

        // persist state
        Settings.setFlipCount(this, flipCounter);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        // reset settings if they are from an earlier version.
        // if any setting keys have changed and we don't reset, the app
        // will force close and nasty e-mails soon follow
        if (Settings.getSchemaVersion(this) != SCHEMA_VERSION)
        {
            Settings.resetAllPrefs(this);
            Settings.setSchemaVersion(this, SCHEMA_VERSION);
        }

        // restore state
        flipCounter = Settings.getFlipCount(this);

        setContentView(R.layout.main);

        // initialize the coin image and result text views
        initViews();

        // initialize the sounds
        initSounds();

        // initialize the coin maps
        coinAnimationsMap = new EnumMap<CoinFlip.ResultState, AnimationDrawable>(ResultState.class);
        coinImagesMap = new EnumMap<CoinFlip.ResultState, Drawable>(ResultState.class);

        // initialize the shake listener
        shaker = new ShakeListener(this);
        shaker.pause();
        shaker.setOnShakeListener(new ShakeListener.OnShakeListener()
        {
            public void onShake()
            {
                flipCoin();
            }
        });

        // initialize the onclick listener
        tableLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(final View v)
            {
                flipCoin();
            }
        });
    }

    private void flipCoin()
    {
        Log.d(TAG, "flipCoin()");

        flipCounter++;
        Log.d(TAG, "flipCounter=" + flipCounter);

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // we're in the process of flipping the coin
        ResultState resultState = ResultState.UNKNOWN;

        // pause the shake listener until the result is rendered
        shaker.pause();

        // vibrate if enabled
        if (Settings.getVibratePref(this))
        {
            vibrator.vibrate(100);
        }

        // flip the coin and update the state with the result
        resultState = updateState(theCoin.flip());

        // update the screen with the result of the flip
        renderResult(resultState);

    }

    private void resetCoin()
    {
        Log.d(TAG, "resetCoin()");

        // hide the animation and draw the reset image
        displayCoinAnimation(false);
        displayCoinImage(true);
        coinImage.setImageDrawable(getResources().getDrawable(R.drawable.unknown));
        resultText.setText("");
        coinAnimationsMap.clear();
        coinImagesMap.clear();
    }

    private void resetInstructions(final int force)
    {
        Log.d(TAG, "resetInstructions()");

        if (force == 0)
        {
            instructionsText.setText(R.string.instructions_tap_tv);
        }
        else
        {
            instructionsText.setText(R.string.instructions_tap_shake_tv);
        }
    }

    private ResultState updateState(final boolean flipResult)
    {
        // Analyze the current coin state and the new coin state and determine
        // the proper transition between the two.
        // true = HEADS | false = TAILS

        Log.d(TAG, "updateState()");

        ResultState resultState = ResultState.UNKNOWN;
        currentResult = flipResult;

        // this is easier to read than the old code
        if (previousResult == true && currentResult == true)
        {
            resultState = ResultState.HEADS_HEADS;
        }
        if (previousResult == true && currentResult == false)
        {
            resultState = ResultState.HEADS_TAILS;
        }
        if (previousResult == false && currentResult == true)
        {
            resultState = ResultState.TAILS_HEADS;
        }
        if (previousResult == false && currentResult == false)
        {
            resultState = ResultState.TAILS_TAILS;
        }

        // update the previousResult for the next flip
        previousResult = currentResult;

        return resultState;
    }

    private BitmapDrawable resizeBitmapDrawable(final BitmapDrawable image,
        final int width, final int height)
    {
        // load the transparent background and convert to a bitmap
        BitmapDrawable background = (BitmapDrawable) getResources().getDrawable(R.drawable.background);
        Bitmap background_bm = background.getBitmap();

        // convert the passed in image to a bitmap and resize according to parameters
        Bitmap image_bm = Bitmap.createScaledBitmap(image.getBitmap(), width, height, true);

        // create a new canvas to combine the two images on
        Bitmap comboImage_bm = Bitmap.createBitmap(background_bm.getWidth(), background_bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(comboImage_bm);

        // add the background as well as the new image to the horizontal center
        // of the image
        comboImage.drawBitmap(background_bm, 0f, 0f, null);
        comboImage.drawBitmap(image_bm, (background_bm.getWidth() - image_bm.getWidth()) / 2, 0f, null);

        // convert the new combo image bitmap to a BitmapDrawable
        final BitmapDrawable comboImage_bmd = new BitmapDrawable(comboImage_bm);

        // I don't know if this is the right thing to do, but this method
        // usually always blows out the heap on pre-Froyo devices.  Clearing
        // the temporary resources and recommending a GC seems to help.
        background = null;
        background_bm = null;
        image_bm = null;
        comboImage_bm = null;
        comboImage = null;
        System.gc();

        return comboImage_bmd;
    }

    private AnimationDrawable generateAnimatedDrawable(final Drawable imageA,
        final Drawable imageB, final Drawable edge,
        final ResultState resultState)
    {
        final AnimationDrawable animation = new AnimationDrawable();
        final int widthA = ((BitmapDrawable) imageA).getBitmap().getWidth();
        final int heightA = ((BitmapDrawable) imageA).getBitmap().getHeight();
        final int widthB = ((BitmapDrawable) imageB).getBitmap().getWidth();
        final int heightB = ((BitmapDrawable) imageB).getBitmap().getHeight();

        // create the individual animation frames for the heads side
        final BitmapDrawable imageA_8 = (BitmapDrawable) imageA;
        final BitmapDrawable imageA_6 = resizeBitmapDrawable(imageA_8, (int) (widthA * 0.75), heightA);
        final BitmapDrawable imageA_4 = resizeBitmapDrawable(imageA_8, (int) (widthA * 0.50), heightA);
        final BitmapDrawable imageA_2 = resizeBitmapDrawable(imageA_8, (int) (widthA * 0.25), heightA);

        // create the individual animation frames for the tails side
        final BitmapDrawable imageB_8 = (BitmapDrawable) imageB;
        final BitmapDrawable imageB_6 = resizeBitmapDrawable(imageB_8, (int) (widthB * 0.75), heightB);
        final BitmapDrawable imageB_4 = resizeBitmapDrawable(imageB_8, (int) (widthB * 0.50), heightB);
        final BitmapDrawable imageB_2 = resizeBitmapDrawable(imageB_8, (int) (widthB * 0.25), heightB);

        // create the appropriate animation depending on the result state
        final int duration = 20;
        switch (resultState)
        {
            case HEADS_HEADS:
                // Begin Flip 1
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 2
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 3
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                break;
            case HEADS_TAILS:
                // Begin Flip 1
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 2
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                // Begin Flip 3 (half flip)
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                break;
            case TAILS_HEADS:
                // Begin Flip 1
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 2
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 3 (half flip)
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                break;
            case TAILS_TAILS:
                // Begin Flip 1
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 2
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                // Begin Flip 3
                animation.addFrame(imageB_8, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_8, duration);
                animation.addFrame(imageA_6, duration);
                animation.addFrame(imageA_4, duration);
                animation.addFrame(imageA_2, duration);
                animation.addFrame(edge, duration);
                animation.addFrame(imageB_2, duration);
                animation.addFrame(imageB_4, duration);
                animation.addFrame(imageB_6, duration);
                animation.addFrame(imageB_8, duration);
                break;
            default:
                Log.w(TAG, "Invalid state. Resetting coin.");
                resetCoin();
                break;
        }

        animation.setOneShot(true);

        return animation;
    }

    // check the coin preference and determine how to load its resources
    private void loadResources()
    {
        Log.d(TAG, "loadResources()");

        // determine coin type to draw
        String coinPrefix = Settings.getCoinPref(this);

        if (coinPrefix.equals("random"))
        {
            Log.d(TAG, "Random coin selected");
            coinPrefix = util.getRandomCoin();
        }

        if (coinPrefix.equals("default"))
        {
            Log.d(TAG, "Default coin selected");
            loadInternalResources();
        }
        else if (coinPrefix.equals("custom"))
        {
            Log.d(TAG, "Custom coin selected");
            loadCustomResources();
        }
        else
        {
            Log.d(TAG, "Add-on coin selected");
            loadExternalResources(coinPrefix);
        }
    }

    // load resources internal to the CoinFlip package
    private void loadInternalResources()
    {
        Log.d(TAG, "loadInternalResources()");

        AnimationDrawable coinAnimation;
        ResultState resultState;

        // load the images
        final Drawable heads = getResources().getDrawable(R.drawable.heads);
        final Drawable tails = getResources().getDrawable(R.drawable.tails);
        final Drawable edge = getResources().getDrawable(R.drawable.edge);

        // only do all the CPU-intensive animation rendering if animations are
        // enabled
        if (Settings.getAnimationPref(this))
        {
            // render the animation for each result state and store it in the
            // animations map
            resultState = ResultState.HEADS_HEADS;
            coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
            coinAnimationsMap.put(resultState, coinAnimation);

            resultState = ResultState.HEADS_TAILS;
            coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
            coinAnimationsMap.put(resultState, coinAnimation);

            resultState = ResultState.TAILS_HEADS;
            coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
            coinAnimationsMap.put(resultState, coinAnimation);

            resultState = ResultState.TAILS_TAILS;
            coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
            coinAnimationsMap.put(resultState, coinAnimation);
        }

        // add the appropriate image for each result state to the images map
        // WTF? There's some kind of rendering bug if you use the "heads" or
        // "tails" variables here...
        coinImagesMap.put(ResultState.HEADS_HEADS, getResources().getDrawable(R.drawable.heads));
        coinImagesMap.put(ResultState.HEADS_TAILS, getResources().getDrawable(R.drawable.tails));
        coinImagesMap.put(ResultState.TAILS_HEADS, getResources().getDrawable(R.drawable.heads));
        coinImagesMap.put(ResultState.TAILS_TAILS, getResources().getDrawable(R.drawable.tails));
    }

    // load resources from the external CoinFlipExt package
    private void loadExternalResources(final String coinPrefix)
    {
        Log.d(TAG, "loadExternalResources()");

        AnimationDrawable coinAnimation;
        ResultState resultState;

        try
        {
            // figure out which add-on package contains the resources we need for this coin prefix
            final String packageName = util.findExternalResourcePackage(coinPrefix);

            if (packageName == null)
            {
                // the coin prefix doesn't exist in any external package
                Toast.makeText(this, R.string.toast_coin_error, Toast.LENGTH_SHORT).show();
                Settings.resetCoinPref(this);
                loadResources();
                return;
            }

            final Resources extPkgResources = getPackageManager().getResourcesForApplication(packageName);

            // load the image IDs from the add-in package
            final int headsId = util.getExternalResourceHeads(packageName, extPkgResources, coinPrefix);
            final int tailsId = util.getExternalResourceTails(packageName, extPkgResources, coinPrefix);
            final int edgeId = util.getExternalResourceEdge(packageName, extPkgResources, coinPrefix);

            // load the images from the add-in package via their ID
            final Drawable heads = extPkgResources.getDrawable(headsId);
            final Drawable tails = extPkgResources.getDrawable(tailsId);
            final Drawable edge = extPkgResources.getDrawable(edgeId);

            // only do all the CPU-intensive animation rendering if animations are enabled
            if (Settings.getAnimationPref(this))
            {
                // render the animation for each result state and store it in the animations map
                resultState = ResultState.HEADS_HEADS;
                coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
                coinAnimationsMap.put(resultState, coinAnimation);

                resultState = ResultState.HEADS_TAILS;
                coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
                coinAnimationsMap.put(resultState, coinAnimation);

                resultState = ResultState.TAILS_HEADS;
                coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
                coinAnimationsMap.put(resultState, coinAnimation);

                resultState = ResultState.TAILS_TAILS;
                coinAnimation = generateAnimatedDrawable(heads, tails, edge, resultState);
                coinAnimationsMap.put(resultState, coinAnimation);
            }

            // add the appropriate image for each result state to the images map
            // WTF? There's (still) some kind of rendering bug if you use the
            // "heads" or "tails" variables here...
            coinImagesMap.put(ResultState.HEADS_HEADS, extPkgResources.getDrawable(headsId));
            coinImagesMap.put(ResultState.HEADS_TAILS, extPkgResources.getDrawable(tailsId));
            coinImagesMap.put(ResultState.TAILS_HEADS, extPkgResources.getDrawable(headsId));
            coinImagesMap.put(ResultState.TAILS_TAILS, extPkgResources.getDrawable(tailsId));

        }
        catch (final NameNotFoundException e)
        {
            Log.e(TAG, "NameNotFoundException");
            e.printStackTrace();
        }
        catch (final NotFoundException e)
        {
            Log.e(TAG, "NotFoundException " + e.getMessage());
        }

    }

    private void loadCustomResources()
    {
        // TODO one day we'll be able to load custom images from the SD card...
        // ... but not today.
        Settings.resetCoinPref(this);
        loadResources();
    }

    private void renderResult(final ResultState resultState)
    {
        Log.d(TAG, "renderResult()");

        AnimationDrawable coinAnimation;
        Drawable coinImageDrawable;

        // hide the static image and clear the text
        displayCoinImage(false);
        displayCoinAnimation(false);
        resultText.setText("");

        // display the result
        if (Settings.getAnimationPref(this))
        {
            // load the appropriate coin animation based on the state
            coinAnimation = coinAnimationsMap.get(resultState);
            coinAnimationCustom = new CustomAnimationDrawable(coinAnimation)
            {
                @Override
                void onAnimationFinish()
                {
                    playCoinSound();
                    updateResultText(resultState);
                    if (shakeForce != 0)
                    {
                        shaker.resume(shakeForce);
                    }
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
        }
        else
        {
            // load the appropriate coin image based on the state
            coinImageDrawable = coinImagesMap.get(resultState);
            coinImage.setImageDrawable(coinImageDrawable);

            // hide the animation and display the static image
            displayCoinImage(true);
            displayCoinAnimation(false);
            playCoinSound();
            updateResultText(resultState);
            if (shakeForce != 0)
            {
                shaker.resume(shakeForce);
            }
        }
    }

    private void initSounds()
    {
        // MediaPlayer was causing ANR issues on some devices.
        // SoundPool should be more efficient.

        Log.d(TAG, "initSounds()");
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        soundCoin = soundPool.load(this, R.raw.coin, 1);
        soundOneUp = soundPool.load(this, R.raw.oneup, 1);

    }

    private void playSound(final int sound)
    {
        Log.d(TAG, "playSound()");
        if (Settings.getSoundPref(this))
        {
            final AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            final float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            final float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            final float volume = streamVolumeCurrent / streamVolumeMax;

            soundPool.play(sound, volume, volume, 1, 0, 1f);
        }
    }

    private void playCoinSound()
    {
        Log.d(TAG, "playCoinSound()");

        synchronized (this)
        {
            if (flipCounter < 100)
            {
                playSound(soundCoin);
            }
            else
            {
                // Happy Easter! (For Ryan)
                // Toast.makeText(this, "1-UP", Toast.LENGTH_SHORT).show();
                playSound(soundOneUp);
                flipCounter = 0;
            }
        }
    }

    private void updateResultText(final ResultState resultState)
    {
        Log.d(TAG, "updateResultText()");

        if (Settings.getTextPref(this))
        {
            switch (resultState)
            {
                case HEADS_HEADS:
                case TAILS_HEADS:
                    resultText.setText(R.string.heads);
                    resultText.setTextColor(Color.parseColor("green"));
                    break;
                case HEADS_TAILS:
                case TAILS_TAILS:
                    resultText.setText(R.string.tails);
                    resultText.setTextColor(Color.parseColor("red"));
                    break;
                default:
                    resultText.setText(R.string.unknown);
                    resultText.setTextColor(Color.parseColor("yellow"));
                    break;
            }
        }
        else
        {
            resultText.setText("");
        }
    }

    private void displayCoinAnimation(final boolean flag)
    {
        Log.d(TAG, "displayCoinAnimation()");

        // safety first!
        if (coinAnimationCustom != null)
        {
            if (flag)
            {
                coinAnimationCustom.setAlpha(255);
            }
            else
            {
                coinAnimationCustom.setAlpha(0);
            }
        }
    }

    private void displayCoinImage(final boolean flag)
    {
        Log.d(TAG, "displayCoinImage()");

        // safety first!
        if (coinImage != null)
        {
            if (flag)
            {
                // get rid of the animation background
                coinImage.setBackgroundDrawable(null);
                coinImage.setAlpha(255);
            }
            else
            {
                coinImage.setAlpha(0);
            }
        }
    }

    private void initViews()
    {
        Log.d(TAG, "initViews()");

        coinImage = (ImageView) findViewById(R.id.coin_image_view);
        resultText = (TextView) findViewById(R.id.result_text_view);
        instructionsText = (TextView) findViewById(R.id.instructions_text_view);
        tableLayout = (TableLayout) findViewById(R.id.table_layout);
    }
}
