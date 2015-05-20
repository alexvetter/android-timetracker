package alexvetter.timetrackr.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.adapter.ScanDataAdapter;
import alexvetter.timetrackr.database.BeaconDatabaseHandler;
import alexvetter.timetrackr.model.BeaconModel;
import alexvetter.timetrackr.utils.DividerItemDecoration;

public class ScanBeaconsActivity extends AppCompatActivity implements BeaconConsumer {
    private BeaconManager beaconManager;

    private RecyclerView recyclerView;
    private ScanDataAdapter scanDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

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
        scanDataAdapter = new ScanDataAdapter();
        recyclerView.setAdapter(scanDataAdapter);

        beaconManager.bind(this);

        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(false);
        }

        scanDataAdapter.clear();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    scanDataAdapter.addDevice(beacon);
                    notifyAdapater();
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException ignore) {
            //ignore
        }
    }

    private void notifyAdapater() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanDataAdapter.notifyDataSetChanged();
            }
        });
    }

    public void onDeviceAdd(View view) {
        int itemPosition = (int) view.getTag();
        final Beacon device = scanDataAdapter.getDevice(itemPosition);

        final String address = device.getId1().toUuidString();

        String name = device.getBluetoothName() == null || device.getBluetoothName().isEmpty() ? getString(R.string.unknown_device) : device.getBluetoothName();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.dialog_add_beacon_title));
        alert.setMessage(getString(R.string.dialog_add_beacon_message));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(name);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                System.out.println("Add device: " + address + " " + device.getIdentifiers().toString());

                BeaconDatabaseHandler handler = new BeaconDatabaseHandler();

                BeaconModel model = handler.get(address);

                if (model != null) {
                    Toast.makeText(ScanBeaconsActivity.this, R.string.beacon_already_registered, Toast.LENGTH_SHORT).show();
                } else {
                    model = new BeaconModel();

                    model.setUuid(address);
                    model.setName(value);
                    model.setEnabled(true);

                    handler.add(model);

                    Toast.makeText(ScanBeaconsActivity.this, R.string.beacon_registered, Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }
}
