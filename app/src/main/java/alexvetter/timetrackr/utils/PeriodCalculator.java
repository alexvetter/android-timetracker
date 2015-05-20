package alexvetter.timetrackr.utils;

import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.List;

import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.model.PeriodModel;

public class PeriodCalculator {

    public static final PeriodFormatter PERIOD_FORMATTER_LONG = new PeriodFormatterBuilder()
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

    public static final PeriodFormatter PERIOD_FORMATTER_SHORT = new PeriodFormatterBuilder()
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

    private final PeriodDatabaseHandler handler;

    private final TargetHours targetHoursSettings;

    public PeriodCalculator(PeriodDatabaseHandler handler, TargetHours targetHoursSettings) {
        this.handler = handler;
        this.targetHoursSettings = targetHoursSettings;
    }

    public Period calculateTotalDuration() {
        List<LocalDate> processedDates = new ArrayList<>();

        List<PeriodModel> periods = handler.getAll();

        Duration totalWorkingDuration = new Duration(0);
        Duration totalTargetHours = new Duration(0);

        for (PeriodModel period : periods) {

            // add target hours to the startimes day to total target hours
            // we don't count day an which we didn't work
            LocalDate startDate = period.getStartTime().toLocalDate();
            if (processedDates.contains(startDate)) {
                System.out.println("Date already processed and targetHoursSettings were counted. " + startDate.toString());
            } else {
                System.out.println("Date first time processed. Target hours will be added. " + startDate.toString());
                processedDates.add(startDate);

                totalTargetHours = totalTargetHours.plus(targetHoursSettings.getDuration(period.getStartTime().dayOfWeek().get()));
            }

            Duration workingDuration = new Duration(period.getStartTime(), period.getEndTime());
            totalWorkingDuration = totalWorkingDuration.plus(workingDuration);
        }

        Period totalWorkingPeriod = totalWorkingDuration.toPeriod();
        Period totalTargetPeriod = totalTargetHours.toPeriod();

        System.out.println("calculated workings time " + getPeriodFormatter().print(totalWorkingPeriod.normalizedStandard()));
        System.out.println("calculated target time " + getPeriodFormatter().print(totalTargetPeriod.normalizedStandard()));

        return totalWorkingPeriod.minus(totalTargetPeriod);
    }

    public static PeriodFormatter getPeriodFormatter() {
        return PERIOD_FORMATTER_LONG;
    }

    public static PeriodFormatter getPeriodFormatterShort() {
        return PERIOD_FORMATTER_SHORT;
    }
}
