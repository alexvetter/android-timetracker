package timetrackr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.daimajia.swipe.SwipeLayout;

import timetrackr.R;
import timetrackr.adapter.BeaconDataAdapter;
import timetrackr.database.BeaconDatabaseHandler;
import timetrackr.model.BeaconModel;
import timetrackr.utils.DividerItemDecoration;

public class RegisteredBeaconsActivity extends AppCompatActivity {

    /**
     * List view
     */
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.beacon_recycler_view);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.setAdapter(new BeaconDataAdapter(new BeaconDatabaseHandler(), this));
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

    public void onDelete(View view) {
        SwipeLayout layout = (SwipeLayout) findViewById(R.id.swipelayout_beacon);
        layout.close(true);

        String uuid = (String) view.getTag();

        System.out.println("Delete beacon with UUID " + uuid);

        BeaconDatabaseHandler handler = new BeaconDatabaseHandler();
        handler.deleteById(uuid);

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onToggleEnabled(View view) {
        SwipeLayout layout = (SwipeLayout) findViewById(R.id.swipelayout_beacon);
        layout.close(true);

        String uuid = (String) view.getTag();

        System.out.println("Toggle enabled of beacon with UUID " + uuid);

        BeaconDatabaseHandler handler = new BeaconDatabaseHandler();

        BeaconModel model = handler.get(uuid);
        model.toggleEnabled();

        handler.update(model);

        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
