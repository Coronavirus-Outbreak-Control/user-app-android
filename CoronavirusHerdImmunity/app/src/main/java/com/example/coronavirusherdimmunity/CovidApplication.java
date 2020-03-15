package com.example.coronavirusherdimmunity;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.coronavirusherdimmunity.utils.ApiManager;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class CovidApplication extends Application implements BootstrapNotifier {

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

    public void onCreate() {
        super.onCreate();
        int deviceId = new PreferenceManager(getApplicationContext()).getDeviceId();
        if (deviceId == -1) {
            Task.callInBackground(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {

                        JSONObject object = ApiManager.registerDevice(/*"06c9cf6c-ecfb-4807-afb4-4220d0614593"*/ UUID.randomUUID().toString());
                        if (object != null) {
                            return object.getInt("id");
                        } else {
                            return -1;
                        }
                }
            }).onSuccess(new Continuation<Integer, Object>() {
                @Override
                public Object then(Task<Integer> task) throws Exception {
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
                .setId2(String.valueOf(deviceId / 65536)) // minor
                .setId3(String.valueOf(deviceId % 65536)) // major
                .setManufacturer(0x004c)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l}))
                .build();

        beaconParser = new BeaconParser()
//                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");

        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon);

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(true);

        /**/
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("FOREGROUNDBEACON",
                    "Foreground beacon service", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
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
    }

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

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "FXX did enter region.");
        // TODO: STORE BEACON INTERACTION


        if (monitoringActivity != null) {
            // If the Monitoring Activity is visible, we log info about the beacons we have
            // seen on its display
            Log.d(TAG, "FXX I see a beacon again");
            logToDisplay("I see a beacon again" );
        } else {
            // If the monitoring activity is not in the foreground, we send a notification to the user.
            // TODO REMOVE NOTIFICATION
            Log.d(TAG, "FXXSending notification.");
            sendNotification();
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
        Notification.Builder builder =
                new Notification.Builder(this)
                        .setContentTitle("Beacon Reference Application")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.mipmap.ic_launcher);

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

}
