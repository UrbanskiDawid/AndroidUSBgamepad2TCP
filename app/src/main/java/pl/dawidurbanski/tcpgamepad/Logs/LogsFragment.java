package pl.dawidurbanski.tcpgamepad.Logs;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pl.dawidurbanski.tcpgamepad.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LogsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogsFragment extends Fragment {

    public class ADdroidDB extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;

        public static final String DATABASE_NAME = "ADdroid.db";

        public static final String DATABASE_TABLE = "events";

        public ADdroidDB(Context context) {

            super(context, Environment.getExternalStorageDirectory() + File.separator +  DATABASE_NAME, null, DATABASE_VERSION);
            Log.d("ADdroidDB",Environment.getExternalStorageDirectory() + File.separator +  DATABASE_NAME);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+DATABASE_TABLE+"( id INTEGER PRIMARY KEY, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, event TEXT );");
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
            return getWritableDatabase().insert(ADdroidDB.DATABASE_TABLE, null, newEvent);
        }
    }

    private ADdroidDB mDatabase = null;

    private ListView mListView = null;

    private ArrayAdapter mAdapter;
    private ArrayList<String> mMyList = new ArrayList<>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LogsFragment.
     */
    public static LogsFragment newInstance() {
        return new LogsFragment();
    }

    public void Log2List(final String str) {

        if(mDatabase==null) {
            mDatabase = new ADdroidDB(getContext());
        }
        mDatabase.insertEvent(str);

        FragmentActivity fa = getActivity();
        if(fa==null){
            Log.e(LogsFragment.class.getName(),"FragmentActivity==null");
            return;
        }

        fa.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String dateTime =sdf.format(new Date());

                mMyList.add(0, dateTime + ":" + str);

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    Log.i(LogsFragment.class.getName(), "logging: " + dateTime+":"+str);
                } else {
                    Log.e(LogsFragment.class.getName(), "logging: mAdapter is null");
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_logs, container, false);

        mListView = (ListView)rootView.findViewById(R.id.logListView);
        if(mListView ==null) {   Log.e(LogsFragment.class.getName(), "cant find list!");}

        mAdapter = new ArrayAdapter<>(rootView.getContext(),  android.R.layout.simple_list_item_1, mMyList);
        mListView.setAdapter(mAdapter);
        return rootView;
    }
}
