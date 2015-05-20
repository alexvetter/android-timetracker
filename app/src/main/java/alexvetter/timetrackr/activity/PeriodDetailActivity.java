package alexvetter.timetrackr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.model.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.PeriodCalculator;
import alexvetter.timetrackr.utils.TargetHours;

public class PeriodDetailActivity extends AppCompatActivity implements DateTimeFormats {

    public static final String EXTRA_PERIOD_ID = "PERIOD_ID";

    private DateTime startDateTime;
    private DateTime endDateTime;

    private TextView dateView;

    private TextView startTimeView;
    private TextView endTimeView;

    private EditText nameEditText;
    private EditText remarkEditText;

    private TextView targetHoursView;
    private TextView actualHoursView;

    private PeriodModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period_detail);

        startDateTime = DateTime.now(DateTimeZone.getDefault());

        TargetHours targetHours = new TargetHours(this);

        endDateTime = DateTime.now(DateTimeZone.getDefault()).plus(targetHours.getDuration(startDateTime.getDayOfWeek()));

        dateView = (TextView) findViewById(R.id.date_picker);
        startTimeView = (TextView) findViewById(R.id.start_time_picker);

        endTimeView = (TextView) findViewById(R.id.end_time_picker);

        nameEditText = (EditText) findViewById(R.id.edit_period_name);
        remarkEditText = (EditText) findViewById(R.id.edit_period_remark);

        targetHoursView = (TextView) findViewById(R.id.period_detail_target_times);
        actualHoursView = (TextView) findViewById(R.id.period_detail_working_times);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        int id = intent.getIntExtra(EXTRA_PERIOD_ID, -1);

        if (id >= 0) {
            fillFromId(id);
        } else {
            updateDateTimeViews();
        }
    }

    private void fillFromId(int id) {
        PeriodDatabaseHandler handler = new PeriodDatabaseHandler();

        model = handler.get(id);

        startDateTime = model.getStartTime();
        endDateTime = model.getEndTime();

        updateDateTimeViews();

        nameEditText.setText(model.getName());
        remarkEditText.setText(model.getRemark());
    }

    private PeriodModel fillModelByView(PeriodModel model) {
        model.setName(nameEditText.getText().toString());
        model.setRemark(remarkEditText.getText().toString());

        model.setStartTime(startDateTime);
        model.setEndTime(endDateTime);

        return model;
    }

    private void checkTimes() {
        if (startDateTime.isAfter(endDateTime)) {
            TargetHours targetHours = new TargetHours(this);
            endDateTime = startDateTime.plus(targetHours.getDuration(startDateTime.getDayOfWeek()));
        }

        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime.toLocalDate();

        if (endDate.isAfter(startDate)) {
            endDateTime = startDateTime.withTime(23, 59, 59, 999);

            Toast.makeText(this, getString(R.string.info_period_end_same_day), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateTimeViews() {
        checkTimes();

        dateView.setText(startDateTime.toString(dateFormatter));

        startTimeView.setText(startDateTime.toString(timeFormatter));
        endTimeView.setText(endDateTime.toString(timeFormatter));

        TargetHours targetHoursSettings = new TargetHours(this);
        Period targetHours = targetHoursSettings.getDuration(startDateTime.getDayOfWeek()).toPeriod();

        Period workingHours = new Period(startDateTime, endDateTime);

        targetHoursView.setText(PeriodCalculator.getPeriodFormatter().print(targetHours.normalizedStandard()));
        actualHoursView.setText(PeriodCalculator.getPeriodFormatter().print(workingHours.normalizedStandard()));
    }

    public void onDateTimeClick(View view) {
        switch (view.getId()) {
            case R.id.date_picker:
                newDatePicker(new StartDateTimeListener(), startDateTime).show(getFragmentManager(), "datePicker");
                break;
            case R.id.start_time_picker:
                newTimePicker(new StartDateTimeListener(), startDateTime).show(getFragmentManager(), "timePicker");
                break;
            case R.id.end_time_picker:
                newTimePicker(new EndDateTimeListener(), endDateTime).show(getFragmentManager(), "timePicker");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_period_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
                PeriodDatabaseHandler handler = new PeriodDatabaseHandler();

                if (model == null) {
                    model = fillModelByView(new PeriodModel());

                    handler.add(model);
                } else {
                    model = fillModelByView(model);

                    handler.update(model);
                }

                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private DatePickerDialog newDatePicker(DatePickerDialog.OnDateSetListener listener, DateTime dateTime) {
        // DateTimePicker library uses old java Calendar (0 = January and so on)
        return DatePickerDialog.newInstance(listener, dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
    }

    private TimePickerDialog newTimePicker(TimePickerDialog.OnTimeSetListener listener,DateTime dateTime) {
        return TimePickerDialog.newInstance(listener, dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), true);
    }

    private static String leadingZero(int num) {
        return String.format("%02d", num);
    }

    private static DateTime generateNewDateTime(DateTime dateTime, int year, int monthOfYear, int dayOfMonth) {
        // DateTimePicker library uses old java Calendar (0 = January and so on)
        String newDate = leadingZero(year) + "-" + leadingZero(monthOfYear + 1) + "-" + leadingZero(dayOfMonth) + " " + dateTime.toString(timeFormatter);
        return dateTimeFormatter.parseDateTime(newDate);
    }

    private static DateTime generateNewDateTime(DateTime dateTime, int hourOfDay, int minute) {
        String newDate = dateTime.toString(dateFormatter) + " " + leadingZero(hourOfDay) + ":" + leadingZero(minute);
        return dateTimeFormatter.parseDateTime(newDate);
    }

    private class StartDateTimeListener implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
        @Override
        public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
            startDateTime = generateNewDateTime(startDateTime, year, monthOfYear, dayOfMonth);
            updateDateTimeViews();
        }

        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
            startDateTime = generateNewDateTime(startDateTime, hourOfDay, minute);
            updateDateTimeViews();
        }
    }

    private class EndDateTimeListener implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
        @Override
        public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
            endDateTime = generateNewDateTime(endDateTime, year, monthOfYear, dayOfMonth);
            updateDateTimeViews();
        }

        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
            endDateTime = generateNewDateTime(endDateTime, hourOfDay, minute);
            updateDateTimeViews();
        }
    }
}
