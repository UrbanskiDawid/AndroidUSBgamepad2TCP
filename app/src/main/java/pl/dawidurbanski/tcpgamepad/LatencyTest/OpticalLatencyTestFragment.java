package pl.dawidurbanski.tcpgamepad.LatencyTest;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import pl.dawidurbanski.tcpgamepad.ADdrone.Message;
import pl.dawidurbanski.tcpgamepad.R;


/**
 * Created by dawid on 20.03.2016.
 */
public class OpticalLatencyTestFragment extends DialogFragment implements View.OnClickListener {

    private Message.ADdroneMessageInterface mListener = null;

    public static void popup(FragmentManager fm /*getSupportFragmentManager*/) {
        DialogFragment dialog = new OpticalLatencyTestFragment();
        dialog.show(fm, "OpticalLatencyTestFragment");
    }

    private TextView mTextView;
    private FrameLayout mFrame;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Message.ADdroneMessageInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement '" + Message.ADdroneMessageInterface.class.getName() + "'");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_optical_latency_test, container, false);
        v.setOnClickListener(this);

        mTextView = (TextView) v.findViewById(R.id.MainTestString);
        if(mTextView==null) Log.e("OpticalLatencyTest","cant find textView!");

        mFrame = (FrameLayout) v.findViewById(R.id.MatinTestFrame);
        if(mFrame==null) Log.e("OpticalLatencyTest","cant find FrameLayout!");

        reset();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.app_name) + ":" + "Test latency");
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("OpticalLatencyTest","onDestroy");
        if(started)
            reset();
    }

    boolean started = false;
    Timer timer = null;

    @Override
    public void onClick(View v) {
        if (started)   return;

        started=true;

        if (timer != null)
            timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, 0, tickTime);
    }

    void setText(final String str,final int textColor,final int BGcolor) {
        FragmentActivity fa = getActivity();
        if(fa==null){
            Log.e("OpticalLatencyTest","setText failed: missing fragmentActivity.");
            return;
        }
        fa.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setTextColor(textColor);
                mTextView.setText(str);
                mFrame.setBackgroundColor(BGcolor);
            }
        });
    }

    void reset() {
        setText(getString(R.string.OpticalLatencyTestFragment_prompt),Color.WHITE,Color.BLACK);
        started=false;
        if(timer!=null) timer.cancel();
    }

    private int tickTime = 1000;
    private int tickCooldown = 5;//cooldown in ticks colldown time (tickTime*tickCooldown)
    private static final int tickCountMax=5;
    private int tickCount= 0;
    void tick() {
        tickCount++;
        Log.d("OpticalLatencyTest","tick"+tickCount);

        if(tickCount<tickCountMax)
        {
            setText(""+(tickCountMax-tickCount),Color.WHITE,Color.BLACK);
        }
        if(tickCount == tickCountMax)
        {
            Log.d("OpticalLatencyTest","sending message");
            setText("GO",Color.BLACK,Color.GREEN);
            mListener.sendMessage("test",0.0f,0.0f,0.0f,0.0f);
        }
        if(tickCount >= tickCountMax+tickCooldown)
        {
            timer.cancel();
            tickCount=0;
            Log.d("OpticalLatencyTest","sending stop");
            reset();
        }
    }
}
