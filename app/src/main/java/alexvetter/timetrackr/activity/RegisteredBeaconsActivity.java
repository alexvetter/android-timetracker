package alexvetter.timetrackr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.daimajia.swipe.SwipeLayout;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.adapter.BeaconDataAdapter;
import alexvetter.timetrackr.controller.BeaconController;
import alexvetter.timetrackr.utils.DividerItemDecoration;

public class RegisteredBeaconsActivity extends AppCompatActivity {

    /**
     * List view
     */
    private RecyclerView recyclerView;

    /**
     * Beacon controller
     */
    private BeaconController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        recyclerView = (RecyclerView) findViewById(R.id.beacon_recycler_view);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);

        controller = new BeaconController();
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.setAdapter(new BeaconDataAdapter(controller.getDatabaseHandler()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_beacon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_scan_settings:
                final Intent intent = new Intent(this, ScanBeaconsActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeSwipeLayout() {
        SwipeLayout layout = (SwipeLayout) findViewById(R.id.swipelayout_beacon);
        layout.close(true);
    }

    public void onDelete(View view) {
        closeSwipeLayout();

        String uuid = (String) view.getTag();

        controller.onDelete(uuid);
    }

    public void onToggleEnabled(View view) {
        closeSwipeLayout();

        String uuid = (String) view.getTag();

        controller.onToggleEnabled(uuid);
    }
}
