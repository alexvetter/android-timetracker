package alexvetter.timetrackr.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import alexvetter.timetrackr.R;

public class BluetoothCheck {

    public static void checkBluetoothSupport(Activity activity) {
        // Use this check to determine whether BLE is
        // supported on the device. Then you can selectively
        // disable BLE-related features.
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }

        // Ensures Bluetooth is available on
        // the device and it is enabled. If not,
        // displays a dialog requesting user
        // permission to enable Bluetooth.
        final int REQUEST_ENABLE_BT = 2;
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}
