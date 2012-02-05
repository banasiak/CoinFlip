/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.banasiak.coinfliphd;

import android.content.Context;
import android.content.res.Resources;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SliderPreference extends Preference implements
OnSeekBarChangeListener
{
    private final static int MAX_SLIDER_VALUE = 5;
    private final static int INITIAL_VALUE = 2;

    private int mValue = INITIAL_VALUE;
    private String mMinText;
    private String mMaxText;

    public SliderPreference(Context context)
    {
        super(context);

        setWidgetLayoutResource(R.layout.slider);
    }

    public SliderPreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.preferenceStyle);

        setWidgetLayoutResource(R.layout.slider);
    }

    public SliderPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        Resources res = context.getResources();
        mMinText = res.getString(R.string.min);
        mMaxText = res.getString(R.string.max);

        setWidgetLayoutResource(R.layout.slider);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        if (mMinText != null)
        {
            TextView minText = (TextView) view.findViewById(R.id.min);
            minText.setText(mMinText);
        }

        if (mMaxText != null)
        {
            TextView maxText = (TextView) view.findViewById(R.id.max);
            maxText.setText(mMaxText);
        }

        SeekBar bar = (SeekBar) view.findViewById(R.id.slider);
        bar.setMax(MAX_SLIDER_VALUE);
        bar.setProgress(mValue);
        bar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (fromUser)
        {
            mValue = progress;
            persistInt(mValue);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        mValue = defaultValue != null ? (Integer) defaultValue : INITIAL_VALUE;

        if (!restoreValue)
        {
            persistInt(mValue);
        }
        else
        {
            mValue = getPersistedInt(mValue);
        }
    }
}