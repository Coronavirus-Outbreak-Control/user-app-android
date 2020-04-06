package com.example.coronavirusherdimmunity;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.coronavirusherdimmunity.enums.ApplicationStatus;
import com.example.coronavirusherdimmunity.enums.Distance;
import com.example.coronavirusherdimmunity.enums.PatientStatus;
import com.example.coronavirusherdimmunity.utils.ApiManager;
import com.example.coronavirusherdimmunity.utils.BeaconDto;
import com.example.coronavirusherdimmunity.utils.PermissionRequest;
import com.example.coronavirusherdimmunity.utils.StorageManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import bolts.Continuation;
import bolts.Task;

public class CovidApplication extends Application implements BootstrapNotifier, BeaconConsumer {

    private static final String BEACON_ID = "451720ea-5e62-11ea-bc55-0242ac130003";

    private static final String TAG = "CovidApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MonitoringActivity monitoringActivity = null;
    private String cumulativeLog = "";

    private Beacon beacon;
    private BeaconParser beaconParser;

    private BeaconManager beaconManager;
    private BeaconTransmitter beaconTransmitter;

    private boolean isPushingInteractions = false;
    private long pushStartTime = -1;

    private int lastCount = 0;
    private PatientStatus lastStatus = PatientStatus.NORMAL;      // Patient Status (NORMAL, INFECTED, QUARANTINE, HEALED, SUSPECT)
    private ApplicationStatus lastAppStatus = ApplicationStatus.ACTIVE; // App Status (ACTIVE if permissions are granted, INACTIVE if at least a permission is not granted)

    private static CovidApplication instance;

