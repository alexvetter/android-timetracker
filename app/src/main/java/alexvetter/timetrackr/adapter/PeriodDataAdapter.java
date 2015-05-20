package alexvetter.timetrackr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.joda.time.Period;

import alexvetter.timetrackr.R;
import alexvetter.timetrackr.activity.PeriodsActivity;
import alexvetter.timetrackr.database.AbstractDatabaseHandler;
import alexvetter.timetrackr.model.PeriodModel;
import alexvetter.timetrackr.utils.DateTimeFormats;
import alexvetter.timetrackr.utils.PeriodCalculator;

/**
 * Adapter for holding tracked periods.
 */
public class PeriodDataAdapter extends RecyclerView.Adapter<PeriodDataAdapter.ViewHolder> implements DateTimeFormats, AbstractDatabaseHandler.DatabaseHandlerListener {

    private AbstractDatabaseHandler<PeriodModel, Integer> dataset;

    /**
     * Provides references to the views for each data item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageButton deleteButton;
        public final ImageButton editButton;

        public final TextView nameTextView;
        public final TextView remarkTextView;

        public final TextView dateTextView;
        public final TextView startTimeTextView;
        public final TextView endTimeTextView;
        public final TextView durationTextView;

        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.period_name);
            remarkTextView = (TextView) v.findViewById(R.id.period_remark);

            dateTextView = (TextView) v.findViewById(R.id.period_date);
            startTimeTextView = (TextView) v.findViewById(R.id.period_starttime);
            endTimeTextView = (TextView) v.findViewById(R.id.period_endtime);
            durationTextView = (TextView) v.findViewById(R.id.period_duration);

            deleteButton = (ImageButton) v.findViewById(R.id.delete_button_period);
            editButton = (ImageButton) v.findViewById(R.id.edit_button_period);
        }
    }

    /**
     * Data adapter for {@link PeriodsActivity}
     * and its RecylerView.
     */
    public PeriodDataAdapter(AbstractDatabaseHandler<PeriodModel, Integer> dataset) {
        this.dataset = dataset;
        this.dataset.registerAdapter(this);
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public PeriodDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_period, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PeriodModel dataModel = dataset.getByRowNum(position);

        holder.nameTextView.setText(dataModel.getName());

        holder.remarkTextView.setText(dataModel.getRemark());

        holder.dateTextView.setText(dataModel.getStartTime().toString(niceDateFormatter));
        holder.startTimeTextView.setText(dataModel.getStartTime().toString(timeFormatter));
        holder.endTimeTextView.setText(dataModel.getEndTime().toString(timeFormatter));

        Period period = new Period(dataModel.getStartTime(), dataModel.getEndTime());

        holder.durationTextView.setText(PeriodCalculator.getPeriodFormatterShort().print(period.normalizedStandard()));

        holder.deleteButton.setTag(dataModel.getId());
        holder.editButton.setTag(dataModel.getId());
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}