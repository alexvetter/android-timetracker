package alexvetter.timetrackr.controller;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.domain.BeaconModel;
import alexvetter.timetrackr.domain.PeriodModel;
import alexvetter.timetrackr.utils.TargetHours;


public class PeriodController implements Controller<PeriodDatabaseHandler> {

    private final Context context;
    private final PeriodDatabaseHandler handler;
    private final TargetHours targetHours;

    public PeriodController(Context context) {
        this.handler = new PeriodDatabaseHandler();
        this.context = context;
        this.targetHours = new TargetHours(context);
    }

    @Override
    public PeriodDatabaseHandler getDatabaseHandler() {
        return handler;
    }

    public PeriodModel getCurrentPeriod() {
        return handler.getCurrentPeriod();
    }

    public void onAutomaticEntry(BeaconModel beacon) {
        PeriodModel current = handler.getCurrentPeriod();
        if (current == null) {
            DateTime now = DateTime.now();

            TargetHours targetHours = new TargetHours(context);

            current = new PeriodModel();

            current.setName(beacon.getName());
            current.setRemark(context.getString(R.string.automatic_entry_remark));
            current.setStartTime(now);
            current.setEndTime(now.plus(targetHours.getDuration(now)));

            handler.add(current);
        }
    }

    public void onStopPeriod(int periodId, DateTime endDateTime) {
        PeriodModel model = handler.get(periodId);

        if (model != null) {
            model.setEndTime(endDateTime);
            handler.update(model);
        }
    }

    public void onDelete(Integer id) {
        handler.deleteById(id);
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
            if (!processedDates.contains(startDate)) {
                processedDates.add(startDate);
                totalTargetHours = totalTargetHours.plus(targetHours.getDuration(period.getStartTime().dayOfWeek().get()));
            }

            Duration workingDuration = new Duration(period.getStartTime(), period.getEndTime());
            totalWorkingDuration = totalWorkingDuration.plus(workingDuration);
        }

        Period totalWorkingPeriod = totalWorkingDuration.toPeriod();
        Period totalTargetPeriod = totalTargetHours.toPeriod();

        return totalWorkingPeriod.minus(totalTargetPeriod);
    }

    public PeriodModel getDefaultPeriodModel() {
        PeriodModel model = new PeriodModel();
        model.setStartTime(DateTime.now());

        Duration target = targetHours.getDuration(model.getStartTime().getDayOfWeek());
        if (target.isEqual(Hours.hours(0).toStandardDuration())) {
            target = Hours.hours(1).toStandardDuration();
        }

        model.setEndTime(model.getStartTime().plus(target));

        return model;
    }

    public PeriodModel getPeriod(int id) {
        return handler.get(id);
    }

    public void addOrUpdate(PeriodModel model) {
        if (model.getId() == null) {
            handler.add(model);
        } else {
            handler.update(model);
        }
    }
}
