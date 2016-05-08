package pl.dawidurbanski.tcpgamepad.Connection;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import pl.dawidurbanski.tcpgamepad.ByteHelpers;
import pl.dawidurbanski.tcpgamepad.ADdrone.DebugData;
import pl.dawidurbanski.tcpgamepad.R;
import pl.dawidurbanski.tcpgamepad.Settings;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
  * to handle interaction events.
 * Use the {@link ConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectionFragment extends Fragment {

    // UI references.
    private AutoCompleteTextView mAddressView = null;
    private EditText mPortView = null;
    private Spinner
            mTransmissionSec = null,
            mRetransmissionRate = null;

    TCPconnectionTask mTCPconnectionTask = null;

    long mConnectionPing = 9999;

    private Switch mSwitch = null;

    private PingPong mPingPong = null;

    public interface OnEvent { void run(String str);   }
    public OnEvent onLog=null,
                   onSave=null;

    public TCPclient.OnMessageReceived onNewMessage=null;

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
    Handler mHandler = new Handler();
    void updateConnectionStatus(ConnectionStatus newStatus)  {
        if(mConnectionStatus==newStatus)
            return;

        if(mConnectionStatus==ConnectionStatus.error){
            if(newStatus!=ConnectionStatus.connecting)
                return;
        }

        if(newStatus==ConnectionStatus.error){
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    Log.v("updateConnectionStatus","update error to disconnected");
                    if(mConnectionStatus==ConnectionStatus.error) {
                        mConnectionStatus = ConnectionStatus.unknown;
                        updateConnectionStatus(ConnectionStatus.disconnected);
                    }
                }
            }, 5000 );
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

        //ping pong
        mPingPong = new PingPong(new PingPong.OnEvent() {
            @Override
            public void send(byte[] msg) { sendBytes(msg);  }
            @Override
            public void onResponse(long deltaMS) { mConnectionPing=deltaMS; }
        });
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

        String s1 = "" + Math.round(Settings.getInstance().getMessageRateInHz());
        int spinnerPosition1 = spinnerAdapter1.getPosition(s1);
        mRetransmissionRate.setSelection(spinnerPosition1);
        Log.e("ConnectionFragment", "onCreateView() : messageRate='" + s1 + "' / '" + Settings.getInstance().messageRetransmissionRate + "' found at:" + spinnerPosition1);

        //messageSec
        mTransmissionSec = (Spinner) rootView.findViewById(R.id.transmissionSec);
        ArrayAdapter<String> spinnerAdapter2
                = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.retransmissionSec));
        mTransmissionSec.setAdapter(spinnerAdapter2);

        String s2 = "" + Math.round(Settings.getInstance().getMessageRetransmissionTimeInSec());
        int spinnerPosition2 = spinnerAdapter2.getPosition(s2);
        mTransmissionSec.setSelection(spinnerPosition2);
        Log.e("ConnectionFragment", "onCreateView() : messageSec='" + s2 + "' / '" + Settings.getInstance().messageRetransmissionNum + "' found at:" + spinnerPosition2);

        //MessageByteOrder
        mSwitch = (Switch) rootView.findViewById(R.id.MessageByteOrder);
        mSwitch.setChecked( Settings.getInstance().isEnableLittleEndianMessageByteOrder() );

        //Submit
        Button mEmailSignInButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDataFilled();
            }
        });

        return rootView;
    }

    private void Log(final String message)  {
        if(onLog!=null)
            onLog.run(message);
    }

    public boolean isConnected()    {
        return  ( mTCPconnectionTask !=null && mTCPconnectionTask.isIsConnected());
    }

    public void disconnect() {
        updateConnectionStatus(ConnectionStatus.disconnected);
        if(!isConnected()) return;
        Log("disconnecting!");
        mTCPconnectionTask.tcPclient.stop();
    }

    ArrayList<Byte> incomingBytes = new ArrayList<>();
    int readAhead=0;
    private void readIncomingBytes(byte[] in){
        for(byte b:in) {
            incomingBytes.add(b);
            if(readAhead==0) {
                if (incomingBytes.size() >= 4) {
                    if (ByteHelpers.ByteArrayStartsWith(incomingBytes,DebugData.preamble)) {
                        readAhead = DebugData.messageLen-4;
                    } else if (ByteHelpers.ByteArrayStartsWith(incomingBytes,PingPong.preamble)) {
                        readAhead = PingPong.messageLen-4;
                    } else {
                        incomingBytes.clear();
                        Log.d("ConnectionFragment", "skipping: "+incomingBytes.toString());
                    }
                }
            }else{
                readAhead--;
                if(readAhead==0)
                {
                    byte [] arr = new byte[incomingBytes.size()];
                    for(int i=0;i<arr.length;i++)
                        arr[i]=incomingBytes.get(i);

                    getBytes(arr);
                    incomingBytes.clear();
                }
            }
        }
    }


    public void connect() {

        updateConnectionStatus(ConnectionStatus.connecting);

        final String address = Settings.getInstance().address;
        int port = Settings.getInstance().port;

        if (mTCPconnectionTask != null) {
            Log("can't connect, connection is active!");
            return;
        }
        Log("connecting to "+address+":"+port);
        mTCPconnectionTask = new TCPconnectionTask(address, port, new TCPclient.OnMessageReceived() {
            @Override
            public void messageReceived(byte[] in) {
                readIncomingBytes(in);
            }
        });
        mTCPconnectionTask.onConnected=new TCPconnectionTask.OnEvent() {
            @Override
            public void run(String str) {
                Log("connected!");
                updateConnectionStatus(ConnectionStatus.connected);
            }
        };
        mTCPconnectionTask.onEnd=new TCPconnectionTask.OnEvent() {
            @Override
            public void run(String msg) {
            Log("connection end " + msg);
            mTCPconnectionTask.cancel(true) ;
                mTCPconnectionTask = null;
            updateConnectionStatus(ConnectionStatus.disconnected);
            }
        };
        mTCPconnectionTask.onFail=new TCPconnectionTask.OnEvent() {
            @Override
            public void run(String msg) {
            Log("connection fail " + msg);
            updateConnectionStatus(ConnectionStatus.error);
            }
        };
        mTCPconnectionTask.execute((Void) null);
    }

    public boolean sendBytes(byte[] myByteArray) {
        if(!isConnected()) return false;
        try {   mTCPconnectionTask.tcPclient.sendBytes(myByteArray);
        }catch (Exception e){return false;}
        return true;
    }

    private void getBytes(byte[] in) {
        if(in.length<4) return;

        if (in.length == DebugData.messageLen) {
            if(onNewMessage!=null) onNewMessage.messageReceived(in);
            return;
        }

        if (in.length == PingPong.messageLen) {
            mPingPong.HandleIncoming(in);//NOTE: can trigger onResponse EVENT
            return;
        }

        Log("unknown bytes received: '"+in.toString()+"'");
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

        //MessageByteOrder
        boolean messageByteOrder = mSwitch.isChecked();
        Settings.getInstance().setEnableLittleEndianMessageByteOrder(messageByteOrder);

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
