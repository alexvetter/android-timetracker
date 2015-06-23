package alexvetter.timetrackr.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeriodUtilsTest {

    @Test
    public void testGetPeriodShort() throws Exception {
        DateTime start = DateTime.parse("2015-01-01 12:30", DateTimeFormats.DATE_TIME);
        DateTime end = DateTime.parse("2015-01-01 14:40", DateTimeFormats.DATE_TIME);

        assertEquals("2 h 10 m", PeriodUtils.getPeriodShort(start, end));
    }

    @Test
    public void testGetPeriodLong() throws Exception {
        DateTime start = DateTime.parse("2015-01-01 12:30", DateTimeFormats.DATE_TIME);
        DateTime end = DateTime.parse("2015-01-01 14:40", DateTimeFormats.DATE_TIME);

        assertEquals("2 hours and 10 minutes", PeriodUtils.getPeriodLong(start, end));
    }

    @Test
    public void testNoDuration() throws Exception {
        DateTime start = DateTime.parse("2015-01-01 12:30", DateTimeFormats.DATE_TIME);
        DateTime end = DateTime.parse("2015-01-01 12:30", DateTimeFormats.DATE_TIME);

        assertEquals("0 seconds", PeriodUtils.getPeriodLong(start, end));
    }

}