package pl.dawidurbanski.tcpgamepad.Connection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import pl.dawidurbanski.tcpgamepad.ADdrone.Message;

/**
 * Created by dawid on 08.05.2016.
 */
public class PingPong {

    public static final byte preamble[] = {'%','%','%','%'};
    public static final int messageLen = 4+4+2;

    private static int mInterval = 2000;

    private Timer mTimer = null;

    public interface OnEvent {
        void send(byte [] msg);
        void onResponse(long deltaMS);
    }
    private OnEvent mEvents = null;

    public PingPong(OnEvent events) {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() { pingPongTick(); }
        }, 0, mInterval);
        mEvents = events;
    }

    private class idAndDataTime
    {
       int mID = -1;
       Date mDateCreate = null;
       long  mDeltaTimeMS = 0;

       idAndDataTime(int ID) {
           mID = ID;
           mDateCreate = new Date();
       }

       public boolean hasResponse() {
           return !(mDeltaTimeMS==0);
       }

       public long  responded(){
           Date nowDate = new Date();
           mDeltaTimeMS = nowDate.getTime() - mDateCreate.getTime();
           return mDeltaTimeMS;
       }
    }
    ArrayList<idAndDataTime> mMemory = new ArrayList<>();

    private static int mMemoryMaxSize = 100;

    private static int counter = 0;
    private ByteBuffer buffer = ByteBuffer.allocate(messageLen);//preamble 4B, integer 4B, CRC 2B
    private void pingPongTick() {

        if(PingPong.counter==Integer.MAX_VALUE) PingPong.counter=0;
        PingPong.counter++;

        buffer.clear();
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(PingPong.preamble);                  //  0-3 preamble
        buffer.putInt(PingPong.counter);              //  4-7 counter

        byte [] CRC = Message.calculateCRC16(buffer.array(),0,8);
        buffer.put(CRC);

        mMemory.add( new idAndDataTime(PingPong.counter) );//remember
        if(mMemory.size()>PingPong.mMemoryMaxSize){
            mMemory.remove(0);
        }
        mEvents.send(buffer.array());
    }

    public boolean HandleIncoming(byte [] message) {
        if(message.length != PingPong.messageLen)
            return false;

        //check preamble
        for(int i = 0; i<PingPong.preamble.length; i++) {
            if (message[i] != PingPong.preamble[i])
                return false;
        }

        byte [] incomingCounterBA = { message[4],message[5],message[6],message[7] };

        ByteBuffer wrapped = ByteBuffer.wrap(incomingCounterBA); // big-endian by default
        int incomingCounter = wrapped.getInt();

        for(idAndDataTime i : mMemory) {

            if(i.hasResponse())
                continue;

            if(incomingCounter==i.mID) {
                long responseTimeMS = i.responded();
                mEvents.onResponse(responseTimeMS);
                //NOTE: mMemory is not cleared to keep memory off last N responses
                return true;
            }
        }

        return false;
    }
}
