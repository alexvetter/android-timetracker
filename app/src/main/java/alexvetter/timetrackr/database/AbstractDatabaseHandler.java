package alexvetter.timetrackr.database;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseHandler<OBJECT, ID> implements DatabaseHandler<OBJECT, ID> {

    private static final List<DatabaseHandlerListener> adapters = new ArrayList<>();

    /**
     * Register a new listener
     * @param adapter
     */
    public void registerAdapter(DatabaseHandlerListener adapter) {
        synchronized (adapters) {
            adapters.add(adapter);
        }
    }

    /**
     * Notifies all listeners
     */
    void fireDataSetChanged() {
        synchronized (adapters) {
            System.out.println("fireDataSetChanged " + adapters.size());
            for (final DatabaseHandlerListener adapter : adapters) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    /**
     * Listener which gets notified on data change
     */
    public interface DatabaseHandlerListener {
        void notifyDataSetChanged();
    }
}
