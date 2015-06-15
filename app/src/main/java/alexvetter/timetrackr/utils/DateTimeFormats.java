package alexvetter.timetrackr.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Convenient interface for all DateTimeFormats
 * which are used be this app.
 */
public interface DateTimeFormats {
    /**
     * e.g. Mon, 5 Jul 2015
     */
    DateTimeFormatter NICE_DATE = DateTimeFormat.forPattern("EEE, d MMM yyyy");

    /**
     * e.g. 2015-07-05 15:00
     */
    DateTimeFormatter DATE_TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    /**
     * e.g. 2015-07-05
     */
    DateTimeFormatter DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * e.g. 15:00
     */
    DateTimeFormatter TIME = DateTimeFormat.forPattern("HH:mm");
}
