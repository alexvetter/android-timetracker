package alexvetter.timetrackr;

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
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import alexvetter.timetrackr.activity.PeriodDetailActivity;
import alexvetter.timetrackr.controller.BeaconController;
import alexvetter.timetrackr.controller.PeriodController;
import alexvetter.timetrackr.database.AbstractDatabaseHandler;
import alexvetter.timetrackr.database.SQLiteHelper;
import alexvetter.timetrackr.domain.BeaconModel;
import alexvetter.timetrackr.domain.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.PeriodUtils;

public class Application extends android.app.Application implements BootstrapNotifier, AbstractDatabaseHandler.DatabaseHandlerListener {

    private static final String ACTION_YES = "ACTION_YES";

    private static final String EXTRA_PERIOD = "PERIOD";
    private static final String EXTRA_STOPDATETIME = "STOPDATETIME";

    private BeaconManager beaconManager;

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private final List<Region> regions = new ArrayList<>();

    private BeaconController beaconController;
    private PeriodController periodController;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        this.beaconController = new BeaconController();
        this.periodController = new PeriodController(this);

        SQLiteHelper.setInstance(this);

        enabledBackgroundScan();
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

        // add registered beacons as regions
        notifyDataSetChanged();

        // wake up the app when a beacon is seen
        regionBootstrap = new RegionBootstrap(this, regions);

        // this reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    public void didEnterRegion(Region region) {
        System.out.println("Did enter region. " + region.getId1().toUuidString());

        BeaconModel beacon = beaconController.getBeacon(region);
        if (beacon == null || !beacon.getEnabled()) {
            return;
        }

        periodController.onAutomaticEntry(beacon);
    }

    @Override
    public void didExitRegion(Region region) {
        PeriodModel period = periodController.getCurrentPeriod();
        if (beaconController.isEnabled(region) && period != null) {
            sendStopNotification(period, DateTime.now());
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // ignore
    }

    /**
     * Builds and send notification to stop working period.
     */
    private void sendStopNotification(PeriodModel model, DateTime stopDateTime) {
        String period = PeriodUtils.getPeriodShort(model.getStartTime(), stopDateTime);
        String endTime = stopDateTime.toString(DateTimeFormats.TIME);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.question_stop_current_working, period))
                        .setSmallIcon(R.drawable.ic_notify)
                        .setAutoCancel(true);

        // Stop button
        Intent stopCurrentPeriod = new Intent(this, StopCurrentPeriodReceiver.class);
        stopCurrentPeriod.setAction(ACTION_YES);

        stopCurrentPeriod.putExtra(EXTRA_PERIOD, model.getId().intValue());
        stopCurrentPeriod.putExtra(EXTRA_STOPDATETIME, stopDateTime.toString(DateTimeFormats.DATE_TIME));

        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, stopCurrentPeriod, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_timer_off_black_24dp, getString(R.string.action_stop, endTime), pendingIntentYes);

        // Click notification -> Open period details
        Intent intent = new Intent(this, PeriodDetailActivity.class);
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_ID, model.getId().intValue());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(model.getId(), builder.build());
    }

    /**
     * Comes with the DatabaseHandlerListener interface.
     * Will be called if the BeaconDatabaseHandler makes
     * changes to the associated table.
     */
    @Override
    public void notifyDataSetChanged() {
        regions.clear();
        regions.addAll(beaconController.getBeaconsAsRegions());
    }

    public static class StopCurrentPeriodReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_YES.equals(action)) {
                int periodId = intent.getIntExtra(EXTRA_PERIOD, -1);
                DateTime endDateTime = DateTimeFormats.DATE_TIME.parseDateTime(intent.getStringExtra(EXTRA_STOPDATETIME));

                PeriodController controller = new PeriodController(context);
                controller.onStopPeriod(periodId, endDateTime);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(periodId);
            }
        }
    }
}