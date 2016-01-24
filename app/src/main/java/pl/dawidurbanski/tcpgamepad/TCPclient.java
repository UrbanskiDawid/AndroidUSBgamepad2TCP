package pl.dawidurbanski.tcpgamepad;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Dawid on 23.01.2016.
 */
public class TCPclient {

    private String mADRESS_IP = "??";
    private int mADRESS_PORT = -1;

    public int CONNECTION_CONNECT_TIMEOUT = 2000; //time to wait for connection
    public int CONNECTION_READ_TIMEOUT = 0;       //0-no timeout

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived { void messageReceived(String message);   }
    private OnMessageReceived mMessageListener = null;

    public interface OnEvent { void run(); }
    public OnEvent onConnected=null;


    // while this is true, the server will continue running
    private boolean mRun = false;

    // used to send messages
    private PrintWriter mBufferOut;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPclient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.print(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stop() {
        Log.i(TCPclient.class.getName(), "stopClient");
        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferOut = null;
    }

    String errorMgs="";
    public boolean run(String adress,int port) {

        errorMgs = "";

        boolean ret = true;
        mADRESS_IP = adress;
        mADRESS_PORT = port;

        mRun = true;

        Socket socket = new Socket();

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(mADRESS_IP);
            if(!serverAddr.isReachable(1000))
            {
                errorMgs+=mADRESS_IP+" is unreachable";
                Log.w("TCPclient",errorMgs);
                return false;
            }

            Log.i("TCPclient","connecting "+adress+":"+port+" (timeout:"+CONNECTION_CONNECT_TIMEOUT+")");

            try {
                socket.setSoTimeout(CONNECTION_READ_TIMEOUT);
                socket.connect(new InetSocketAddress(mADRESS_IP, mADRESS_PORT), CONNECTION_CONNECT_TIMEOUT);
            }catch (Exception e){
                String errorStr ="cant connect: "+e.toString();
                Log.w("TCPclient",errorStr );
                errorMgs+=errorStr ;
                return false;
            }

            Log.i("TCPclient", "connected " + adress + ":" + port);
            if(onConnected!=null)   onConnected.run();

            try {
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                // used to read messages from the server
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                String ServerMessage;// message to send to the server
                while (mRun) {
                    ServerMessage = mBufferIn.readLine();
                    if (ServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(ServerMessage);
                    }
                }
            } catch (Exception e) {
                Log.e(TCPclient.class.getName(), e.toString());
                errorMgs+=e.toString();
                ret=false;
            } finally {
                stop();
                socket.close();//the socket must be closed.
            }
        } catch(java.net.ConnectException ce)  {
            Log.w(TCPclient.class.getName(), "cant connect " + ce.toString());
            errorMgs+="cant connect"+ce.toString();
            ret=false;
        } catch (Exception e) {
            Log.e(TCPclient.class.getName(), "Error", e);
            errorMgs+=e.toString();
            ret=false;
        }
        try {
            socket.close();//the socket must be closed.
        }catch (Exception e){ Log.e("TCPclient",e.toString());}
        return ret;
    }

}
