package pl.dawidurbanski.tcpgamepad.Connection;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dawid on 06.03.2016.
 */
public class Watchdog {

    private int mPatienceMS = 10*1000;//how long will dog wait for food
    private Timer watchdogTimer =null;

    public interface OnEvent { void run(); }
    private OnEvent mOnRunAway = null;

    public Watchdog(int patienceMS,OnEvent runAway) {
        mPatienceMS = patienceMS;
        mOnRunAway = runAway;
    }

    /*
     * you must feed the dog or he will run away!
     * this wil buy you a new dog if needed.
     */
    public void feed() {
        if (watchdogTimer != null)
            watchdogTimer.cancel();
        watchdogTimer = new Timer();
        watchdogTimer.schedule(new TimerTask() {
            @Override
            public void run() { runAway(); }
        }, mPatienceMS);
    }

    /*
     * watchdog will go to grandmother's farm
     */
    public  void kill() {
        if(watchdogTimer !=null)  watchdogTimer.cancel();
    }

    /*
     *you did not take good care of your watch dog for more than 'mPatienceMS' and now the dog is gone!
     */
    public void runAway()  {
        Log.e(this.getClass().getName(), "watch dog starved!");
        mOnRunAway.run();
    }

}
