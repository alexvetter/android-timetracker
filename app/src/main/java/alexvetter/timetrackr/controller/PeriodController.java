package alexvetter.timetrackr.controller;

import android.content.Context;

import org.joda.time.DateTime;

import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.domain.BeaconModel;
import alexvetter.timetrackr.domain.PeriodModel;
import alexvetter.timetrackr.utils.TargetHours;


public class PeriodController implements Controller<PeriodDatabaseHandler> {

    private final Context context;
    private final PeriodDatabaseHandler handler;

    public PeriodController(Context context) {
        this.handler = new PeriodDatabaseHandler();
        this.context = context;
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
            current.setRemark("Automatic entry");
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
        System.out.println("Delete period with ID " + id);

        handler.deleteById(id);
    }
}
