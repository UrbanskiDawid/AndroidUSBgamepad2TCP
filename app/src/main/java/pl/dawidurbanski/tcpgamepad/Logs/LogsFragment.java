package pl.dawidurbanski.tcpgamepad.Logs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

    private void logToDB(Context context,String str)
    {
        if(mDatabase==null) {
            try {
                mDatabase = new ADdroidDB(context);
            } catch (Exception e) {
                Log.e("Log2List",e.getMessage());
                return;
            }
        }
        mDatabase.insertEvent(str);
    }

    public void Log2List(final String str) {

        FragmentActivity fa = getActivity();
        if(fa==null){
            Log.e(LogsFragment.class.getName(),"FragmentActivity==null");
            return;
        }

        logToDB(fa,str);

        fa.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
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

        mAdapter = new ArrayAdapter<>(rootView.getContext(),  R.layout.log_text, mMyList);
        mListView.setAdapter(mAdapter);
        return rootView;
    }

    Context mContext=null;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;

        Activity activity = context instanceof Activity ? (Activity) context : null;
        Log.d("A","onAttached"+activity);
    }

    @Override
    public void onDestroy(){
        Log.w(LogsFragment.class.getName(), "onDestroy");
        super.onDestroy();
    }

}
