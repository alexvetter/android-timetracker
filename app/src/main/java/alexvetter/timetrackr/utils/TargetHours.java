package alexvetter.timetrackr.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Hours;

public class TargetHours {
    private static final String SHARED_PREFERENCES = "WORKING_HOURS";

    public enum Day {
        MONDAY("HOURS_MONDAY", DateTimeConstants.MONDAY, 8),
        TUESDAY("HOURS_TUESDAY", DateTimeConstants.TUESDAY, 8),
        WEDNESDAY("HOURS_WEDNESDAY", DateTimeConstants.WEDNESDAY, 8),
        THURSDAY("HOURS_THURSDAY", DateTimeConstants.THURSDAY, 8),
        FRIDAY("HOURS_FRIDAY", DateTimeConstants.FRIDAY, 8),
        SATURDAY("HOURS_SATURDAY", DateTimeConstants.SATURDAY, 0),
        SUNDAY("HOURS_SUNDAY", DateTimeConstants.SUNDAY, 0);

        private String settingsKey;
        private int dayOfWeek;
        private int defaultTargetHours;

        Day(String settingsKey, int dayOfWeek, int defaultTargetHours) {
            this.settingsKey = settingsKey;
            this.dayOfWeek = dayOfWeek;
            this.defaultTargetHours = defaultTargetHours;
        }

        public String getSettingsKey() {
            return settingsKey;
        }

        public int getDayOfWeek() {
            return dayOfWeek;
        }

        public int getDefaultTargetHours() {
            return defaultTargetHours;
        }

        public static Day valueOf(int dayOfWeek) {
            for (Day day : values()) {
                if (day.getDayOfWeek() == dayOfWeek) {
                    return day;
                }
            }
            return null;
        }
    }

    private Context context;

    public TargetHours(Context context) {
        this.context = context;
    }

    public Duration getDuration(DateTime dateTime) {
        return getDuration(dateTime.getDayOfWeek());
    }

    public Duration getDuration(int dayOfWeek) {
        return getDuration(Day.valueOf(dayOfWeek));
    }

    private Duration getDuration(Day dayOfWeek) {
        Duration targetHours = new Duration(0);

        switch (dayOfWeek) {
            case MONDAY:
                targetHours = getMonday();
                break;
            case TUESDAY:
                targetHours = getTuesday();
                break;
            case WEDNESDAY:
                targetHours = getWednesday();
                break;
            case THURSDAY:
                targetHours = getThursday();
                break;
            case FRIDAY:
                targetHours = getFriday();
                break;
            case SATURDAY:
                targetHours = getSaturday();
                break;
            case SUNDAY:
                targetHours = getSunday();
                break;
        }

        return targetHours;
    }

    public Duration getMonday() {
        return getTargetHours(Day.MONDAY);
    }

    public void setMonday(int value) {
        setTargetHours(Day.MONDAY, value);
    }

    public Duration getTuesday() {
        return getTargetHours(Day.TUESDAY);
    }

    public void setTuesday(int value) {
        setTargetHours(Day.TUESDAY, value);
    }

    public Duration getWednesday() {
        return getTargetHours(Day.WEDNESDAY);
    }

    public void setWednesday(int value) {
        setTargetHours(Day.WEDNESDAY, value);
    }

    public Duration getThursday() {
        return getTargetHours(Day.THURSDAY);
    }

    public void setThursday(int value) {
        setTargetHours(Day.THURSDAY, value);
    }

    public Duration getFriday() {
        return getTargetHours(Day.FRIDAY);
    }

    public void setFriday(int value) {
        setTargetHours(Day.FRIDAY, value);
    }

    public Duration getSaturday() {
        return getTargetHours(Day.SATURDAY);
    }

    public void setSaturday(int value) {
        setTargetHours(Day.SATURDAY, value);
    }

    public Duration getSunday() {
        return getTargetHours(Day.SUNDAY);
    }

    public void setSunday(int value) {
        setTargetHours(Day.SUNDAY, value);
    }

    private Duration getTargetHours(Day day) {
        SharedPreferences settings = this.context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int targetHours = settings.getInt(day.getSettingsKey(), day.getDefaultTargetHours());
        return Hours.hours(targetHours).toStandardDuration();
    }

    private void setTargetHours(Day day, int targetHours) {
        SharedPreferences settings = this.context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(day.getSettingsKey(), targetHours);

        editor.apply();
    }
}
