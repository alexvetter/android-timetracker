package alexvetter.timetrackr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import alexvetter.timetrackr.domain.Period;
import alexvetter.timetrackr.utils.DateTimeFormats;

public class PeriodDatabaseHandler extends AbstractDatabaseHandler<Period, Integer> {

    /**
     * Table name
     */
    private static final String TABLE_PERIOD = "period";

    /**
     * Table columns
     */
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_REMARK = "remark";
    private static final String KEY_STARTTIME = "starttime";
    private static final String KEY_ENDTIME = "endtime";

    static {
        final String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PERIOD + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_REMARK + " TEXT,"
                + KEY_STARTTIME + " TEXT,"
                + KEY_ENDTIME + " TEXT"
                + ")";

        SQLiteHelper.CreateUpgradeRunnable upgradeRunnable = new SQLiteHelper.CreateUpgradeRunnable() {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(CREATE_CONTACTS_TABLE);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // Drop older table if existed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERIOD);

                // Create tables again
                onCreate(db);
            }
        };

        SQLiteHelper.addCreateUpgradeRunnable(TABLE_PERIOD, upgradeRunnable);
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
    public void add(Period object) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Inserting Row
        db.insert(TABLE_PERIOD, null, getContentFromObject(object));

        fireDataSetChanged();
    }

    @Override
    public Period getByRowNum(int rowNum) {
        String selectQuery = "SELECT * FROM " + TABLE_PERIOD + " ORDER BY datetime(" + KEY_STARTTIME + ") DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Period object = null;
        if (cursor.moveToPosition(rowNum)) {
            object = getObjectFromCursor(cursor);
        }

        cursor.close();

        return object;
    }

    /**
     * Reads object from table.
     *
     * @param id entity id
     * @return object
     */
    @Override
    public Period get(Integer id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PERIOD, new String[]{
                        KEY_ID,
                        KEY_NAME,
                        KEY_REMARK,
                        KEY_STARTTIME,
                        KEY_ENDTIME
                }, KEY_ID + "=?",
                idWhereClause(id), null, null, null, null);

        Period object = null;
        if (cursor.moveToFirst()) {
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
    public List<Period> getAll() {
        List<Period> result = new ArrayList<>(size());

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_PERIOD + " ORDER BY datetime(" + KEY_STARTTIME + ") DESC";

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
    public int update(Period object) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = getContentFromObject(object);

        // updating row
        int updated = db.update(TABLE_PERIOD, values, KEY_ID + " = ?", idWhereClause(object.getId()));

        fireDataSetChanged();

        return updated;
    }

    /**
     * @param object to be deleted
     */
    @Override
    public Period delete(Period object) {
        return deleteById(object.getId());
    }

    @Override
    public Period deleteById(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        Period result = get(id);

        db.delete(TABLE_PERIOD, KEY_ID + " = ?", idWhereClause(id));

        fireDataSetChanged();

        return result;
    }

    @Override
    public int size() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Select Count(*) Query
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_PERIOD;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int size = 0;

        // Get first column from first row
        if (cursor.moveToFirst()) {
            size = Integer.parseInt(cursor.getString(0));
        }

        cursor.close();

        return size;
    }

    public Period getCurrentPeriod() {
        SQLiteDatabase db = this.getReadableDatabase();

        String now = DateTime.now().toString(DateTimeFormats.dateTimeFormatter);

        String selectQuery = "SELECT * FROM " + TABLE_PERIOD + " WHERE datetime('" + now + "') BETWEEN datetime(" + KEY_STARTTIME + ") AND datetime(" + KEY_ENDTIME + ")";
        Cursor cursor = db.rawQuery(selectQuery, null);

        System.out.println("Current count: " + cursor.getCount());

        Period model = null;
        if (cursor.moveToFirst()) {
            model = getObjectFromCursor(cursor);
        }

        cursor.close();

        return model;
    }

    private static ContentValues getContentFromObject(Period object) {
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, object.getName());
        values.put(KEY_REMARK, object.getRemark());
        values.put(KEY_STARTTIME, object.getStartTimeString());
        values.put(KEY_ENDTIME, object.getEndTimeString());

        return values;
    }

    private static Period getObjectFromCursor(Cursor cursor) {
        Period object = new Period();

        object.setId(Integer.parseInt(cursor.getString(0)));
        object.setName(cursor.getString(1));
        object.setRemark(cursor.getString(2));
        object.setStartTime(cursor.getString(3));
        object.setEndTime(cursor.getString(4));

        return object;
    }

    private static String[] idWhereClause(int id) {
        return new String[]{String.valueOf(id)};
    }
}