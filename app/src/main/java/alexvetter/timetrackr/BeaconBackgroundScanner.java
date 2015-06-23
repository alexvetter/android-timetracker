package alexvetter.timetrackr;


import android.content.Context;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import alexvetter.timetrackr.controller.BeaconController;
import alexvetter.timetrackr.controller.PeriodController;
import alexvetter.timetrackr.database.AbstractDatabaseHandler;
import alexvetter.timetrackr.domain.BeaconModel;
import alexvetter.timetrackr.domain.PeriodModel;

public class BeaconBackgroundScanner implements BootstrapNotifier, AbstractDatabaseHandler.DatabaseHandlerListener {

    private final ScannerListener listener;
    private final Context applicationContext;

    private BeaconManager beaconManager;

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private BeaconController beaconController;
    private PeriodController periodController;

    private final List<Region> regions = new ArrayList<>();

    public BeaconBackgroundScanner(Context context, ScannerListener scannerListener) {
        applicationContext = context;

        listener = scannerListener;

        beaconController = new BeaconController();
        periodController = new PeriodController(context);

        // add registered beacons as regions
        notifyDataSetChanged();

        beaconManager = BeaconManager.getInstanceForApplication(context);

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

        // wake up the app when a beacon is seen
        regionBootstrap = new RegionBootstrap(this, regions);

        // this reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(context);
    }

    @Override
    public Context getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void didEnterRegion(Region region) {
        BeaconModel beacon = beaconController.getBeacon(region);
        if (beacon == null || !beacon.getEnabled()) {
            return;
        }

        periodController.onAutomaticEntry(beacon);
    }

    @Override
    public void didExitRegion(Region region) {
        PeriodModel currentPeriod = periodController.getCurrentPeriod();
        if (beaconController.isEnabled(region) && currentPeriod != null) {
            listener.sendStopNotification(currentPeriod, DateTime.now());
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // ignore
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

    /**
     * Will be used to communicate with Application
     * and still be decoupled.
     */
    public interface ScannerListener {
        void sendStopNotification(PeriodModel model, DateTime stopDateTime);
    }
}
