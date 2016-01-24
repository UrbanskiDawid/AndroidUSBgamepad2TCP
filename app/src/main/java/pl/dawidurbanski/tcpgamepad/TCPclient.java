package pl.dawidurbanski.tcpgamepad;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by dawid on 23.01.2016.
 */
public class TCPclient {

    private String mADRESS_IP = "??";
    private int mADRESS_PORT = -1;

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived { public void messageReceived(String message);   }
    private OnMessageReceived mMessageListener = null;

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
            mBufferOut.println(message);
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

    public void run(String adress,int port) {

        mADRESS_IP = adress;
        mADRESS_PORT = port;

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(mADRESS_IP);

            Log.i(TCPclient.class.getName(), "Connecting...");
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, mADRESS_PORT);

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
                Log.e(TCPclient.class.getName(), "Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                stop();
            }
        } catch(java.net.ConnectException ce)  {
            Log.e(TCPclient.class.getName(), "cant connect");
        } catch (Exception e) {
            Log.e(TCPclient.class.getName(), "Error", e);
        }
    }

}
