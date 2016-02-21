package pl.dawidurbanski.tcpgamepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Dawid on 23.01.2016.
 */
public class Settings {

    private Settings(){};

    private static Settings instance = new Settings();
    public static Settings getInstance() {
        return instance;
    }

    private final String mSharedPreferencesName = "pl.dawidurbanski.tcpgamepad";

    private static String SETTINGS_ADDRESS_DEFAULT = "127.0.0.1";
    private static Integer SETTINGS_PORT_DEFAULT = 8080;
    private static Integer SETTINGS_MESSAGE_RETRANSMISSION_RATE_DEFAULT = 50;//in MS (aka 20times per sec)
    private static Integer SETTINGS_MESSAGE_RETRANSMISSION_NUM_DEFAULT = 200;//number of repetition (10sec for rate=50)

    public static String address = SETTINGS_ADDRESS_DEFAULT;
    public static int port = SETTINGS_PORT_DEFAULT;
    public static int messageRetransmissionRate = SETTINGS_MESSAGE_RETRANSMISSION_RATE_DEFAULT;
    public static int messageRetransmissionNum = SETTINGS_MESSAGE_RETRANSMISSION_NUM_DEFAULT;

    private static String
       SETTINGS_ADDRESS = "address",
       SETTINGS_PORT = "port",
       SETTINGS_MESSAGE_RETRANSMISSION_RATE = "msgRate",
       SETTINGS_MESSAGE_RETRANSMISSION_NUM  = "msgNum";

    /*
     * message rate is stored as milliseconds between sending message
     * min 20 milliseconds
     */
    public void setMessageRateInHz(float hz) {
        messageRetransmissionRate = Math.max(20, (int)Math.ceil(1000.0f*1.0f/hz) );
        Log.e("MessageRate","("+hz+"Hz) := "+messageRetransmissionRate+" mSec delay");
    }

    public float getMessageRateInHz() {
        return 1000.0f / (float)messageRetransmissionRate;
    }

    public float getMessageRateInMillisSec() {
        return (float)messageRetransmissionRate;
    }

    /*
     * delay between sending messages in seconds
     */
    public float getMessageRetransmissionTimeInSec() {
        return messageRetransmissionRate * messageRetransmissionNum / 1000.0f;
    }

    /*
     * how long to re-transmit message is seconds
     * minimum 1 second
     */
    public void setMessageRetransmissionTimeInSec(float sec) {
        messageRetransmissionNum= Math.max(1,(int)Math.ceil(1000.0f * sec / messageRetransmissionRate ));
        Log.e("RetransmissionTime","("+sec+"sec) :="+messageRetransmissionNum + " number of retransmissions @"+ getMessageRateInHz()+"Hz");
    }

    public void save(Context context) {
        if(context==null) {
            Log.e(Settings.class.getName()+":"," save() !contest is null! )");
            return;
        }
        SharedPreferences userDetails = context.getSharedPreferences(mSharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.clear();
        edit.putString(SETTINGS_ADDRESS, address.trim());
        edit.putInt(SETTINGS_PORT, port);
        edit.putInt(SETTINGS_MESSAGE_RETRANSMISSION_RATE, messageRetransmissionRate);
        edit.putInt(SETTINGS_MESSAGE_RETRANSMISSION_NUM, messageRetransmissionNum);
        edit.commit();
        Toast.makeText(context, "Settings are saved.",Toast.LENGTH_SHORT).show();
    }

    public void load(Context context) {
        if(context==null) {
            Log.e(Settings.class.getName()+":"," load() !contest is null! )");
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(mSharedPreferencesName, Context.MODE_PRIVATE);
        try {
            address = sp.getString(SETTINGS_ADDRESS, SETTINGS_ADDRESS_DEFAULT);
            port = sp.getInt(SETTINGS_PORT, SETTINGS_PORT_DEFAULT);
            messageRetransmissionRate = sp.getInt(SETTINGS_MESSAGE_RETRANSMISSION_RATE, SETTINGS_MESSAGE_RETRANSMISSION_RATE_DEFAULT);
            messageRetransmissionNum = sp.getInt(SETTINGS_MESSAGE_RETRANSMISSION_NUM, SETTINGS_MESSAGE_RETRANSMISSION_NUM_DEFAULT);
        }catch (Exception e){
            address = SETTINGS_ADDRESS_DEFAULT;
            port = SETTINGS_PORT_DEFAULT;
            messageRetransmissionRate = SETTINGS_MESSAGE_RETRANSMISSION_RATE_DEFAULT;
            messageRetransmissionNum = SETTINGS_MESSAGE_RETRANSMISSION_NUM_DEFAULT;
        }
        Log.e("Settings","load() address="+address+";");
        Log.e("Settings","load() port  ="+port+"; ");
        Log.e("Settings","load() messageRetransmissionRate="+messageRetransmissionRate+" : "+ getMessageRateInHz()+"Hz; ");
        Log.e("Settings","load() messageRetransmissionNum ="+messageRetransmissionNum+ " : "+ getMessageRetransmissionTimeInSec()+"sec;");

        Toast.makeText(context, "Settings loaded.",Toast.LENGTH_SHORT).show();
    }
}
