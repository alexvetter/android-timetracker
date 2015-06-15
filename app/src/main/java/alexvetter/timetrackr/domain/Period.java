package alexvetter.timetrackr.domain;


import org.joda.time.DateTime;

import alexvetter.timetrackr.utils.DateTimeFormats;

/**
 *
 */
public class Period implements DomainModel<Integer> {
    private Integer id;
    private String name;
    private String remark;
    private DateTime startTime;
    private DateTime endTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public String getStartTimeString() {
        return toString(startTime);
    }

    /**
     * @param startTimeString "2010-01-19 23:59:59"
     */
    public void setStartTime(String startTimeString) {
        this.startTime = parseDateTime(startTimeString);
    }

    public void setStartTime(DateTime starttime) {
        this.startTime = starttime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public String getEndTimeString() {
        return toString(endTime);
    }

    /**
     * @param endTimeString "2010-01-19 23:59:59"
     */
    public void setEndTime(String endTimeString) {
        this.endTime = parseDateTime(endTimeString);
    }

    public void setEndTime(DateTime endtime) {
        this.endTime = endtime;
    }

    private DateTime parseDateTime(String dateTimeString) {
        return DateTimeFormats.dateTimeFormatter.parseDateTime(dateTimeString);
    }

    private String toString(DateTime dateTime) {
        return dateTime.toString(DateTimeFormats.dateTimeFormatter);
    }

    @Override
    public String toString() {
        return "Period{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", remark='" + remark + '\'' +
                ", startTime=" + getStartTimeString() +
                ", endTime=" + getEndTimeString() +
                '}';
    }
}
