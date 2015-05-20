package alexvetter.timetrackr.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public interface DateTimeFormats {
    /**
     * Mon, 5 Jul 2015
     */
    DateTimeFormatter niceDateFormatter = DateTimeFormat.forPattern("EEE, d MMM yyyy");

    /**
     * 2015-07-05 15:00
     */
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    /**
     * 2015-07-05
     */
    DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * 15:00
     */
    DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
}
