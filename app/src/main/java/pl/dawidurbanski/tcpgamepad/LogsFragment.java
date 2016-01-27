package pl.dawidurbanski.tcpgamepad;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import android.widget.SimpleAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LogsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    ListView listView = null;
    ArrayList<String> listItems = new ArrayList<>();
    SimpleAdapter adapter;//DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Name of this log.
     * @return A new instance of fragment LogsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogsFragment newInstance(String name) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public void Log2List(final String str) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String dateTime =sdf.format(new Date());
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("from", dateTime);
                map.put("to", str);
                mylist.add(map);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    Log.i("edawurb", "logging: " + dateTime+":"+str);
                } else {
                    Log.e("edawurb", "logging: adapter is null");
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_logs, container, false);

        listView= (ListView)rootView.findViewById(R.id.logListView);
        if(listView==null) {   Log.e("edawurb","cant find list!");}

        adapter= new SimpleAdapter(rootView.getContext(), mylist, R.layout.row,
                new String[] {"from", "to"}, new int[] {R.id.FROM_CELL, R.id.TO_CELL});
        listView.setAdapter(adapter);
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
