package alexvetter.timetrackr.controller;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;
import java.util.UUID;

import alexvetter.timetrackr.database.SQLiteHelper;
import alexvetter.timetrackr.domain.BeaconModel;
import alexvetter.timetrackr.domain.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.TargetHours;

public class PeriodControllerTest extends AndroidTestCase {

    private RenamingDelegatingContext context;
    private PeriodController controller;
    private TargetHours targetHoursSettings;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context = new RenamingDelegatingContext(getContext(), "test_");
        JodaTimeAndroid.init(context);

        SQLiteHelper.setInstance(context);

        controller = new PeriodController(context);
        targetHoursSettings = new TargetHours(context);
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
        // Mi, 24. Juni 2015
        DateTime initial = DateTime.parse("2015-06-24 08:00", DateTimeFormats.DATE_TIME);

        // add period1
        PeriodModel period1 = controller.getDefaultPeriodModel();
        period1.setStartTime(initial);
        period1.setEndTime(initial.plusHours(TargetHours.DEFAULT_HOURS_WORKDAY));

        controller.addOrUpdate(period1);

        // add period2
        PeriodModel period2 = controller.getDefaultPeriodModel();
        period2.setStartTime(initial.plusDays(1));
        period2.setEndTime(initial.plusDays(1).plusHours(TargetHours.DEFAULT_HOURS_WORKDAY));

        controller.addOrUpdate(period2);

        Duration totalTarget = new Duration(0);

        Duration target1 = targetHoursSettings.getDuration(period1.getStartTime());
        Duration target2 = targetHoursSettings.getDuration(period2.getStartTime());

        totalTarget = totalTarget.plus(target1).plus(target2);

        Duration totalWork = new Duration(0);

        Duration duration1 = new Duration(period1.getStartTime(), period1.getEndTime());
        Duration duration2 = new Duration(period2.getStartTime(), period2.getEndTime());

        totalWork = totalWork.plus(duration1).plus(duration2);

        assertEquals(totalWork.toPeriod().minus(totalTarget.toPeriod()), controller.calculateTotalDuration());
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