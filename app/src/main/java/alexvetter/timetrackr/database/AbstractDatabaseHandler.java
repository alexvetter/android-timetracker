package alexvetter.timetrackr.database;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseHandler<OBJECT, ID> implements DatabaseHandler<OBJECT, ID> {

    private static final List<RecyclerView.Adapter> adapters = new ArrayList<>();

    public void registerAdapter(RecyclerView.Adapter adapter) {
        synchronized (adapters) {
            adapters.add(adapter);
        }
    }

    public void unregisterAdapter(RecyclerView.Adapter adapter) {
        adapters.remove(adapter);
    }

    public void fireDataSetChanged() {
        synchronized (adapters) {
            System.out.println("fireDataSetChanged " + adapters.size());
            for (final RecyclerView.Adapter adapter : adapters) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
