package com.example.coronavirusherdimmunity;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.coronavirusherdimmunity.enums.ApplicationStatus;
import com.example.coronavirusherdimmunity.enums.PatientStatus;

import java.util.UUID;


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
    private static final String DEVICE_UUID = "device_uuid";
    private static final String LAST_INTERACTIONS_PUSH_TIME = "lastInteractionsPushTime";
    private static final String NEXT_INTERACTIONS_PUSH_TIME = "nextInteractionsPushTime";
    private static final String PATIENT_STATUS = "patientStatus";
    private static final String APPLICATION_STATUS = "applicationStatus";
    private static final String AUTH_TOKEN = "authToken";
    private static final String BACKEND_LOCATION = "backendLocation";
    private static final String USER_LOCATION_PERMISSION = "userLocationPermission";
    private static final String CHALLENGE = "challenge"; //google challenge (token received by reCaptcha)
    private static final String EXCLUDE_FAR = "exclude_far";

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

    public void setDeviceId(Long device_id){
        editor.putLong(DEVICE_ID, device_id);
        editor.commit();
    }

    public Long getDeviceId(){
        return pref.getLong(DEVICE_ID, -1);
    }


    public void setDeviceUUID(String device_uuid) {
        editor.putString(DEVICE_UUID, device_uuid);
        editor.commit();
    }

    public String getDeviceUUID() {
        String uiid = pref.getString(DEVICE_UUID, null);

        if (uiid == null) {
            uiid = UUID.randomUUID().toString();
            setDeviceUUID(uiid);
        }
        return uiid;
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

    // {0: active, 1: inactive}
    public void setApplicationStatus(ApplicationStatus status) {
        setApplicationStatus(status.toInt());
    }

    public void setApplicationStatus(int status) {
        editor.putInt(APPLICATION_STATUS, status);
        editor.commit();
    }

    public ApplicationStatus getApplicationStatus() {
        return ApplicationStatus.valueOf(pref.getInt(APPLICATION_STATUS, 0));
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

    public void setChallenge(String challenge) {
        editor.putString(CHALLENGE, challenge);
        editor.commit();
    }

    public String getChallenge(){
        return pref.getString(CHALLENGE, null);
    }

    public void setExcludeFar(boolean excludeFar) {
        editor.putBoolean(EXCLUDE_FAR, excludeFar);
        editor.commit();
    }

    public boolean getExcludeFar() {
        return pref.getBoolean(EXCLUDE_FAR, false);
    }
}