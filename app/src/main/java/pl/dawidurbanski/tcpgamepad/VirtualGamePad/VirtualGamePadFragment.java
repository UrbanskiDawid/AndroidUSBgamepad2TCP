package pl.dawidurbanski.tcpgamepad.VirtualGamePad;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.dawidurbanski.tcpgamepad.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VirtualGamePadFragment extends Fragment {

    VirtualGamePadKnobView
            mAnalogL,
            mAnalogR;

    public interface OnEvent { void onMove(float x,float y,float a,float b); }
    public OnEvent onMove=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView =inflater.inflate(R.layout.activity_game_pad_virtual, container, false);

        mAnalogL = (VirtualGamePadKnobView)rootView.findViewById(R.id.fullscreen_contentL);
        mAnalogL.init("left");
        mAnalogL.disableKnoxXReset();
        mAnalogL.onMove = new VirtualGamePadKnobView.OnEvent() {
            @Override
            public void onMove(float x, float y) {
                if(onMove!=null)
                    onMove.onMove(x,y, mAnalogR.xPos,mAnalogR.yPos);
            }
        };

        mAnalogR = (VirtualGamePadKnobView)rootView.findViewById(R.id.fullscreen_contentR);
        mAnalogR.init("right");
        mAnalogR.onMove = new VirtualGamePadKnobView.OnEvent() {
            @Override
            public void onMove(float x, float y) {
                if(onMove!=null)
                    onMove.onMove(mAnalogL.xPos,mAnalogL.xPos,x,y);
            }
        };

        return rootView;
    }
}
