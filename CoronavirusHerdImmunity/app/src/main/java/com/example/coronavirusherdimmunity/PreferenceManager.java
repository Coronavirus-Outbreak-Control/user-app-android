package com.example.coronavirusherdimmunity;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "WelcomeActivity";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String DEVICE_ID = "device_id";
    private static final String LAST_INTERACTIONS_PUSH_TIME = "lastInteractionsPushTime";
    private static final String NEXT_INTERACTIONS_PUSH_TIME = "nextInteractionsPushTime";

    public PreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setDeviceId(int device_id){
        editor.putInt(DEVICE_ID, device_id);
        editor.commit();
    }

    public int getDeviceId(){
        return pref.getInt(DEVICE_ID, -1);
    }

    public void setLastInteractionsPushTime(long timestamp) {
        editor.putLong(LAST_INTERACTIONS_PUSH_TIME, timestamp);
        editor.commit();
    }

    public long getLastInteractionPushTime() {
        return pref.getLong(LAST_INTERACTIONS_PUSH_TIME, -1);
    }

    public void setNextInteractionsPushTime(long timestamp) {
        editor.putLong(NEXT_INTERACTIONS_PUSH_TIME, timestamp);
        editor.commit();
    }

    public long getNextInteractionPushTime() {
        return pref.getLong(NEXT_INTERACTIONS_PUSH_TIME, -1);
    }
}