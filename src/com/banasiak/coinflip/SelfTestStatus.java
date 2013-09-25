/*
 *========================================================================
 * SelfTestStatus.java
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


public class SelfTestStatus {

    // debugging tag
    private static final String TAG = "SelfTestStatus";

    public static final int NUMBER_OF_FLIPS = 100000;

    private int headsCount = 0;

    private int tailsCount = 0;

    private long startTime = 0;

    private long endTime = 0;


    public int getHeads() {
        return headsCount;
    }

    public int getTails() {
        return tailsCount;
    }

    public int getTotal() {
        return headsCount + tailsCount;
    }

    public void incrementHeads() {
        headsCount++;
    }

    public void incrementTails() {
        tailsCount++;
    }

    public double getHeadsPercentage() {
        return (double) headsCount / (double) getTotal();
    }

    public double getTailsPercentage() {
        return (double) tailsCount / (double) getTotal();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long iTime) {
        startTime = iTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long iTime) {
        endTime = iTime;
    }

    public long getElapsedTime() {
        return endTime - startTime;
    }

    public double getCompletionPercentage() {
        return (double) getTotal() / (double) NUMBER_OF_FLIPS;
    }

}
