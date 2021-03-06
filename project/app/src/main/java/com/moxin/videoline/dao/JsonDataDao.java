package com.moxin.videoline.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.moxin.videoline.modle.JsonData;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "JSON_DATA".
*/
public class JsonDataDao extends AbstractDao<JsonData, Long> {

    public static final String TABLENAME = "JSON_DATA";

    /**
     * Properties of entity JsonData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Key = new Property(1, String.class, "key", false, "KEY");
        public final static Property Val = new Property(2, String.class, "val", false, "VAL");
    }


    public JsonDataDao(DaoConfig config) {
        super(config);
    }
    
    public JsonDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"JSON_DATA\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"KEY\" TEXT," + // 1: key
                "\"VAL\" TEXT);"); // 2: val
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"JSON_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, JsonData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(2, key);
        }
 
        String val = entity.getVal();
        if (val != null) {
            stmt.bindString(3, val);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, JsonData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(2, key);
        }
 
        String val = entity.getVal();
        if (val != null) {
            stmt.bindString(3, val);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public JsonData readEntity(Cursor cursor, int offset) {
        JsonData entity = new JsonData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // key
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // val
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, JsonData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setKey(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setVal(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(JsonData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(JsonData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(JsonData entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
