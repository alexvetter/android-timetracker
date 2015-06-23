package alexvetter.timetrackr.controller;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

import alexvetter.timetrackr.database.SQLiteHelper;
import alexvetter.timetrackr.domain.BeaconModel;
import alexvetter.timetrackr.domain.PeriodModel;

public class PeriodControllerTest extends AndroidTestCase {

    private RenamingDelegatingContext context;
    private PeriodController controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context = new RenamingDelegatingContext(getContext(), "test_");
        JodaTimeAndroid.init(context);

        SQLiteHelper.setInstance(context);

        controller = new PeriodController(context);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        List<PeriodModel> periods = controller.getDatabaseHandler().getAll();

        for (PeriodModel period : periods) {
            controller.onDelete(period.getId());
        }
    }

    public void testCurrentPeriod() throws Exception {
        PeriodModel current = controller.getDefaultPeriodModel();

        controller.addOrUpdate(current);

        assertTrue(controller.existsCurrentPeriod());
        assertEquals(current.getStartTime(), controller.getCurrentPeriod().getStartTime());
    }

    public void testOnAutomaticEntry() throws Exception {
        BeaconModel beacon = new BeaconModel();
        beacon.setId(UUID.randomUUID());
        beacon.setName("Temp Beacon");

        assertFalse(controller.existsCurrentPeriod());

        controller.onAutomaticEntry(beacon);

        assertEquals(beacon.getName(), controller.getCurrentPeriod().getName());
    }

    public void testOnStopPeriod() throws Exception {
        BeaconModel beacon = new BeaconModel();
        beacon.setId(UUID.randomUUID());
        beacon.setName("Temp Beacon");

        assertFalse(controller.existsCurrentPeriod());

        controller.onAutomaticEntry(beacon);

        int id = controller.getCurrentPeriod().getId();
        DateTime endDateTime = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).plusHours(4);

        controller.onStopPeriod(id, endDateTime);

        assertEquals(endDateTime, controller.getPeriod(id).getEndTime());
    }

    public void testOnDelete() throws Exception {
        PeriodModel current = controller.getDefaultPeriodModel();

        controller.addOrUpdate(current);
        controller.addOrUpdate(current);
        controller.addOrUpdate(current);

        List<PeriodModel> periods = controller.getDatabaseHandler().getAll();

        assertEquals(3, periods.size());

        for (PeriodModel period : periods) {
            controller.onDelete(period.getId());
        }

        periods = controller.getDatabaseHandler().getAll();

        assertEquals(0, periods.size());
    }

    public void testCalculateTotalDuration() throws Exception {

    }

    public void testAddOrUpdate() throws Exception {
        controller.addOrUpdate(controller.getDefaultPeriodModel());

        PeriodModel current = controller.getCurrentPeriod();

        assertNotNull(current);

        DateTime endDateTime = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).plusHours(4);

        current.setEndTime(endDateTime);

        controller.addOrUpdate(current);

        assertEquals(endDateTime, controller.getPeriod(current.getId()).getEndTime());
    }
}