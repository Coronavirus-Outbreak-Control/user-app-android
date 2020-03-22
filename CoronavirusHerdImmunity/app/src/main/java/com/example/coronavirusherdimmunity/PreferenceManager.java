package com.example.coronavirusherdimmunity;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.coronavirusherdimmunity.enums.PatientStatus;


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
    private static final String PATIENT_STATUS = "patientStatus";
    private static final String AUTH_TOKEN = "authToken";
    private static final String BACKEND_LOCATION = "backendLocation";
    private static final String USER_LOCATION_PERMISSION = "userLocationPermission";

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

    // {0: normal, 1: infected, 2: quarantine, 3: healed, 4: suspect}
    public void setPatientStatus(PatientStatus status) {
        setPatientStatus(status.toInt());
    }

    public void setPatientStatus(int status) {
        editor.putInt(PATIENT_STATUS, status);
        editor.commit();
    }

    public PatientStatus getPatientStatus() {
        return PatientStatus.valueOf(pref.getInt(PATIENT_STATUS, 0));
    }

    public void setAuthToken(String token) {
        editor.putString(AUTH_TOKEN, token);
        editor.commit();
    }

    public String getAuthToken() {
        return pref.getString(AUTH_TOKEN, null);
    }

    public void setBackendLocation(boolean backendLocation) {
        editor.putBoolean(BACKEND_LOCATION, backendLocation);
        editor.commit();
    }

    public boolean getBackendLocation() {
        return pref.getBoolean(BACKEND_LOCATION, false);
    }


    public void setUserLocationPermission(boolean userLocationPermission) {
        editor.putBoolean(USER_LOCATION_PERMISSION, userLocationPermission);
        editor.commit();
    }

    public boolean getUserLocationPermission() {
        return pref.getBoolean(USER_LOCATION_PERMISSION, false);
    }
}