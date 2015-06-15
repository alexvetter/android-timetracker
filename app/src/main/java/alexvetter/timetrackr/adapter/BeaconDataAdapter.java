package alexvetter.timetrackr.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.activity.RegisteredBeaconsActivity;
import alexvetter.timetrackr.database.AbstractDatabaseHandler;
import alexvetter.timetrackr.domain.BeaconModel;

/**
 *
 */
public class BeaconDataAdapter extends RecyclerView.Adapter<BeaconDataAdapter.ViewHolder> implements AbstractDatabaseHandler.DatabaseHandlerListener {

    private AbstractDatabaseHandler<BeaconModel, String> dataset;

    /**
     * Provides references to the views for each data item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView uuidTextView;
        public final TextView nameTextView;
        public final Switch enabledSwitch;
        public final ImageButton deleteButton;

        public ViewHolder(View v) {
            super(v);

            uuidTextView = (TextView) v.findViewById(R.id.beacon_uuid);
            nameTextView = (TextView) v.findViewById(R.id.beacon_name);
            enabledSwitch = (Switch) v.findViewById(R.id.beacon_switch);
            deleteButton = (ImageButton) v.findViewById(R.id.delete_button_beacon);
        }
    }

    /**
     * Data adapter for {@link RegisteredBeaconsActivity}
     * and its RecylerView.
     */
    public BeaconDataAdapter(AbstractDatabaseHandler<BeaconModel, String> dataset) {
        this.dataset = dataset;
        this.dataset.registerAdapter(this);
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public BeaconDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_beacon, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BeaconModel beaconModel = dataset.getByRowNum(position);

        if (beaconModel == null) {
            System.out.println("Beacon is null");
            return;
        }

        String uuid = beaconModel.getId().toString();

        holder.nameTextView.setText(beaconModel.getName());

        holder.uuidTextView.setText(uuid);

        holder.enabledSwitch.setChecked(beaconModel.getEnabled());

        // Tag the switch button, so we know what to delete
        holder.enabledSwitch.setTag(uuid);

        // Tag the delete button, so we know what to delete
        holder.deleteButton.setTag(uuid);
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}