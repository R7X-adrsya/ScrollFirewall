package com.example.digitaldetox;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class SessionManager {
    private static final String PREF_NAME = "DigitalDetoxPrefs";
    private static final String KEY_ACCUMULATED_TIME = "accumulatedTime";
    private static final String KEY_LAST_RESET_TIME = "lastResetTime";
    private static final String KEY_TEST_MODE = "testMode";

    // Defaults in milliseconds
    private static final long LIMIT_DEFAULT = 10 * 60 * 1000L; // 10 minutes
    private static final long LIMIT_TEST_MODE = 60 * 1000L; // 1 minute

    private final SharedPreferences prefs;
    
    private long sessionStartTime = 0;
    private boolean isTracking = false;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkDailyReset();
    }

    private void checkDailyReset() {
        long lastResetTime = prefs.getLong(KEY_LAST_RESET_TIME, 0);
        if (isNewDay(lastResetTime)) {
            resetAll();
        }
    }

    private boolean isNewDay(long lastResetTime) {
        if (lastResetTime == 0) return true;

        Calendar lastReset = Calendar.getInstance();
        lastReset.setTimeInMillis(lastResetTime);

        Calendar now = Calendar.getInstance();

        return now.get(Calendar.YEAR) > lastReset.get(Calendar.YEAR) ||
               now.get(Calendar.DAY_OF_YEAR) > lastReset.get(Calendar.DAY_OF_YEAR);
    }

    public void resetAll() {
        prefs.edit()
             .putLong(KEY_ACCUMULATED_TIME, 0)
             .putLong(KEY_LAST_RESET_TIME, System.currentTimeMillis())
             .apply();
        sessionStartTime = 0;
        isTracking = false;
    }

    public void setTestMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_TEST_MODE, enabled).apply();
    }

    public boolean isTestMode() {
        return prefs.getBoolean(KEY_TEST_MODE, false);
    }

    public long getLimit() {
        return isTestMode() ? LIMIT_TEST_MODE : LIMIT_DEFAULT;
    }

    public void startOrResume() {
        checkDailyReset();
        if (!isTracking) {
            sessionStartTime = System.currentTimeMillis();
            isTracking = true;
        }
    }

    public void pauseAndReset() {
        if (isTracking) {
            long now = System.currentTimeMillis();
            long sessionDuration = now - sessionStartTime;
            
            long currentAccumulated = prefs.getLong(KEY_ACCUMULATED_TIME, 0);
            prefs.edit().putLong(KEY_ACCUMULATED_TIME, currentAccumulated + sessionDuration).apply();
            
            isTracking = false;
            sessionStartTime = 0;
        }
    }

    public long getAccumulatedTime() {
        long time = prefs.getLong(KEY_ACCUMULATED_TIME, 0);
        if (isTracking) {
            time += (System.currentTimeMillis() - sessionStartTime);
        }
        return time;
    }

    public boolean isLimitReached() {
        checkDailyReset();
        return getAccumulatedTime() >= getLimit();
    }
}
