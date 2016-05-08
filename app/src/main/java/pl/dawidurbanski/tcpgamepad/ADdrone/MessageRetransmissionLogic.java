package pl.dawidurbanski.tcpgamepad.ADdrone;

import java.util.Timer;
import java.util.TimerTask;

import pl.dawidurbanski.tcpgamepad.Settings;

/**
 * Created by dawid on 28.03.2016.
 */
public class MessageRetransmissionLogic implements Message.OnNewInput {

    public MessageRetransmissionLogic(OnRetransmissionEvent events) {
        mEvents = events;
        reset();
    }

    /*
     events reported by 'MessageRetransmissionLogic'
     */
    public interface OnRetransmissionEvent {

        /*
        queued message is transited accordingly to 'Settings.getInstance().messageRetransmissionRate'
         */
        void onTransmitMessage(byte [] message);

        /*
        retransmission has ended  due to:
        * no input in 'Settings.getInstance().messageRetransmissionRate'
        after this 'mTimeoutMessage' will be retransmitted
         */
        void onRetransmissionEnded(byte [] message);
    }

    private OnRetransmissionEvent mEvents;

    /**
     *  number of retransmissions left
     */
    private int mMessageRetransmissionNum = 0;

    /**
     *  mMessage to send
     *  if null mTimeoutMessage will be sent
     */
    private byte [] mMessage = null;

    /**
     * this message will be sent if no new messages are in queue
     */
    private byte [] mTimeoutMessage = null;

    /**
     * main application mTimer for sending messages
     */
    private Timer mTimer = null;

    /**
     * starts/restart Fixed event
     */
    public void reset() {
        stop();

        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() { tick(); }
        }, 0, Settings.getInstance().messageRetransmissionRate);

        mTimeoutMessage = Message.generate(0,0,0,0, Message.Command.ERROR_NOINPUT, Settings.getInstance().isEnableLittleEndianMessageByteOrder());
    }

    /**
     * stops execution of events
     */
    public void stop() {
        if (mTimer != null)
            mTimer.cancel();
    }

    /**
     * this will run on messageRetransmissionRate
     */
    private void tick() {
        if(mMessage==null) {
            transmit(mTimeoutMessage);
            return;
        }

        transmit(mMessage);

        if(--mMessageRetransmissionNum==0) {
            mEvents.onRetransmissionEnded(mMessage);
            mMessage = null;
        }
    }

    /**
     * report that message should be transmited
     * @param message
     */
    private void transmit(byte [] message) {
        if(message!=null && message.length>1)
        mEvents.onTransmitMessage(message);
    }

    /**
     * this will drop messages. only one msg can be in queue at the moment
     */
    @Override
    public void onNewMessage(String name, float axis1, float axis2, float axis3, float axis4)
    {
        mMessageRetransmissionNum = Settings.getInstance().messageRetransmissionNum;
        boolean littleEndianByteOrder = Settings.getInstance().isEnableLittleEndianMessageByteOrder();
        mMessage = Message.generate(axis1, axis2, axis3, axis4, Message.Command.MANUAL, littleEndianByteOrder);
    }
}
