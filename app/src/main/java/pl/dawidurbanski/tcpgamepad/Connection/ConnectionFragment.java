package pl.dawidurbanski.tcpgamepad.Connection;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import pl.dawidurbanski.tcpgamepad.R;
import pl.dawidurbanski.tcpgamepad.Settings;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
  * to handle interaction events.
 * Use the {@link ConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectionFragment extends Fragment{

    // UI references.
    private AutoCompleteTextView mAddressView = null;
    private EditText mPortView = null;
    private Spinner
            mTransmissionSec = null,
            mRetransmissionRate = null;

    TCPconnectionTask mAuthTask = null;

    public interface OnEvent { void run(String str);   }
    public OnEvent onLog=null,
                   onSave=null;

    public interface OnConnectionStatusEvent { void change(ConnectionStatus newStatus);   }
    public OnConnectionStatusEvent onConnectionStatusChange=null;

    public enum ConnectionStatus
    {
        unknown,
        disconnected,
        error,//only connecting cant change this state
        connecting,
        connected
    }

    ConnectionStatus mConnectionStatus = ConnectionStatus.unknown;
    void updateConnectionStatus(ConnectionStatus newStatus)  {
        if(mConnectionStatus==newStatus)
            return;

        if(mConnectionStatus==ConnectionStatus.error){
            if(newStatus!=ConnectionStatus.connecting)
                return;
        }

        mConnectionStatus = newStatus;
        if(onConnectionStatusChange!=null)
            onConnectionStatusChange.change(mConnectionStatus);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConnectionFragment.
     */
    public static ConnectionFragment newInstance(){
        return new ConnectionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);

        //address
        mAddressView = (AutoCompleteTextView) rootView.findViewById(R.id.adress);
        mAddressView.setText(Settings.getInstance().address);

        //port
        mPortView = (EditText) rootView.findViewById(R.id.port);
        mPortView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    onDataFilled();
                    return true;
                }
                return false;
            }
        });
        mPortView.setText("" + Settings.getInstance().port);


        //messageRate
        mRetransmissionRate = (Spinner) rootView.findViewById(R.id.transmissionRate);
        ArrayAdapter<String> spinnerAdapter1
                = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.transmissionRate));
        mRetransmissionRate.setAdapter(spinnerAdapter1);

        String s1 = ""+Math.round(Settings.getInstance().getMessageRateInHz());
        int spinnerPosition1 = spinnerAdapter1.getPosition( s1 );
        mRetransmissionRate.setSelection(spinnerPosition1);
        Log.e("ConnectionFragment", "onCreateView() : messageRate='" + s1 +"' / '"+ Settings.getInstance().messageRetransmissionRate+ "' found at:" + spinnerPosition1);

        //messageSec
        mTransmissionSec = (Spinner) rootView.findViewById(R.id.transmissionSec);
        ArrayAdapter<String> spinnerAdapter2
                = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.retransmissionSec));
        mTransmissionSec.setAdapter(spinnerAdapter2);

        String s2 = ""+Math.round(Settings.getInstance().getMessageRetransmissionTimeInSec());
        int spinnerPosition2 = spinnerAdapter2.getPosition(s2);
        mTransmissionSec.setSelection(spinnerPosition2);
        Log.e("ConnectionFragment", "onCreateView() : messageSec='" + s2 + "' / '"+Settings.getInstance().messageRetransmissionNum+"' found at:" + spinnerPosition2);


        Button mEmailSignInButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDataFilled();
            }
        });

        updateConnectionStatus(ConnectionStatus.disconnected);

        return rootView;
    }

    private void Log(final String message)  {
        if(onLog!=null)
            onLog.run(message);
    }

    public boolean isConnected()    {
        return  ( mAuthTask!=null && mAuthTask.isIsConnected());
    }

    public void disconnect() {
        updateConnectionStatus(ConnectionStatus.disconnected);
        if(!isConnected()) return;
        Log("disconnecting!");
        mAuthTask.tcPclient.stop();
    }

    public void connect() {

        updateConnectionStatus(ConnectionStatus.connecting);

        String address = Settings.getInstance().address;
        int port = Settings.getInstance().port;

        if (mAuthTask != null) {
            Log("cant connect conection is active");
            return;
        }

        Log("connecting to "+address+":"+port);
        mAuthTask = new TCPconnectionTask(address,port);
        mAuthTask.onConnected=new TCPconnectionTask.OnEvent() {
            @Override
            public void run(String str) {
                Log("connected!");
                updateConnectionStatus(ConnectionStatus.connected);
            }
        };
        mAuthTask.onEnd=new TCPconnectionTask.OnEvent() {
            @Override
            public void run(String msg) {
            Log("connection end " + msg);
            mAuthTask.cancel(true) ;mAuthTask = null;
            updateConnectionStatus(ConnectionStatus.disconnected);
            }
        };
        mAuthTask.onFail=new TCPconnectionTask.OnEvent() {
            @Override
            public void run(String msg) {
            Log("connection fail " + msg);
            updateConnectionStatus(ConnectionStatus.error);
            }
        };
        mAuthTask.execute((Void) null);
    }

    public boolean sendBytes(byte[] myByteArray) {
        if(!isConnected()) return false;
        try {   mAuthTask.tcPclient.sendBytes(myByteArray);
        }catch (Exception e){return false;}
        return true;
    }

    private void onDataFilled()  {
        // Reset errors.
        mAddressView.setError(null);
        mPortView.setError(null);

        // Store values at the time of the login attempt.
        boolean cancel = false;
        View focusView = null;

        //port
        String port = mPortView.getText().toString();
        if (!isPortValid(port)) {
            mPortView.setError(getString(R.string.error_invalid_port));
            focusView = mPortView;
            cancel = true;
        }

        //address
        String adress = mAddressView.getText().toString();
        if (!isAddressValid(adress)) {
            mAddressView.setError(getString(R.string.error_invalid_adress));
            focusView = mAddressView;
            cancel = true;
        }

        if(! isInt(mRetransmissionRate.getSelectedItem().toString()) ) {
            focusView = mRetransmissionRate;
            cancel = true;
        }

        if(! isInt(mTransmissionSec.getSelectedItem().toString()) ) {
            focusView = mTransmissionSec;
            cancel = true;
        }

        if (cancel) {
            Log("connection data in NOT valid");
            focusView.requestFocus();
        } else {
            Log("connection data in OK");
            onSaveData();
        }
    }

    private void onSaveData()  {

        //address
        String address = mAddressView.getText().toString();
        Settings.getInstance().address = address;

        //port
        String port = mPortView.getText().toString();
        Settings.getInstance().port   = Integer.parseInt(port);

        //RetransmissionRate
        float hz = Float.parseFloat( mRetransmissionRate.getSelectedItem().toString() );
        Settings.getInstance().setMessageRateInHz(hz);

        //mTransmissionSec
        float transmissionSec = Float.parseFloat( mTransmissionSec.getSelectedItem().toString() );
        Settings.getInstance().setMessageRetransmissionTimeInSec(transmissionSec);

        //save
        Settings.getInstance().save( getActivity().getApplicationContext() );

        if(onSave!=null)
            onSave.run("");
    }

    private boolean isAddressValid(String email) {
        return (!TextUtils.isEmpty(email) && email.matches("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}$"));
    }

    private boolean isPortValid(String port)  {
        return (!TextUtils.isEmpty(port) && port.matches("^[0-9]{1,8}$"));
    }
    private boolean isInt(String s) {
        try
        {
            Float.parseFloat(s);
            return true;
        }catch (Exception e) {
            return false;
        }
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
