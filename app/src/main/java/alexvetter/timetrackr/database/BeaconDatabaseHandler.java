package alexvetter.timetrackr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import alexvetter.timetrackr.domain.BeaconModel;

public class BeaconDatabaseHandler extends AbstractDatabaseHandler<BeaconModel, String> {

    /**
     * Database Version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Database Name
     */
    private static final String DATABASE_NAME = "timetrackdb";

    /**
     * Table name
     */
    private static final String TABLE = "beacon";

    /**
     * Table columns
     */
    private static final String KEY_ID = "uuid";
    private static final String KEY_NAME = "name";
    private static final String KEY_ENABLED = "enabled";

    public static void registerTable() {
        final String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_ENABLED + " INTEGER"
                + ")";

        SQLiteHelper.CreateUpgradeRunnable upgradeRunnable = new SQLiteHelper.CreateUpgradeRunnable() {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(CREATE_CONTACTS_TABLE);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // Drop older table if existed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE);

                // Create tables again
                onCreate(db);
            }
        };

        SQLiteHelper.addCreateUpgradeRunnable(TABLE, upgradeRunnable);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return SQLiteHelper.getInstance().getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return SQLiteHelper.getInstance().getWritableDatabase();
    }

    /**
     * Create new row in table.
     *
     * @param object model which should be persisted
     */
    @Override
    public void add(BeaconModel object) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Inserting Row
        db.insert(TABLE, null, getContentFromObject(object));

        fireDataSetChanged();
    }

    /**
     * Reads object from table.
     *
     * @param id entity id
     * @return object
     */
    @Override
    public BeaconModel get(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE, new String[]{
                        KEY_ID,
                        KEY_NAME,
                        KEY_ENABLED
                }, KEY_ID + "=?",
                idWhereClause(id), null, null, null, null);

        BeaconModel object = null;

        if (cursor.moveToFirst()) {
            object = getObjectFromCursor(cursor);
        }

        cursor.close();

        return object;
    }

    @Override
    public BeaconModel getByRowNum(int rowNum) {
        String selectQuery = "SELECT * FROM " + TABLE + " ORDER BY " + KEY_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        BeaconModel object = null;
        if (cursor.moveToPosition(rowNum)) {
            object = getObjectFromCursor(cursor);
        }

        cursor.close();

        return object;
    }

    /**
     * Returns all model objects
     *
     * @return list of model objects
     */
    @Override
    public List<BeaconModel> getAll() {
        List<BeaconModel> result = new ArrayList<>(size());

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE + " ORDER BY " + KEY_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                result.add(getObjectFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return result;
    }

    /**
     * Updates an entity in table.
     *
     * @param object model object
     * @return number of entities which where updated (0 or 1)
     */
    @Override
    public int update(BeaconModel object) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = getContentFromObject(object);

        // updating row
        int updated = db.update(TABLE, values, KEY_ID + " = ?", idWhereClause(object.getId().toString()));

        fireDataSetChanged();

        return updated;
    }

    /**
     * @param object to be deleted
     */
    @Override
    public BeaconModel delete(BeaconModel object) {
        return deleteById(object.getId().toString());
    }

    @Override
    public BeaconModel deleteById(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        BeaconModel result = get(id);

        db.delete(TABLE, KEY_ID + " = ?", idWhereClause(id));

        fireDataSetChanged();

        return result;
    }

    @Override
    public int size() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Select Count(*) Query
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int size = 0;

        // Get first column from first row
        if (cursor.moveToFirst()) {
            size = Integer.parseInt(cursor.getString(0));
        }

        cursor.close();

        return size;
    }

    private static ContentValues getContentFromObject(BeaconModel object) {
        ContentValues values = new ContentValues();

        values.put(KEY_ID, object.getId().toString());
        values.put(KEY_NAME, object.getName());
        values.put(KEY_ENABLED, object.getEnabled() ? 1 : 0);

        return values;
    }

    private static BeaconModel getObjectFromCursor(Cursor cursor) {
        BeaconModel object = new BeaconModel();

        object.setId(UUID.fromString(cursor.getString(0)));
        object.setName(cursor.getString(1));
        object.setEnabled(cursor.getInt(2) == 1);

        return object;
    }

    private static String[] idWhereClause(String id) {
        return new String[]{String.valueOf(id)};
    }
}