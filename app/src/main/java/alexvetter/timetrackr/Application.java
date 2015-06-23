package alexvetter.timetrackr;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import alexvetter.timetrackr.activity.PeriodDetailActivity;
import alexvetter.timetrackr.controller.PeriodController;
import alexvetter.timetrackr.database.BeaconDatabaseHandler;
import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.database.SQLiteHelper;
import alexvetter.timetrackr.domain.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.PeriodUtils;

public class Application extends android.app.Application implements BeaconBackgroundScanner.ScannerListener {

    private static final String ACTION_YES = "ACTION_YES";

    private static final String EXTRA_PERIOD = "PERIOD";
    private static final String EXTRA_STOPDATETIME = "STOPDATETIME";

    private BeaconBackgroundScanner scanner;

    @Override
    public void onCreate() {
        super.onCreate();

        // This needs to be the first thing which
        // the application does because it heavily
        // depends on Joda Time and it needs to be
        // initialised properly.
        JodaTimeAndroid.init(this);

        // Next we need to initialize the
        // all table (handlers) we will
        // make use of later
        initializeDatabase();

        // Last but not least we can
        // enable bluetooth le
        // background scanning
        enabledBackgroundScan();
    }

    protected void initializeDatabase() {
        BeaconDatabaseHandler.registerTable();
        PeriodDatabaseHandler.registerTable();

        SQLiteHelper.setInstance(this);
    }

    protected void enabledBackgroundScan() {
        scanner = new BeaconBackgroundScanner(getApplicationContext(), this);
    }

    /**
     * Builds and send notification to stop working period.
     */
    public void sendStopNotification(PeriodModel model, DateTime stopDateTime) {
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