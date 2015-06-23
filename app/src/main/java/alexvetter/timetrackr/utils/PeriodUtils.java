package alexvetter.timetrackr.utils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class PeriodUtils {

    private static final PeriodFormatter PERIOD_FORMATTER_LONG = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix(" day", " days")
            .appendSeparator(" and ")
            .appendHours()
            .appendSuffix(" hour", " hours")
            .appendSeparator(" and ")
            .appendMinutes()
            .appendSuffix(" minute", " minutes")
            .appendSeparator(" and ")
            .appendSeconds()
            .appendSuffix(" second", " seconds")
            .toFormatter();

    private static final PeriodFormatter PERIOD_FORMATTER_SHORT = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix(" d")
            .appendSeparator(" ")
            .appendHours()
            .appendSuffix(" h")
            .appendSeparator(" ")
            .appendMinutes()
            .appendSuffix(" m")
            .appendSeparator(" ")
            .appendSeconds()
            .appendSuffix(" s")
            .toFormatter();

    public static String getPeriodShort(DateTime start, DateTime end) {
        Duration duration = new Duration(start, end);

        return getPeriodFormatterShort().print(duration.toPeriod().normalizedStandard());
    }

    public static String getPeriodLong(DateTime start, DateTime end) {
        Duration duration = new Duration(start, end);

        return getPeriodFormatter().print(duration.toPeriod().normalizedStandard());
    }

    public static PeriodFormatter getPeriodFormatter() {
        return PERIOD_FORMATTER_LONG;
    }

    public static PeriodFormatter getPeriodFormatterShort() {
        return PERIOD_FORMATTER_SHORT;
    }
}
