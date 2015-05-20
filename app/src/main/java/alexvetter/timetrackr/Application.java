package alexvetter.timetrackr;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;

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

import alexvetter.timetrackr.activity.PeriodDetailActivity;
import alexvetter.timetrackr.database.AbstractDatabaseHandler;
import alexvetter.timetrackr.database.BeaconDatabaseHandler;
import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.database.SQLiteHelper;
import alexvetter.timetrackr.model.BeaconModel;
import alexvetter.timetrackr.model.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.PeriodCalculator;
import alexvetter.timetrackr.utils.TargetHours;

public class Application extends android.app.Application implements BootstrapNotifier, DateTimeFormats, AbstractDatabaseHandler.DatabaseHandlerListener {

    private static final String ACTION_YES = "ACTION_YES";

    private static final String EXTRA_PERIOD = "PERIOD";
    private static final String EXTRA_STOPDATETIME = "STOPDATETIME";

    private BeaconManager beaconManager;

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private BeaconDatabaseHandler beaconDatabaseHandler;
    private PeriodDatabaseHandler periodDatabaseHandler;

    private final List<Region> regions = new ArrayList<>();

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

        BeaconModel model = beaconDatabaseHandler.get(region.getId1().toUuidString());
        if (model == null || !model.getEnabled()) {
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

        BeaconModel model = beaconDatabaseHandler.get(region.getId1().toUuidString());
        if (model == null || !model.getEnabled()) {
            return;
        }

        if (current != null) {
            sendStopNotification(current, DateTime.now());
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // ignore
    }

    private void openStopDialog(final PeriodModel model, final DateTime stopDateTime) {
        String period = PeriodCalculator.getPeriodShort(model.getStartTime(), stopDateTime);
        String endTime = stopDateTime.toString(DateTimeFormats.timeFormatter);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(getString(R.string.question_stop_current_working, period));

        alert.setPositiveButton(getString(R.string.action_stop, endTime), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent stopCurrentPeriod = new Intent(Application.this, StopCurrentPeriodReceiver.class);
                stopCurrentPeriod.setAction(ACTION_YES);

                stopCurrentPeriod.putExtra(EXTRA_PERIOD, model.getId().intValue());
                stopCurrentPeriod.putExtra(EXTRA_STOPDATETIME, stopDateTime.toString(dateTimeFormatter));

                Application.this.sendBroadcast(stopCurrentPeriod);
            }
        });

        alert.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    /**
     * Builds and send notification to stop working period.
     */
    private void sendStopNotification(PeriodModel model, DateTime stopDateTime) {
        String period = PeriodCalculator.getPeriodShort(model.getStartTime(), stopDateTime);
        String endTime = stopDateTime.toString(DateTimeFormats.timeFormatter);

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
        stopCurrentPeriod.putExtra(EXTRA_STOPDATETIME, stopDateTime.toString(dateTimeFormatter));

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
        for (BeaconModel model : beaconDatabaseHandler.getAll()) {
            regions.add(newRegionFromBeacon(model));
        }
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