package alexvetter.timetrackr.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.utils.InputFilterMinMax;
import alexvetter.timetrackr.utils.TargetHours;

public class TargetHoursActivity extends AppCompatActivity {

    private TextView mondayTextView;
    private TextView tuesdayTextView;
    private TextView wednesdayTextView;
    private TextView thursdayTextView;
    private TextView fridayTextView;
    private TextView saturdayTextView;
    private TextView sundayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working_hours);

        mondayTextView = (TextView) findViewById(R.id.times_monday);
        tuesdayTextView = (TextView) findViewById(R.id.times_tuesday);
        wednesdayTextView = (TextView) findViewById(R.id.times_wednesday);
        thursdayTextView = (TextView) findViewById(R.id.times_thursday);
        fridayTextView = (TextView) findViewById(R.id.times_friday);
        saturdayTextView = (TextView) findViewById(R.id.times_saturday);
        sundayTextView = (TextView) findViewById(R.id.times_sunday);

        InputFilter[] inputFilter = new InputFilter[]{new InputFilterMinMax(0, 24)};

        mondayTextView.setFilters(inputFilter);
        tuesdayTextView.setFilters(inputFilter);
        wednesdayTextView.setFilters(inputFilter);
        thursdayTextView.setFilters(inputFilter);
        fridayTextView.setFilters(inputFilter);
        saturdayTextView.setFilters(inputFilter);
        sundayTextView.setFilters(inputFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TargetHours hours = new TargetHours(this);

        mondayTextView.setText(String.valueOf(hours.getMonday().getStandardHours()));
        tuesdayTextView.setText(String.valueOf(hours.getTuesday().getStandardHours()));
        wednesdayTextView.setText(String.valueOf(hours.getWednesday().getStandardHours()));
        thursdayTextView.setText(String.valueOf(hours.getThursday().getStandardHours()));
        fridayTextView.setText(String.valueOf(hours.getFriday().getStandardHours()));
        saturdayTextView.setText(String.valueOf(hours.getSaturday().getStandardHours()));
        sundayTextView.setText(String.valueOf(hours.getSunday().getStandardHours()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_working_hours, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveWorkingHours();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveWorkingHours() {
        TargetHours hours = new TargetHours(this);

        hours.setMonday(Integer.parseInt(mondayTextView.getText().toString()));
        hours.setTuesday(Integer.parseInt(tuesdayTextView.getText().toString()));
        hours.setWednesday(Integer.parseInt(wednesdayTextView.getText().toString()));
        hours.setThursday(Integer.parseInt(thursdayTextView.getText().toString()));
        hours.setFriday(Integer.parseInt(fridayTextView.getText().toString()));
        hours.setSaturday(Integer.parseInt(saturdayTextView.getText().toString()));
        hours.setSunday(Integer.parseInt(sundayTextView.getText().toString()));
    }
}
