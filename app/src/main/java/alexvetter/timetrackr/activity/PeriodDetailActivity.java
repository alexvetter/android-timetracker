package alexvetter.timetrackr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.util.Random;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.controller.PeriodController;
import alexvetter.timetrackr.domain.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.PeriodUtils;
import alexvetter.timetrackr.utils.TargetHours;

public class PeriodDetailActivity extends AppCompatActivity {

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

    private PeriodController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period_detail);

        controller = new PeriodController(this);

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

        configureDateTimePicker(dateView, new StartDatePicker());
        configureDateTimePicker(startTimeView, new StartTimePicker());
        configureDateTimePicker(endTimeView, new EndTimePicker());

        Intent intent = getIntent();
        int id = intent.getIntExtra(EXTRA_PERIOD_ID, -1);

        PeriodModel newModel;
        if (id >= 0) {
            // default is -1 so an ID would be greater
            newModel = controller.getPeriod(id);
        } else {
            // get a new default model with no ID
            newModel = controller.getDefaultPeriodModel();
        }

        setModel(newModel);

        updateDateTimeViews();
    }

    protected void configureDateTimePicker(View view, final SublimePickerFragment.Callback callback) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SublimePickerFragment pickerFrag = new SublimePickerFragment();
                pickerFrag.setCallback(callback);

                pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                pickerFrag.show(getSupportFragmentManager(), "SUBLIME_PICKER_" + new Random().nextInt(1000));
            }
        });
    }

    private void setModel(PeriodModel newModel) {
        model = newModel;

        nameEditText.setText(model.getName());
        remarkEditText.setText(model.getRemark());

        startDateTime = model.getStartTime();
        endDateTime = model.getEndTime();
    }

    /**
     * Updates model by view.
     */
    private PeriodModel updateModel() {
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

        dateView.setText(startDateTime.toString(DateTimeFormats.DATE));

        startTimeView.setText(startDateTime.toString(DateTimeFormats.TIME));
        endTimeView.setText(endDateTime.toString(DateTimeFormats.TIME));

        TargetHours targetHoursSettings = new TargetHours(this);
        Period targetHours = targetHoursSettings.getDuration(startDateTime.getDayOfWeek()).toPeriod();

        Period workingHours = new Period(startDateTime, endDateTime);

        targetHoursView.setText(PeriodUtils.getPeriodFormatter().print(targetHours.normalizedStandard()));
        actualHoursView.setText(PeriodUtils.getPeriodFormatter().print(workingHours.normalizedStandard()));
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
                controller.addOrUpdate(updateModel());
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected SublimeOptions getDateOptions(DateTime dateTime) {
        // Sublime options
        SublimeOptions options = new SublimeOptions();

        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);

        options.setDateParams(dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
        options.setTimeParams(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), true);

        return options;
    }

    protected SublimeOptions getTimeOptions(DateTime dateTime) {
        // Sublime options
        SublimeOptions options = new SublimeOptions();

        options.setDisplayOptions(SublimeOptions.ACTIVATE_TIME_PICKER);
        options.setPickerToShow(SublimeOptions.Picker.TIME_PICKER);

        options.setDateParams(dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
        options.setTimeParams(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), true);

        return options;
    }

    class StartDatePicker extends SublimePickerFragment.DateCallback {

        @Override
        public void onSet(LocalDate date) {
            startDateTime = startDateTime.withDate(date);
            endDateTime = endDateTime.withDate(date);
            updateDateTimeViews();
        }

        @Override
        SublimeOptions getOptions() {
            return getDateOptions(startDateTime);
        }
    }

    class StartTimePicker extends SublimePickerFragment.TimeCallback {

        @Override
        public void onSet(LocalTime time) {
            startDateTime = startDateTime.withTime(time);
            updateDateTimeViews();
        }

        @Override
        SublimeOptions getOptions() {
            return getTimeOptions(startDateTime);
        }
    }

    class EndTimePicker extends SublimePickerFragment.TimeCallback {

        @Override
        public void onSet(LocalTime time) {
            endDateTime = endDateTime.withTime(time);
            updateDateTimeViews();
        }

        @Override
        SublimeOptions getOptions() {
            return getTimeOptions(endDateTime);
        }
    }
}
