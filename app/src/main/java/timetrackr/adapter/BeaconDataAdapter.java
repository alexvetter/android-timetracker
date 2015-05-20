package timetrackr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import timetrackr.R;
import timetrackr.activity.RegisteredBeaconsActivity;
import timetrackr.database.AbstractDatabaseHandler;
import timetrackr.model.BeaconModel;

/**
 *
 */
public class BeaconDataAdapter extends RecyclerView.Adapter<BeaconDataAdapter.ViewHolder> {

    private AbstractDatabaseHandler<BeaconModel, String> dataset;
    private Context context;

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
     *
     * @param dataset
     * @param context
     */
    public BeaconDataAdapter(AbstractDatabaseHandler<BeaconModel, String> dataset, Context context) {
        this.dataset = dataset;
        this.context = context;

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
        BeaconModel dataModel = dataset.getByRowNum(position);

        if (dataModel == null) {
            System.out.println("BeaconModel is null");
            return;
        }

        holder.nameTextView.setText(dataModel.getName());

        holder.uuidTextView.setText(dataModel.getUuid());

        holder.enabledSwitch.setChecked(dataModel.getEnabled());

        // Tag the switch button, so we know what to delete
        holder.enabledSwitch.setTag(dataModel.getUuid());

        // Tag the delete button, so we know what to delete
        holder.deleteButton.setTag(dataModel.getUuid());
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}