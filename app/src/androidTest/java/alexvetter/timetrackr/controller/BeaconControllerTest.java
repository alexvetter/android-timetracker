package alexvetter.timetrackr.controller;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import net.danlew.android.joda.JodaTimeAndroid;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.List;
import java.util.UUID;

import alexvetter.timetrackr.database.SQLiteHelper;
import alexvetter.timetrackr.domain.BeaconModel;

public class BeaconControllerTest extends AndroidTestCase {

    private RenamingDelegatingContext context;
    private BeaconController controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context = new RenamingDelegatingContext(getContext(), "test_");
        JodaTimeAndroid.init(context);

        SQLiteHelper.setInstance(context);

        controller = new BeaconController();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        List<BeaconModel> beacons = controller.getDatabaseHandler().getAll();

        for (BeaconModel beacon : beacons) {
            controller.onDelete(beacon.getId().toString());
        }
    }

    public void testAddGetDelete() throws Exception {
        UUID uuid = UUID.randomUUID();

        Beacon.Builder builder = new Beacon.Builder();
        builder.setId1(uuid.toString());

        Beacon beacon = builder.build();

        controller.onDeviceAdd(beacon, "Test Beacon");

        assertEquals(uuid, controller.getBeacon(uuid).getId());

        controller.onDelete(uuid.toString());

        assertNull(controller.getBeacon(uuid));
    }

    public void testGetBeaconsAsRegions() throws Exception {
        UUID uuid = UUID.randomUUID();

        Beacon.Builder builder = new Beacon.Builder();
        builder.setId1(uuid.toString());

        Beacon beacon = builder.build();

        controller.onDeviceAdd(beacon, "Test Beacon");

        List<Region> regions = controller.getBeaconsAsRegions();

        assertEquals(1, regions.size());

        assertEquals(uuid, regions.get(0).getId1().toUuid());
    }

    public void testToggleEnabledAndIsEnabled() throws Exception {
        UUID uuid = UUID.randomUUID();

        Beacon.Builder builder = new Beacon.Builder();
        builder.setId1(uuid.toString());

        Beacon beacon = builder.build();

        controller.onDeviceAdd(beacon, "Test Beacon");

        List<Region> regions = controller.getBeaconsAsRegions();

        assertEquals(1, regions.size());
        assertTrue(controller.isEnabled(regions.get(0)));

        controller.onToggleEnabled(uuid.toString());

        regions = controller.getBeaconsAsRegions();

        assertEquals(1, regions.size());
        assertFalse(controller.isEnabled(regions.get(0)));
    }

    public void testIsDeviceRegistered() throws Exception {
        UUID uuid = UUID.randomUUID();

        Beacon.Builder builder = new Beacon.Builder();
        builder.setId1(uuid.toString());

        Beacon beacon = builder.build();

        controller.onDeviceAdd(beacon, "Test Beacon");

        assertTrue(controller.isDeviceRegistered(beacon));
    }
}