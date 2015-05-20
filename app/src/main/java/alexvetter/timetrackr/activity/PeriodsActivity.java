package alexvetter.timetrackr.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import org.joda.time.Period;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.adapter.PeriodDataAdapter;
import alexvetter.timetrackr.database.PeriodDatabaseHandler;
import alexvetter.timetrackr.utils.DividerItemDecoration;
import alexvetter.timetrackr.utils.PeriodCalculator;
import alexvetter.timetrackr.utils.TargetHours;

public class PeriodsActivity extends AppCompatActivity {

    /**
     * List view
     */
    private RecyclerView recyclerView;

    private TextView hoursTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_periods);

        hoursTextView = (TextView) findViewById(R.id.period_working_times);

        recyclerView = (RecyclerView) findViewById(R.id.period_recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        recyclerView.setAdapter(new PeriodDataAdapter(new PeriodDatabaseHandler(), this));
        recyclerView.getAdapter().registerAdapterDataObserver(new AdapterObserver(this, hoursTextView));

        // use a linear layout manager
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_periods, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_beacon_settings:
                startActivity(new Intent(this, RegisteredBeaconsActivity.class));
                return true;
            case R.id.action_add_period:
                startActivity(new Intent(this, PeriodDetailActivity.class));
                return true;
            case R.id.action_working_hours_settings:
                startActivity(new Intent(this, TargetHoursActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDelete(View view) {
        SwipeLayout layout = (SwipeLayout) findViewById(R.id.swipelayout_period);
        layout.close(true);

        Integer id = (Integer) view.getTag();

        System.out.println("Delete period with ID " + id);

        PeriodDatabaseHandler handler = new PeriodDatabaseHandler();
        handler.deleteById(id);

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onEdit(View view) {
        SwipeLayout layout = (SwipeLayout) findViewById(R.id.swipelayout_period);
        layout.close(true);

        Integer id = (Integer) view.getTag();

        Intent intent = new Intent(this, PeriodDetailActivity.class);
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_ID, id.intValue());

        startActivity(intent);
    }

    private class AdapterObserver extends RecyclerView.AdapterDataObserver {
        Context context;
        TextView hoursTextView;

        public AdapterObserver(Context context, TextView hoursTextView) {
            this.context = context;
            this.hoursTextView = hoursTextView;
        }

        @Override
        public void onChanged() {
            PeriodDatabaseHandler handler = new PeriodDatabaseHandler();
            TargetHours targetHours = new TargetHours(context);

            PeriodCalculator calculator = new PeriodCalculator(handler, targetHours);
            Period period = calculator.calculateTotalDuration();

            hoursTextView.setText(PeriodCalculator.getPeriodFormatter().print(period.normalizedStandard()));
        }
    }
}