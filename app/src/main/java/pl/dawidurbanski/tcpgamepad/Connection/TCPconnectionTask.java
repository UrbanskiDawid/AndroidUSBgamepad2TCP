package pl.dawidurbanski.tcpgamepad.Connection;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

/**
 * Created by Dawid on 24.01.2016.
 */
public class TCPconnectionTask extends AsyncTask<Void, Void, Boolean> {

    public interface OnEvent { void run(String str); }

    TCPclient tcPclient = null;

    public OnEvent
            onConnected = null,
            onEnd = null,
            onFail = null;

    private Watchdog connectionWatchdog;
    private int watchdogPatienceMS = 10*1000;//how long will dog wait for food

    private boolean mIsConnected = false;
    public boolean isIsConnected() { return mIsConnected; }

    private String mAdress;
    private int mPort;

    public TCPconnectionTask(String adress,int port,final TCPclient.OnMessageReceived messageHandler) {
        mIsConnected=false;
        mAdress=adress;
        mPort=port;
        tcPclient = new TCPclient();
        tcPclient.mMessageListener= new TCPclient.OnMessageReceived() {
            @Override
            public void messageReceived(byte [] msg) {
                connectionWatchdog.feed();
                messageHandler.messageReceived(msg);
            }
        };
        tcPclient.onConnected=new TCPclient.OnEvent() {
            @Override
            public void run() {
                mIsConnected=true;
                if(onConnected!=null) onConnected.run("connected");
            }
        };

        connectionWatchdog = new Watchdog(watchdogPatienceMS,
            new Watchdog.OnEvent() {
                @Override
                public void run() {watchdogStarved();                }
            });
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        mIsConnected=false;
        boolean ret =true;
        if(tcPclient==null)
        {
            fail("tcPclient==null");
            return false;
        }
        try {
            connectionWatchdog.feed();
            Log.i("TCPconnectionTask","connecting"+mAdress+":"+mPort);
            ret=tcPclient.run(mAdress,mPort);
        } catch (Exception e) {
            fail(e.getMessage());
            ret =false;
        }finally {
            mIsConnected=false;
            connectionWatchdog.kill();
            tcPclient.stop();
        }

        if(!ret) fail(tcPclient.errorMgs);

        end("connection End (doInBackground success: " + ((ret) ? "yes" : "no") + ")");
        return ret;
    }

    /*
     *you did not take good care for more than 'watchdogPatienceMS' and now the dog is gone
     */
    public void watchdogStarved()  {
        Log.e(this.getClass().getName(), "watch dog starved!");
        tcPclient.stop();
        fail("watch dog starved!");
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        end("connection End (success: " + ((success) ? "yes" : "no") + ")");
    }

    private void end(String str)    { if(onEnd!=null)     onEnd.run(str);     }
    private void fail(String str)   { if(onFail!=null)    onFail.run(str);    }

    @Override
    protected void onCancelled() {
        if (tcPclient != null)
            tcPclient.stop();
    }
}
