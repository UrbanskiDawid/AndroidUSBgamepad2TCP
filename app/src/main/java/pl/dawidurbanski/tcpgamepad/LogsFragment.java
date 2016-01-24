package pl.dawidurbanski.tcpgamepad;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.app.ListFragment;


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
    ArrayAdapter<String> adapter;//DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW


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
                String fullLogMessage = sdf.format(new Date()) + ":" + str;

                listItems.add(0,fullLogMessage);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    //listView.setSelection(adapter.getCount() - 1);
                    Log.i("edawurb", "logging: " + fullLogMessage);
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

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItems);
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
