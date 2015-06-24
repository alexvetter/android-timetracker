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

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.adapter.ScanDataAdapter;
import alexvetter.timetrackr.controller.BeaconController;
import alexvetter.timetrackr.utils.BluetoothCheck;
import alexvetter.timetrackr.utils.DividerItemDecoration;

public class ScanBeaconsActivity extends AppCompatActivity implements BeaconConsumer {
    private BeaconManager beaconManager;

    private RecyclerView recyclerView;
    private ScanDataAdapter scanDataAdapter;

    private BeaconController controller;

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

        controller = new BeaconController();

        BluetoothCheck.checkBluetoothSupport(this);
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
            public void didRangeBeaconsInRegion(Collection<org.altbeacon.beacon.Beacon> beacons, Region region) {
                for (org.altbeacon.beacon.Beacon beacon : beacons) {
                    scanDataAdapter.addDevice(beacon);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scanDataAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException ignore) {
            //ignore
        }
    }

    public void onDeviceAdd(View view) {
        int itemPosition = (int) view.getTag();
        final org.altbeacon.beacon.Beacon device = scanDataAdapter.getDevice(itemPosition);

        if (controller.isDeviceRegistered(device)) {
            Toast.makeText(ScanBeaconsActivity.this, R.string.beacon_already_registered, Toast.LENGTH_SHORT).show();
            return;
        }

        String defaultName = device.getBluetoothName() == null || device.getBluetoothName().isEmpty() ? getString(R.string.unknown_device) : device.getBluetoothName();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.dialog_add_beacon_title));
        alert.setMessage(getString(R.string.dialog_add_beacon_message));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(defaultName);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                controller.onDeviceAdd(device, input.getText().toString());
                Toast.makeText(ScanBeaconsActivity.this, R.string.beacon_registered, Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }
}
