package timetrackr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class SQLiteHelper extends SQLiteOpenHelper {

    /**
     * Database Version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Database Name
     */
    private static final String DATABASE_NAME = "timetrackdb";

    /**
     * @param context
     */
    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private volatile static SQLiteHelper INSTANCE;

    /**
     * @return
     */
    static SQLiteHelper getInstance() {
        return INSTANCE;
    }

    /**
     * @param context
     * @return
     */
    public static void setInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SQLiteHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SQLiteHelper(context);
                }
            }
        }
    }

    private static Map<String, CreateUpgradeRunnable> createUpgradeRunnable = new HashMap<>();

    static void addCreateUpgradeRunnable(String name, CreateUpgradeRunnable runnable) {
        System.out.println("Add createUpgrade for " + name);
        createUpgradeRunnable.put(name, runnable);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Map.Entry<String, CreateUpgradeRunnable> runnable : createUpgradeRunnable.entrySet()) {
            runnable.getValue().onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Map.Entry<String, CreateUpgradeRunnable> runnable : createUpgradeRunnable.entrySet()) {
            runnable.getValue().onUpgrade(db, oldVersion, newVersion);
        }
    }

    interface CreateUpgradeRunnable {
        void onCreate(SQLiteDatabase db);

        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }
}
