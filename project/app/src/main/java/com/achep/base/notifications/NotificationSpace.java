/*
 * Copyright (C) 2015 AChep@xda <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.achep.base.notifications;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.achep.base.Device;

import java.util.ArrayList;

import timber.log.Timber;

import static com.achep.base.Build.DEBUG;

/**
 * A helper class to help to not mess up with ids of notifications.
 *
 * @author Artem Chepurnoy
 */
public class NotificationSpace {

    private static final String TAG = "NotificationSpace";

    private static NotificationSpace sInstance;

    @NonNull
    private final ArrayList<Integer> mList = new ArrayList<>();

    @NonNull
    public static synchronized NotificationSpace getInstance() {
        if (sInstance == null) {
            sInstance = new NotificationSpace();
        }
        return sInstance;
    }

    public void requestRange(int from, int to) {
        int i = 0;
        int length = mList.size();
        for (; i < length; i++) {
            if (mList.get(i) >= from) {
                //noinspection CaughtExceptionImmediatelyRethrown
                try {
                    int a = mList.get(i);
                    if (to >= a) throw new RuntimeException();
                } catch (Exception e) {
                    if (DEBUG) {
                        throw e;
                    } else Timber.tag(TAG).e(e.getMessage());
                }
                break;
            }
        }
        // Add a new range.
        mList.add(i, to);
        mList.add(i, from);
    }

    /**
     * You must call this method before creating a new notification to
     * ensure that you don't mess with ids.
     */
    public void ensure(int id) {
        final int length = mList.size();
        for (int i = 0; i < length; i += 2) {
            if (mList.get(i) <= id && mList.get(i + 1) >= id) return;
        }
        // Oh no
        throw new RuntimeException(
                "You must request range before:"
                        + " id=" + id
                        + " list=" + mList.toString());
    }

    /**
     * @return the string which is granted to be unique among other ids
     * generated by this method.
     */
    public static String generateUniqueTag() {
        final long a = nanoTime();
        //noinspection StatementWithEmptyBody
        while (nanoTime() == a) ; // just to be sure
        // on the other side, converting long to string
        // will probably always last more than 0 ns.
        return Long.toString(nanoTime());
    }

    @SuppressLint("NewApi")
    private static long nanoTime() {
        return Device.hasJellyBeanMR1Api()
                ? SystemClock.elapsedRealtimeNanos()
                : System.nanoTime();
    }

}
