package timetrackr.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import timetrackr.R;
import timetrackr.adapter.ScanDataAdapter;
import timetrackr.database.BeaconDatabaseHandler;
import timetrackr.model.BeaconModel;
import timetrackr.utils.DividerItemDecoration;

public class ScanBeaconsActivity extends AppCompatActivity implements BeaconConsumer {
    private BeaconManager beaconManager;

    private RecyclerView recyclerView;
    private ScanDataAdapter scanDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.scan_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // use a linear layout manager
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);

        beaconManager = BeaconManager.getInstanceForApplication(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (beaconManager.isBound(this)) {
            beaconManager.unbind(this);
        }

        scanDataAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        scanDataAdapter = new ScanDataAdapter(this);
        recyclerView.setAdapter(scanDataAdapter);

        beaconManager.bind(this);

        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(false);
        }

        scanDataAdapter.clear();
    }

    @Override
    public void onBeaconServiceConnect() {
        System.out.println("onBeaconServiceConnect");

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    StringBuffer s = new StringBuffer();

                    for (Beacon beacon : beacons) {
                        beacon.getBluetoothName();
                        s.append('–').append(beacon.toString()).append('\n');
                        scanDataAdapter.addDevice(beacon);
                        notifyAdapater();
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    protected void notifyAdapater() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanDataAdapter.notifyDataSetChanged();
            }
        });
    }

    public void onDeviceAdd(View view) {
        int itemPosition = (int) view.getTag();
        Beacon device = scanDataAdapter.getDevice(itemPosition);

        String address = device.getId1().toUuidString();
        String name = device.getBluetoothName() == null || device.getBluetoothName().isEmpty() ? getString(R.string.unknown_device) : device.getBluetoothName();

        System.out.println("Add device: " + address + " " + device.getIdentifiers().toString());

        BeaconDatabaseHandler handler = new BeaconDatabaseHandler();

        BeaconModel model = handler.get(address);

        if (model != null) {
            Toast.makeText(this, R.string.beacon_already_registered, Toast.LENGTH_SHORT).show();
        } else {
            model = new BeaconModel();

            model.setUuid(address);
            model.setName(name);
            model.setEnabled(true);

            handler.add(model);

            Toast.makeText(this, R.string.beacon_registered, Toast.LENGTH_SHORT).show();
        }
    }
}
