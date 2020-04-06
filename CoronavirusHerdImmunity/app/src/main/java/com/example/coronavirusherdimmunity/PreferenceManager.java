package com.example.coronavirusherdimmunity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.coronavirusherdimmunity.enums.ApplicationStatus;
import com.example.coronavirusherdimmunity.enums.PatientStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;


public class PreferenceManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SharedPreferences pref_sdkvers;
    SharedPreferences.Editor editor_sdkvers;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file names
    private static final String PREF_NAME = "WelcomeActivity";
    private static final String PREF_NAME_SDK_VERS = "SdkVersion";  //used to save sdk versions

    /***** Key shared preferences "WelcomeActivity" *******/
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
    private static final String PASSWORD_DB = "password_db"; //password to open SQLcipher DB
    private static final String DISTANCE_FILTER = "distance_filter";
    private static final String WARNING_LEVEL = "warning_level";
    private static final String ALERT_LINK = "alert_link";
    private static final String ALERT_LANGUAGE = "alert_language";
    private static final String ALERT_FILTER_ID = "alert_filter_id";
    private static final String ALERT_CONTENT = "alert_content";


    /***** Key shared preferences "WelcomeActivity" *******/
    private static final String SDK_VERS = "sdk_vers"; //sdk android version

    private String TAG = "PreferenceManager";

    public PreferenceManager(Context context) {
        this._context = context;

        if(check_versions_update()) {  // if version is updated and is greater than 23

            backupFile(); //copy file from SharedPreferences to EncryptedSharedPreferences

        }else if (Build.VERSION.SDK_INT >= 23) { //if android api versions >= 23 and old version is not <23

            try {

                //create an encryption master key and store it in the Android KeyStore
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

                // Opens an instance of encrypted SharedPreferences
                pref = EncryptedSharedPreferences.create(
                        PREF_NAME,
                        masterKeyAlias,
                        _context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                // use the shared preferences and editor as you normally would
                editor = pref.edit();

            } catch (Exception e){

                Log.d(TAG, "Error to open Encrypted Shared Preferences");
            }

        }else{ //if android api versions: 20-21-22

            pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
            editor = pref.edit();
        }

        setSdkVers(Build.VERSION.SDK_INT);   //save new sdk version
    }


    /*******+ functions versions ***+*****/

    /**
     * check if version sdk (saved on file) is updated, less than 23 and current version is greater than 23
     * @return "true" if version sdk (saved on file) is updated, less than 23 and current version is greater or equal than 23,
     *          "false" otherwise
     */
    private Boolean check_versions_update (){
        pref_sdkvers = _context.getSharedPreferences(PREF_NAME_SDK_VERS,PRIVATE_MODE);
        editor_sdkvers = pref_sdkvers.edit();
        int old_sdkvers = getSdkVers();
        return (old_sdkvers >= 10 && old_sdkvers < 23 && Build.VERSION.SDK_INT >= 23);
    }

    /**
     *  copy Shared Preferences into Encrypted Shared Preferences in order to not loose shared data
     *  when sdk versions, less than 23 (<23), become greater or equal than 23 (>=23)
     */
    private void backupFile(){

        pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();

        /* get shared data saved into shared preferences */
        boolean is_first_time_launch         = isFirstTimeLaunch();
        long device_id                       = getDeviceId();
        String device_UIID                   = getDeviceUUID();
        long last_interaction_push_time      = getLastInteractionPushTime();
        long next_interaction_push_time      = getNextInteractionPushTime();
        PatientStatus patient_status         = getPatientStatus();
        ApplicationStatus application_status = getApplicationStatus();
        String auth_token                    = getAuthToken();
        boolean backend_location             = getBackendLocation();
        boolean user_location_permission     = getUserLocationPermission();
        String challenge                     = getChallenge();
        String password_db                   = getPasswordDB();
        double distanceFilter                = getDistanceFilter();
        String link                          = getAlertLink();
        String language                      = getAlertLanguage();
        int filterId                         = getAlertFilterId();
        int warningLevel                     = getWarningLevel();
        JSONObject alertContent              = getAlertContent();

        try {

            //create an encryption master key and store it in the Android KeyStore
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // Opens an instance of encrypted SharedPreferences
            pref = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    _context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // use the shared preferences and editor as you normally would
            editor = pref.edit();

            /* copy saved data into encrypted shared preferences */
            setFirstTimeLaunch(is_first_time_launch);
            setDeviceId(device_id);
            setDeviceUUID(device_UIID);
            setLastInteractionsPushTime(last_interaction_push_time);
            setNextInteractionsPushTime(next_interaction_push_time);
            setPatientStatus(patient_status);
            setApplicationStatus(application_status);
            setAuthToken(auth_token);
            setBackendLocation(backend_location);
            setUserLocationPermission(user_location_permission);
            setChallenge(challenge);
            setDistanceFilter(distanceFilter);
            setPasswordDB(password_db);
            setAlertFilterId(filterId);
            setAlertLink(link);
            setAlertLanguage(language);
            setWarningLevel(warningLevel);
            setAlertContent(alertContent);

        } catch (Exception e){

            Log.d(TAG, "Error to open Encrypted Shared Preferences during backup");
        }

    }



    /******** functions on Shared Preference "WelcomeActivity" *************/

    void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setDeviceId(long device_id){
        editor.putLong(DEVICE_ID, device_id);
        editor.commit();
    }

    public long getDeviceId(){
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

    public long getNextInteractionPushTime() { return pref.getLong(NEXT_INTERACTIONS_PUSH_TIME, -1);
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

    public void setDistanceFilter(double distanceFilter) {
        editor.putLong(DISTANCE_FILTER, Double.doubleToRawLongBits(distanceFilter));
        editor.commit();
    }

    public double getDistanceFilter() {
        return Double.longBitsToDouble(pref.getLong(DISTANCE_FILTER, Double.doubleToLongBits(-1)));
    }


    public void setPasswordDB(String passwordDB) {
        editor.putString(PASSWORD_DB, passwordDB);
        editor.commit();
    }

    public String getPasswordDB() {
        String password = pref.getString(PASSWORD_DB, null);

        if (password == null) {
            password = UUID.randomUUID().toString();; // generate random password
            setPasswordDB(password);                  // save password
        }
        return password;
    }

    public void setWarningLevel(int warningLevel) {
        editor.putInt(WARNING_LEVEL, warningLevel);
        editor.commit();
    }

    public int getWarningLevel() {
        return pref.getInt(WARNING_LEVEL, 0);
    }

    public void setAlertFilterId(int filterId) {
        editor.putInt(ALERT_FILTER_ID, filterId);
        editor.commit();
    }

    public int getAlertFilterId() {
        return pref.getInt(ALERT_FILTER_ID, 0);
    }

    public void setAlertLink(String link) {
        editor.putString(ALERT_LINK, link);
        editor.commit();
        setAlertContent(null);
    }

    public String getAlertLink() {
        return pref.getString(ALERT_LINK, null);
    }

    public void setAlertLanguage(String language) {
        editor.putString(ALERT_LANGUAGE, language);
        editor.commit();
    }

    public String getAlertLanguage() {
        return pref.getString(ALERT_LANGUAGE, null);
    }

    public void setAlertContent(JSONObject content) {
        editor.putString(ALERT_CONTENT, content == null ? null : content.toString());
        editor.commit();
    }

    public JSONObject getAlertContent() {
        String content = pref.getString(ALERT_CONTENT, null);
        if (content != null){
            try {
                return new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /******** functions on Shared Preference "SdkVersion" *************/

    private void setSdkVers(int sdkVers){
        editor_sdkvers.putInt(SDK_VERS, sdkVers);
        editor_sdkvers.commit();
    }

    private int getSdkVers(){
        return pref_sdkvers.getInt(SDK_VERS, -1);
    }

}