    public static CovidApplication getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
    }

    public void onCreate() {
        instance = this;
        super.onCreate();

        lastCount = new StorageManager(getApplicationContext()).countInteractions();
        lastStatus = new PreferenceManager(getApplicationContext()).getPatientStatus();
        lastAppStatus = new PreferenceManager(getApplicationContext()).getApplicationStatus();

        long deviceId = new PreferenceManager(getApplicationContext()).getDeviceId();
        if (deviceId != -1) {
            initBeacon(deviceId);
        }
    }


    public void initBeacon(long deviceId){
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        beacon = new Beacon.Builder()
                .setId1(BEACON_ID)
                .setId2(String.valueOf(deviceId % 65536)) // major
                .setId3(String.valueOf(deviceId / 65536)) // minor
                .setManufacturer(0x004c)
                .setTxPower(-59)
                .setDataFields(new ArrayList<Long>())
                .build();

        beaconParser = new BeaconParser()
//                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");

        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        beaconTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        beaconTransmitter.startAdvertising(beacon);
        mHandler.postDelayed(resetTransmission, 15 * 1000); // 15 sec

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        BeaconManager.setDebug(BuildConfig.DEBUG);

        /**/
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(
                String.format(getString(R.string.permanent_notification), lastAppStatus.toString(), lastStatus.toString()) /*, lastCount*/
        );
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("FOREGROUNDBEACON",
                    "Foreground beacon service", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Foreground beacon service");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            channel.setSound(null, null);

            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(500);
        beaconManager.setBackgroundScanPeriod(600);
        /**/

        // wake up the app when a beacon is seen
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();

        beaconManager.bind(this);
    }

    private Handler mHandler = new Handler();
    private Runnable resetTransmission = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "Transmission stop");
            disableTrasmission();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PermissionRequest permissions = new PermissionRequest(getApplicationContext());
                    if (permissions.checkPermissions(false) == true) {  //if bluetooth and location are granted -> enable transmission

                        if (lastAppStatus.toInt() == 1){  //1: Inactive. Used to update just one time permanent notification when the permission is granted

                            new PreferenceManager(getApplicationContext()).setApplicationStatus(0);
                            lastAppStatus = new PreferenceManager(getApplicationContext()).getApplicationStatus();
                            updateNotification();
                        }

                        enableTrasmission();
                        Log.e(TAG, "Transmission restart");

                    } else{  //if bluetooth or location is not granted -> send a notification in order to alert the User

                        PreferenceManager pm = new PreferenceManager(getApplicationContext());
                        if (lastAppStatus.toInt() == 0 &&
                            !pm.isFirstTimeLaunch()){  //0: Active and is not first time launch -> Used to send just one notification when the permission are not granted

                            new PreferenceManager(getApplicationContext()).setApplicationStatus(1);
                            lastAppStatus = new PreferenceManager(getApplicationContext()).getApplicationStatus();
                            updateNotification();  //update permanent notification

                            String title = getString(R.string.notification_appstatus_title);
                            String msg = getString(R.string.notification_appstatus_msg);

                            FCMService.sendNotification(getApplicationContext(), title, msg); //send notification to alert user about app status change

                        }
                    }

                    mHandler.postDelayed(resetTransmission, 15 * 1000); // 15 sec
                }
            }, 5*1000); // 5 sec
        }
    };

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }
    public void enableMonitoring() {
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    public void disableTrasmission() {
        if (beaconTransmitter != null) {
            beaconTransmitter.stopAdvertising();
            beaconTransmitter = null;
        }
    }

    public void enableTrasmission() {
        if (beaconTransmitter == null) {
            beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
            beaconTransmitter.startAdvertising(beacon);
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "FXX did enter region.");


        if (monitoringActivity != null) {
            // If the Monitoring Activity is visible, we log info about the beacons we have
            // seen on its display
            Log.d(TAG, "FXX I see a beacon again");
            logToDisplay("I see a beacon again" );
        } else {
            // If the monitoring activity is not in the foreground, we send a notification to the user.
            Log.d(TAG, "FXXSending notification.");
            if (BuildConfig.DEBUG) {
                sendNotification();
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        logToDisplay("I no longer see a beacon.");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
    }

    private void sendNotification() {
/*        Notification.Builder builder =
                new Notification.Builder(this)
                        .setContentTitle("Beacon Reference Application")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.mipmap.ic_launcher);*/
/*

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("BEACONNOTIFICATION", "BEACON NOTIFICATION", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Beacon notification");
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        notificationManager.notify(1, builder.build());
*/

    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }

    private void logToDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.monitoringActivity != null) {
            this.monitoringActivity.updateLog(cumulativeLog);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                for (Beacon beacon : beacons) {
                    if (beacon.getId1().toString().equals(BEACON_ID)) {

                        // id2 major - id3 minor
                        long deviceId = Long.valueOf(65536 * beacon.getId3().toInt() + beacon.getId2().toInt());

                        Distance distance = Distance.FAR;
                        if (beacon.getDistance() <= 0.4){
                            distance = Distance.IMMEDIATE;
                        }else if (beacon.getDistance() <= 2){
                            distance = Distance.NEAR;
                        }

                        Double distanceFilter = new PreferenceManager(getApplicationContext()).getDistanceFilter();
                        if (distanceFilter < 0 || beacon.getDistance() <= distanceFilter) {
                            //boolean sendBackendLocation = new PreferenceManager(getApplicationContext()).getBackendLocation();
                            boolean sendUserLocation = new PreferenceManager(getApplicationContext()).getUserLocationPermission();
                            double x = 0;
                            double y = 0;

                            //if (sendBackendLocation && sendUserLocation) {
                            if (sendUserLocation) {
                                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (locationManager != null &&
                                        (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                                    Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                                    x = (location == null ? 0 : location.getLatitude());
                                    y = (location == null ? 0 : location.getLongitude());
                                }
                            }
                            BeaconDto beaconDto = new BeaconDto(deviceId, beacon.getRssi(), distance, beacon.getDistance(), x, y);
                            new StorageManager(getApplicationContext()).insertBeacon(beaconDto);
                        }
                    }
                }


                Task.callInBackground(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        int countInteractions = new StorageManager(getApplicationContext()).countInteractions();
                        if (lastCount != countInteractions || lastStatus != new PreferenceManager(getApplicationContext()).getPatientStatus()) {
                            lastCount = countInteractions;
                            lastStatus = new PreferenceManager(getApplicationContext()).getPatientStatus();
                            updateNotification();
                            notifyUI();
                        }
                        pushInteractions();
                        return null;
                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private void notifyUI(){
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("STATUS_UPDATE"));
    }

    private void updateNotification(){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setContentTitle(
                String.format(getString(R.string.permanent_notification), lastAppStatus, lastStatus.toString()) /*, lastCount)*/
        );

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("FOREGROUNDBEACON",
//                    "Foreground beacon service", NotificationManager.IMPORTANCE_MIN);
//            channel.setDescription("Foreground beacon service");
//            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
//            channel.enableLights(false);
//            channel.enableVibration(false);
//            channel.setShowBadge(false);
//            channel.setSound(null, null);
//
//
//            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("FOREGROUNDBEACON");
        }
        notificationManager.notify(456, builder.build());
    }

    private synchronized void pushInteractions(){
        final List<BeaconDto> groups = new ArrayList<>();

        final Date now = new Date();
        long lastPushTime = new PreferenceManager(getApplicationContext()).getLastInteractionPushTime();
        long nextPushTime = new PreferenceManager(getApplicationContext()).getNextInteractionPushTime();
        Date lastPushDate = new Date(lastPushTime*1000);

        Log.i("PUSH new isPushing", isPushingInteractions? "TRUE" : "FALSE");
        Log.i("PUSH new startTime", ""+pushStartTime);
        Log.i("PUSH new nowtime  ", ""+now.getTime());
        Log.i("PUSH new nextTime ", ""+nextPushTime);
        Log.i("PUSH new", "---------------------------------");
        Log.i("PUSH new time", now.getTime()/1000 < nextPushTime? "TRUE" : "FALSE");
        Log.i("PUSH new pushing",  (isPushingInteractions && pushStartTime + 2*60*1000 > now.getTime())? "TRUE" : "FALSE");
        Log.i("PUSH new", "---------------------------------");

        if (now.getTime()/1000 < nextPushTime ||
                (isPushingInteractions && pushStartTime + 2*60*1000 > now.getTime()))
            return;

        isPushingInteractions = true;
        pushStartTime = now.getTime();
        Log.i("PUSH start isPushing", isPushingInteractions? "TRUE" : "FALSE");
        Log.i("PUSH start startTime", ""+pushStartTime);

        //boolean sendBackendLocation = new PreferenceManager(getApplicationContext()).getBackendLocation();
        boolean sendUserLocation = new PreferenceManager(getApplicationContext()).getUserLocationPermission();

        ArrayList<Integer> dist = new ArrayList<>();
        ArrayList<Integer> rssi = new ArrayList<>();
        ArrayList<Double> distVal = new ArrayList<>();

        List<BeaconDto> beacons = new StorageManager(getApplicationContext()).readBeacons(lastPushDate);
        for (BeaconDto beacon: beacons) {
            if (groups.size()==0){
                groups.add(beacon);
                dist.clear();
                distVal.clear();
                rssi.clear();
                dist.add(beacon.distance.toInt());
                distVal.add(beacon.distanceValue);
                rssi.add(beacon.rssi);
            } else {
                BeaconDto lastGroup = groups.get(groups.size() -1);
                if (lastGroup.identifier == beacon.identifier && lastGroup.timestmp + 3*60 > beacon.timestmp){
                    groups.remove(lastGroup);

                    dist.add(beacon.distance.toInt());
                    distVal.add(beacon.distanceValue);
                    rssi.add(beacon.rssi);

                    Collections.sort(dist);
                    lastGroup.distance = Distance.valueOf(dist.get(dist.size()/2));

                    Collections.sort(distVal);
                    lastGroup.distanceValue = distVal.get(distVal.size()/2);

                    Collections.sort(rssi);
                    lastGroup.rssi = rssi.get(rssi.size()/2);

                    lastGroup.interval = (int) Math.abs(lastGroup.timestmp - beacon.timestmp)+10;

                    //if (sendBackendLocation && sendUserLocation) {
                    if (sendUserLocation) {
                        lastGroup.x = beacon.x;
                        lastGroup.y = beacon.y;
                    } else {
                        lastGroup.x = 0;
                        lastGroup.y = 0;
                    }
                    groups.add(lastGroup);
                } else {
                    groups.add(beacon);
                    dist.clear();
                    distVal.clear();
                    rssi.clear();
                    dist.add(beacon.distance.toInt());
                    distVal.add(beacon.distanceValue);
                    rssi.add(beacon.rssi);
                }
            }
        }

        final List<BeaconDto> reducedGroups = new ArrayList<>();
        for (BeaconDto b : groups) {
            if (reducedGroups.size()==0){
                reducedGroups.add(b);
            } else {
                BeaconDto lastGroup = groups.get(groups.size() -1);
                if (lastGroup.distance == b.distance && b.timestmp < lastGroup.timestmp + lastGroup.interval + 30*1000 ){
                    reducedGroups.remove(lastGroup);

                    lastGroup.rssi = (lastGroup.rssi + b.rssi)/2;
                    lastGroup.interval = lastGroup.interval + b.interval;
                    lastGroup.distanceValue = (lastGroup.distanceValue + b.distanceValue) /2;

                    reducedGroups.add(lastGroup);
                } else {
                    reducedGroups.add(b);
                }
            }
        }


        if (reducedGroups.size() > 0) {
            Task.callInBackground(new Callable<JSONObject>() {
                @Override
                public JSONObject call() throws Exception {
                    return ApiManager.pushInteractions(getApplicationContext(), reducedGroups, new PreferenceManager(getApplicationContext()).getAuthToken());
                }
            }).onSuccess(new Continuation<JSONObject, Object>() {
                @Override
                public Object then(Task<JSONObject> task) throws Exception {
                    JSONObject result = task.getResult();
                    if (result != null) {
                        new PreferenceManager(getApplicationContext()).setLastInteractionsPushTime(now.getTime() / 1000);
                        Log.i("PUSH last time", "" + now.getTime() / 1000);
                        if (result.has("next_try")) {
                            int next = result.getInt("next_try");
                            Random r = new Random();
                            double rangeMin = -0.25;
                            double rangeMax = 0.25;
                            double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                            next = next + (int)(randomValue * next);
                            new PreferenceManager(getApplicationContext()).setNextInteractionsPushTime(now.getTime() / 1000 + next);
                        }
                        if (result.has("location")) {
                            new PreferenceManager(getApplicationContext()).setBackendLocation(result.getBoolean("location"));
                        }
                        if (result.has("distance_filter")) {
                            new PreferenceManager(getApplicationContext()).setDistanceFilter(result.getDouble("distance_filter"));
                        } else {
                            new PreferenceManager(getApplicationContext()).setDistanceFilter(-1);
                        }
                    }
                    isPushingInteractions = false;
                    pushStartTime = -1;
                    Log.i("PUSH end isPushing", isPushingInteractions? "TRUE" : "FALSE");
                    Log.i("PUSH end result", task.getResult().toString());
                    return null;
                }
            });
        }
    }

}
