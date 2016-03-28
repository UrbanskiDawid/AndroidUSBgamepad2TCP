package pl.dawidurbanski.tcpgamepad.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by dawid on 28.03.2016.
 */
public class OnBackKeyActions {

    public interface iActions {  void onDoublePress(); }

    private Context mContext;

    private iActions
            mDefaultAction = null,
            mCurrentAction = null;
    private String
            mDefaultMessage = "Please click BACK again to exit",
            mCurrentMessage = null;
    private boolean mRemoveAfterRun = true;//if true will return to default action

    private int mTimeTimeBetweenPressesMS =2000;//in milliseconds

    public OnBackKeyActions(int maxDelayMS,Context context)   {
        mContext = context;
        mTimeTimeBetweenPressesMS = maxDelayMS;
    }

    public void setDefaultAction(String message,iActions actions)  {
        if(message!=null)  mDefaultMessage = message;
        mDefaultAction = actions;
    }

    public void setCustomAction(String message, iActions actions,boolean returnToDefaultActionAfterRun) {
        Log.d("OnBackKeyActions","setCustomAction");
        mCurrentMessage= message;
        mCurrentAction = actions;
        mRemoveAfterRun= returnToDefaultActionAfterRun;
    }

    public void clearOneTime(){
        setCustomAction(null,null,false);
    }

    private long lastPressedTime = 0;
    public void onPress() {

        long timeNow = System.currentTimeMillis();
        long timeD = timeNow - lastPressedTime;
        lastPressedTime=timeNow;

        if(timeD<= mTimeTimeBetweenPressesMS) {
            runOnPressTwice();
            if(mRemoveAfterRun)  clearOneTime();
            lastPressedTime = 0;
            return;
        }

        runOnPressOnce();
    }

    private void runOnPressTwice() {
        if(mCurrentAction!=null)
        {
            mCurrentAction.onDoublePress();
            return;
        }

        if(mDefaultAction!=null)
        {
            mDefaultAction.onDoublePress();
            return;
        }
    }

    private void runOnPressOnce() {
        if(mCurrentMessage!=null)
        {
            Toast.makeText(mContext, mCurrentMessage, Toast.LENGTH_SHORT).show();
            return;
        }
        if(mDefaultMessage!=null)
        {
            Toast.makeText(mContext, mDefaultMessage, Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
