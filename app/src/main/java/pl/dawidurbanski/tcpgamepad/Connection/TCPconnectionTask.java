package pl.dawidurbanski.tcpgamepad.Connection;

import android.os.AsyncTask;
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
            onFail = null,
            onMessage = null;

    private boolean mIsConnected = false;
    public boolean isIsConnected() { return mIsConnected; }

    private String mAdress;
    private int mPort;

    public TCPconnectionTask(String adress,int port) {
        mIsConnected=false;
        mAdress=adress;
        mPort=port;
        tcPclient = new TCPclient(new TCPclient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                message(message);
            }
        });
        tcPclient.onConnected=new TCPclient.OnEvent() {
            @Override
            public void run() {
                mIsConnected=true;
                if(onConnected!=null) onConnected.run("connected");
            }
        };
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
            Log.i("TCPconnectionTask","connecting"+mAdress+":"+mPort);
            ret=tcPclient.run(mAdress,mPort);
        } catch (Exception e) {
            fail(e.getMessage());
            ret =false;
        }finally {
            mIsConnected=false;
            tcPclient.stop();
        }

        if(!ret) fail(tcPclient.errorMgs);

        end("connection End (doInBackground success: " + ((ret) ? "yes" : "no") + ")");
        return ret;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        end("connection End (success: " + ((success) ? "yes" : "no") + ")");
    }

    private void end(String str)    { if(onEnd!=null)     onEnd.run(str);    }
    private void fail(String str)   { if(onFail!=null)    onFail.run(str);   }
    private void message(String str){ if(onMessage!=null) onMessage.run(str);}

    @Override
    protected void onCancelled() {
        if (tcPclient != null)
            tcPclient.stop();
    }
}
