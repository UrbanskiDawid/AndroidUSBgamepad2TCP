package pl.dawidurbanski.tcpgamepad.Logs;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by dawid on 27.04.2016.
 */
public class ADdroidDB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "ADdroid.db";

    public static final String DATABASE_TABLE = "events";

    public ADdroidDB(Context context) throws Exception {

        super(context, Environment.getExternalStorageDirectory() + File.separator +  DATABASE_NAME, null, DATABASE_VERSION);
        if(context==null){
            throw new Exception("cant init ADdroidDB using empty context!");
        }
        String msg = "sqlDBOpen:"+Environment.getExternalStorageDirectory() + File.separator +  DATABASE_NAME;
        Log.d("ADdroidDB",msg);
        insertEvent(msg);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+DATABASE_TABLE+"( id INTEGER PRIMARY KEY, created_at DATETIME DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')), event TEXT );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertEvent(String description) {
        ContentValues newEvent = new ContentValues();
        newEvent.put("event", description);
        long ret =0;
        try {
            getWritableDatabase().insert(ADdroidDB.DATABASE_TABLE, null, newEvent);
        }catch (Exception e){
            Log.e("ADdroidDB", "can't insert element! "+e.getMessage());
        }
        return ret;
    }
}
