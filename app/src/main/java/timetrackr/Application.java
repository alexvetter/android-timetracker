package timetrackr;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.danlew.android.joda.JodaTimeAndroid;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import timetrackr.activity.PeriodDetailActivity;
import timetrackr.database.BeaconDatabaseHandler;
import timetrackr.database.PeriodDatabaseHandler;
import timetrackr.database.SQLiteHelper;
import timetrackr.model.BeaconModel;
import timetrackr.model.PeriodModel;
import timetrackr.utils.DateTimeFormats;
import timetrackr.utils.TargetHours;

public class Application extends android.app.Application implements BootstrapNotifier, DateTimeFormats {

    private static final String ACTION_YES = "ACTION_YES";

    private static final String EXTRA_PERIOD = "PERIOD";
    private static final String EXTRA_STOPDATETIME = "STOPDATETIME";

    private BeaconManager beaconManager;

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private BeaconDatabaseHandler beaconDatabaseHandler;
    private PeriodDatabaseHandler periodDatabaseHandler;

    private List<Region> regions;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        this.beaconDatabaseHandler = new BeaconDatabaseHandler();
        this.periodDatabaseHandler = new PeriodDatabaseHandler();

        SQLiteHelper.setInstance(this);

        enabledBackgroundScan();
    }

    protected Region newRegionFromBeacon(BeaconModel model) {
        return new Region(model.getName() + "-" + model.getUuid(), Identifier.parse(model.getUuid()), null, null);
    }

    protected void enabledBackgroundScan() {
        beaconManager = BeaconManager.getInstanceForApplication(this);

        /*
        m: Manufacturer Data,
        i: Proximity UUID,
        i: Major Number,
        i: Minor Number,
        p: Signal Power,
        d: Battery Level
        */

        // jaalee, Estimote, Beacon inside
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        // AltBeacon
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        // iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        regions = new ArrayList<>();
        for (BeaconModel model : beaconDatabaseHandler.getAll()) {
            if (model.getEnabled()) {
                regions.add(newRegionFromBeacon(model));
            }
        }

        // wake up the app when a beacon is seen
        regionBootstrap = new RegionBootstrap(this, regions);

        // this reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    public void didEnterRegion(Region region) {
        System.out.println("Did enter region. " + region.getId1().toUuidString());

        BeaconModel model = beaconDatabaseHandler.get(region.getId1().toUuidString());
        if (!model.getEnabled()) {
            regions.remove(region);
            return;
        }

        PeriodModel current = periodDatabaseHandler.getCurrentPeriod();
        if (current == null) {
            DateTime now = DateTime.now();

            TargetHours targetHours = new TargetHours(this);

            current = new PeriodModel();

            current.setName(model.getName());
            current.setRemark("Automatic entry");
            current.setStartTime(now);
            current.setEndTime(now.plus(targetHours.getDuration(now)));

            periodDatabaseHandler.add(current);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        PeriodModel current = periodDatabaseHandler.getCurrentPeriod();

        if (current != null) {
            sendStopNotification(current, DateTime.now());
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // ignore
    }

    private void sendStopNotification(PeriodModel model, DateTime stopDateTime) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.question_stop_current_working))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);

        Intent stopCurrentPeriod = new Intent(this, StopCurrentPeriodReceiver.class);
        stopCurrentPeriod.setAction(ACTION_YES);

        stopCurrentPeriod.putExtra(EXTRA_PERIOD, model.getId().intValue());
        stopCurrentPeriod.putExtra(EXTRA_STOPDATETIME, stopDateTime.toString(dateTimeFormatter));

        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, stopCurrentPeriod, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_timer_off_black_24dp, getString(R.string.action_stop), pendingIntentYes);

        Intent intent = new Intent(this, PeriodDetailActivity.class);
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_ID, model.getId().intValue());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(model.getId(), builder.build());
    }

    public static class StopCurrentPeriodReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_YES.equals(action)) {
                final PeriodDatabaseHandler periodDatabaseHandler = new PeriodDatabaseHandler();

                int periodId = intent.getIntExtra(EXTRA_PERIOD, -1);
                DateTime endDateTime = dateTimeFormatter.parseDateTime(intent.getStringExtra(EXTRA_STOPDATETIME));

                PeriodModel model = periodDatabaseHandler.get(periodId);
                if (model != null) {
                    model.setEndTime(endDateTime);
                    periodDatabaseHandler.update(model);
                }

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(periodId);
            }
        }
    }
}