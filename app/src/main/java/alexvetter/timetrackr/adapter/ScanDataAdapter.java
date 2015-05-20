package alexvetter.timetrackr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

import alexvetter.timetrackr.R;

/**
 * Adapter for holding devices found through scanning.
 */
public class ScanDataAdapter extends RecyclerView.Adapter<ScanDataAdapter.ViewHolder> {

    private final List<Beacon> devices;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView deviceName;
        public TextView deviceAddress;
        public ImageButton deviceAdd;

        public ViewHolder(View view) {
            super(view);

            deviceAddress = (TextView) view.findViewById(R.id.device_address);
            deviceName = (TextView) view.findViewById(R.id.device_name);
            deviceAdd = (ImageButton) view.findViewById(R.id.device_add);
        }
    }

    public ScanDataAdapter() {
        super();
        this.devices = new ArrayList<>();
    }

    public void addDevice(Beacon device) {
        if (!devices.contains(device)) {
            devices.add(device);
        }
    }

    public Beacon getDevice(int position) {
        return devices.get(position);
    }

    public void clear() {
        devices.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Beacon beacon = devices.get(position);

        final String deviceName = beacon.getBluetoothName();
        if (deviceName != null && deviceName.length() > 0) {
            holder.deviceName.setText(deviceName);
        } else {
            holder.deviceName.setText(R.string.unknown_device);
        }

        holder.deviceAddress.setText(beacon.getId1().toUuidString());

        holder.deviceAdd.setTag(position);
    }
}