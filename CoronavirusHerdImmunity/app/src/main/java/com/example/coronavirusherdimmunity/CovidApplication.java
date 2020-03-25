package com.example.coronavirusherdimmunity;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.coronavirusherdimmunity.enums.Distance;
import com.example.coronavirusherdimmunity.utils.ApiManager;
import com.example.coronavirusherdimmunity.utils.BeaconDto;
import com.example.coronavirusherdimmunity.utils.StorageManager;

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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

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
        int deviceId = new PreferenceManager(getApplicationContext()).getDeviceId();
        if (deviceId == -1) {
            Task.callInBackground(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {

                        JSONObject object = ApiManager.registerDevice(/*"06c9cf6c-ecfb-4807-afb4-4220d0614593"*/ UUID.randomUUID().toString());
                    if (object != null) {
                        if (object.has("token")){
                            new PreferenceManager(getApplicationContext()).setAuthToken(object.getString("token"));
                        }
                        return object.getInt("id");
                    } else {
                        return -1;
                    }
                }
            }).onSuccess(new Continuation<Integer, Object>() {
                @Override
                public Object then(Task<Integer> task) {
                    Log.e(TAG, "dev " + task.getResult());
                    if (task.getResult() != -1)
                        new PreferenceManager(getApplicationContext()).setDeviceId(task.getResult());
                        initBeacon(task.getResult());
                    return null;
                }
            });
        } else {
            initBeacon(deviceId);
        }
    }

    private void initBeacon(int deviceId){
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
        beaconTransmitter.startAdvertising(beacon);
        mHandler.postDelayed(resetTransmission, 5*60*1000); // 5 min

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        BeaconManager.setDebug(true);

        /**/
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(
                String.format(getString(R.string.permanent_notification),
                        new PreferenceManager(getApplicationContext()).getPatientStatus().toString(),
                        new StorageManager(getApplicationContext()).countInteractions()
                )
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
        beaconManager.setBackgroundBetweenScanPeriod(1000);
        beaconManager.setBackgroundScanPeriod(1100);
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
                    enableTrasmission();
                    Log.e(TAG, "Transmission restart");
                    mHandler.postDelayed(resetTransmission, 15*1000); // 15 sec
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
            // TODO REMOVE NOTIFICATION
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
                        int deviceId = 65536 * beacon.getId3().toInt() + beacon.getId2().toInt();

                        Distance distance = Distance.FAR;
                        if (beacon.getDistance() <= 0.4){
                            distance = Distance.IMMEDIATE;
                        }else if (beacon.getDistance() <= 2){
                            distance = Distance.NEAR;
                        }

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
                        BeaconDto beaconDto = new BeaconDto(deviceId, beacon.getRssi(), distance, x, y);
                        new StorageManager(getApplicationContext()).insertBeacon(beaconDto);
                    }
                }
                updateNotification();
                notifyUI();
                pushInteractions();
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
        builder.setContentTitle(
                String.format(getString(R.string.permanent_notification),
                        new PreferenceManager(getApplicationContext()).getPatientStatus().toString(),
                        new StorageManager(getApplicationContext()).countInteractions()
                )
                );
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("FOREGROUNDBEACON",
                    "Foreground beacon service", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Foreground beacon service");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            channel.setSound(null, null);


            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        notificationManager.notify(456, builder.build());
    }

    private void pushInteractions(){
        final List<BeaconDto> groups = new ArrayList<>();

        final Date now = new Date();
        long lastPushTime = new PreferenceManager(getApplicationContext()).getLastInteractionPushTime();
        long nextPushTime = new PreferenceManager(getApplicationContext()).getNextInteractionPushTime();
        Date lastPushDate = new Date(lastPushTime*1000);

        if (now.getTime()/1000 < nextPushTime ||
                (isPushingInteractions && pushStartTime + 2*60*1000 < now.getTime()))
            return;

        //boolean sendBackendLocation = new PreferenceManager(getApplicationContext()).getBackendLocation();
        boolean sendUserLocation = new PreferenceManager(getApplicationContext()).getUserLocationPermission();

        ArrayList<Integer> dist = new ArrayList<>();

        List<BeaconDto> beacons = new StorageManager(getApplicationContext()).readBeacons(lastPushDate);
        for (BeaconDto beacon: beacons) {
            if (groups.size()==0){
                groups.add(beacon);
                dist.clear();
                dist.add(beacon.distance.toInt());
            } else {
                BeaconDto lastGroup = groups.get(groups.size() -1);
                if (lastGroup.identifier == beacon.identifier && lastGroup.timestmp + 3*60 > beacon.timestmp){
                    groups.remove(lastGroup);

                    dist.add(beacon.distance.toInt());

                    Collections.sort(dist);
                    lastGroup.distance = Distance.valueOf(dist.get(dist.size()/2));
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
                    dist.add(beacon.distance.toInt());
                }
            }
        }

        if (groups.size() > 0) {
            isPushingInteractions = true;
            pushStartTime = now.getTime();
            Task.callInBackground(new Callable<JSONObject>() {
                @Override
                public JSONObject call() throws Exception {
                    return ApiManager.pushInteractions(getApplicationContext(), groups, new PreferenceManager(getApplicationContext()).getAuthToken());
                }
            }).onSuccess(new Continuation<JSONObject, Object>() {
                @Override
                public Object then(Task<JSONObject> task) throws Exception {
                    isPushingInteractions = false;
                    JSONObject result = task.getResult();
                    if (result != null) {
                        new PreferenceManager(getApplicationContext()).setLastInteractionsPushTime(now.getTime() / 1000);
                        new PreferenceManager(getApplicationContext()).setNextInteractionsPushTime(now.getTime() / 1000 + result.getInt("next_try"));
                        new PreferenceManager(getApplicationContext()).setBackendLocation(result.getBoolean("location"));
                    }
                    return null;
                }
            });
        }
    }

}
