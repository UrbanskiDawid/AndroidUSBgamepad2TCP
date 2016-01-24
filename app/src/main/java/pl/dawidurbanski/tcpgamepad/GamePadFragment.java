package pl.dawidurbanski.tcpgamepad;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GamePadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GamePadFragment extends Fragment {

    private TextView mTextGamepadName;
    private ProgressBar mProgressBarLX,mProgressBarLY;
    private ProgressBar mProgressBarRX,mProgressBarRY;
    private ProgressBar mProgressBarDX,mProgressBarDY;

    private ToggleButton mTogleA,mTogleB,mTogleX,mTogleY;

    private ToggleButton mTogleL1,mTogleL2,mTogleR1,mTogleR2;
    private ToggleButton mTogleStart,mTogleSelect,mTogleCL,mTogleCR;

    private static Gamepad gamepad = new Gamepad();
    private GamepadInput gamepadInput = new GamepadInput();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GamePadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GamePadFragment newInstance() {
        return new GamePadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.fragment_game_pad, container, false);

        mTextGamepadName=(TextView)rootView.findViewById(R.id.textGamepadName);
        mProgressBarLX  =initProgressbar(rootView,R.id.progressBarLX);
        mProgressBarLY  =initProgressbar(rootView,R.id.progressBarLY);

        mProgressBarRX  =initProgressbar(rootView,R.id.progressBarRX);
        mProgressBarRY  =initProgressbar(rootView,R.id.progressBarRY);

        mProgressBarDX  =initProgressbar(rootView,R.id.progressBarDpadX);
        mProgressBarDY  =initProgressbar(rootView,R.id.progressBarDpadY);

        mTogleA = initTogleButton(rootView, R.id.toggleButtonA);
        mTogleB = initTogleButton(rootView, R.id.toggleButtonB);

        mTogleX = initTogleButton(rootView, R.id.toggleButtonX);
        mTogleY = initTogleButton(rootView, R.id.toggleButtonY);

        mTogleL1 = initTogleButton(rootView,R.id.toggleButtonL1);
        mTogleL2 = initTogleButton(rootView,R.id.toggleButtonL2);

        mTogleR1 = initTogleButton(rootView,R.id.toggleButtonR1);
        mTogleR2 = initTogleButton(rootView,R.id.toggleButtonR2);

        mTogleStart = initTogleButton(rootView,R.id.toggleButtonStart);
        mTogleSelect = initTogleButton(rootView,R.id.toggleButtonSelect);

        mTogleCL = initTogleButton(rootView,R.id.toggleButtonCl);
        mTogleCR = initTogleButton(rootView,R.id.toggleButtonCR);

        updateGamepad();

        return rootView;
    }
    private ToggleButton initTogleButton(View view,int id)
    {
        ToggleButton ret=(ToggleButton)view.findViewById(id);
        ret.setFocusable(false);
        ret.setEnabled(false);
        CharSequence str = ret.getText();
        ret.setTextOff(str);
        ret.setTextOn(str);
        return ret;
    }


    private ProgressBar initProgressbar(View view,int id)
    {
        ProgressBar ret=(ProgressBar)view.findViewById(id);
        ret.setProgress(0);
        ret.setMax(100);
        ret.setProgress(50);
        return ret;
    }

    // -1.0f - 0.0f - 1.0f convert to form 0 to 100 range
    private void updateProgressBarValue(ProgressBar pBar, float f)
    {
        float lCX = gamepadInput.gamepadAxis.leftControleStickX;
        pBar.setProgress( 50+(int)(f*100.0f) );
    }

    private void updateTogleButton(ToggleButton button, boolean on)
    {
        button.setChecked(on);
    }

    private void updateGamepad()
    {
        ArrayList<InputDevice> gameControlers = gamepad.getGameControllers();
        if( gameControlers.size() == 0) {
            mTextGamepadName.setText("Gamepad: no gamepad connected!");
        }else {
            mTextGamepadName.setText("Gamepad: " + gameControlers.get(0).getName());
        }

        updateProgressBarValue(mProgressBarLX, gamepadInput.gamepadAxis.leftControleStickX);
        updateProgressBarValue(mProgressBarLY, gamepadInput.gamepadAxis.leftControleStickY);
        updateProgressBarValue(mProgressBarRX, gamepadInput.gamepadAxis.rightControleStickX);
        updateProgressBarValue(mProgressBarRY, gamepadInput.gamepadAxis.rightControleStickY);
        updateProgressBarValue(mProgressBarDX, gamepadInput.gamepadAxis.dpadControleStickX);
        updateProgressBarValue(mProgressBarDY, gamepadInput.gamepadAxis.dpadControleStickY);

        updateTogleButton(mTogleA, gamepadInput.isKeyDown(GamepadInput.GamepadKey.A));
        updateTogleButton(mTogleB, gamepadInput.isKeyDown(GamepadInput.GamepadKey.B));
        updateTogleButton(mTogleX, gamepadInput.isKeyDown(GamepadInput.GamepadKey.X));
        updateTogleButton(mTogleY, gamepadInput.isKeyDown(GamepadInput.GamepadKey.Y));
        updateTogleButton(mTogleL1, gamepadInput.isKeyDown(GamepadInput.GamepadKey.L1));
        updateTogleButton(mTogleL2, gamepadInput.isKeyDown(GamepadInput.GamepadKey.L2));
        updateTogleButton(mTogleR1, gamepadInput.isKeyDown(GamepadInput.GamepadKey.R1));
        updateTogleButton(mTogleR2, gamepadInput.isKeyDown(GamepadInput.GamepadKey.R2));
        updateTogleButton(mTogleStart, gamepadInput.isKeyDown(GamepadInput.GamepadKey.START));
        updateTogleButton(mTogleSelect, gamepadInput.isKeyDown(GamepadInput.GamepadKey.SELECT));
        updateTogleButton(mTogleCL, gamepadInput.isKeyDown(GamepadInput.GamepadKey.THUMBL));
        updateTogleButton(mTogleCR, gamepadInput.isKeyDown(GamepadInput.GamepadKey.THUMBR));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public GamepadInput.GamepadAxis onGenericMotionEvent(MotionEvent event) {
        GamepadInput.GamepadAxis ret = gamepadInput.onGenericMotionEvent(event);
        if (ret!= null) updateGamepad();
        return ret;
    }

    public GamepadInput.GamepadKey onKey(int keyCode, KeyEvent event,boolean down) {
        GamepadInput.GamepadKey ret = gamepadInput.onKey(keyCode, event, down);
        if (ret != null) updateGamepad();
        return ret;
    }

}
