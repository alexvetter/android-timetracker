package timetrackr.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Interface for Database Handlers.
 * {@link OBJECT} should be the POJO which should be will be persisted.
 */
interface DatabaseHandler<OBJECT, ID> {
    SQLiteDatabase getReadableDatabase();

    SQLiteDatabase getWritableDatabase();

    void add(OBJECT object);

    OBJECT getByRowNum(int rowNum);

    OBJECT get(ID id);

    List<OBJECT> getAll();

    int update(OBJECT object);

    OBJECT delete(OBJECT object);

    OBJECT deleteById(ID id);

    int size();
}
