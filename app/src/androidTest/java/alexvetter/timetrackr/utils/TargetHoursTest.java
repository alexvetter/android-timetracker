package alexvetter.timetrackr.utils;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class TargetHoursTest extends AndroidTestCase {

    Context context;

    public void setUp() throws Exception {
        super.setUp();

        context = new RenamingDelegatingContext(getContext(), "test_");
        JodaTimeAndroid.init(context);
    }

    public void testGetDurationForWorkday() throws Exception {
        TargetHours hours = new TargetHours(context);

        DateTime date = DateTime.parse("2015-06-17", DateTimeFormats.DATE);
        int dayOfWeek = 3;

        Duration d = Duration.standardHours(TargetHours.DEFAULT_HOURS_WORKDAY);

        assertEquals(hours.getDuration(dayOfWeek), hours.getDuration(date));
        assertEquals(d, hours.getDuration(dayOfWeek));

        assertEquals(hours.getDuration(dayOfWeek), hours.getDuration(date));
        assertEquals(d, hours.getDuration(dayOfWeek));
    }

    public void testGetDurationForWeekend() throws Exception {
        TargetHours hours = new TargetHours(context);

        DateTime date = DateTime.parse("2015-06-20", DateTimeFormats.DATE);
        int dayOfWeek = 6;

        Duration d = Duration.standardHours(TargetHours.DEFAULT_HOURS_WEEKEND);

        assertEquals(hours.getDuration(dayOfWeek), hours.getDuration(date));
        assertEquals(d, hours.getDuration(dayOfWeek));
    }

    public void testGetAndSetMonday() throws Exception {
        TargetHours hours = new TargetHours(context);

        int dayOfWeek = 1;

        Duration w = Duration.standardHours(8);
        Duration d = Duration.standardHours(3);

        assertEquals(w, hours.getDuration(dayOfWeek));

        hours.setMonday((int) d.getStandardHours());

        assertEquals(d, hours.getDuration(dayOfWeek));
    }
}