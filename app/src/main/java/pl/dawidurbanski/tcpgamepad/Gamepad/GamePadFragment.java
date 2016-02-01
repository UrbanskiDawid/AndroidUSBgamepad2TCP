package pl.dawidurbanski.tcpgamepad.GamePad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.util.ArrayList;
import pl.dawidurbanski.tcpgamepad.R;
import pl.dawidurbanski.tcpgamepad.VirtualGamePad.VirtualGamePadFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GamePadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GamePadFragment extends Fragment  {

    private TextView mTextGamePadName;
    View mPadView;
    private ProgressBar mProgressBarLX,mProgressBarLY;
    private ProgressBar mProgressBarRX,mProgressBarRY;
    private ProgressBar mProgressBarDX,mProgressBarDY;

    private ToggleButton mTogleA,mTogleB,mTogleX,mTogleY;

    private ToggleButton mTogleL1,mTogleL2,mTogleR1,mTogleR2;
    private ToggleButton mTogleStart,mTogleSelect,mTogleCL,mTogleCR;

    private static GamepadLister gamepad = new GamepadLister();
    private GamepadInput gamepadInput = new GamepadInput();

    private Button mShowVirtualGamePadButton;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GamePadFragment.
     */
    public static GamePadFragment newInstance() {
        return new GamePadFragment();
    }

    private void showVirtualGamePad(Context context)
    {
        Intent myIntent = new Intent(context, VirtualGamePadFragment.class);
        startActivity(myIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =inflater.inflate(R.layout.fragment_game_pad, container, false);

        mPadView = rootView.findViewById(R.id.padView);

        mShowVirtualGamePadButton = (Button)rootView.findViewById(R.id.show_virtualGamepad);
        mShowVirtualGamePadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVirtualGamePad(v.getContext());
            }
        });

        mTextGamePadName =(TextView)rootView.findViewById(R.id.textGamepadName);
        mProgressBarLX  = initProgressbar(rootView,R.id.progressBarLX);
        mProgressBarLY  = initProgressbar(rootView,R.id.progressBarLY);

        mProgressBarRX  = initProgressbar(rootView,R.id.progressBarRX);
        mProgressBarRY  = initProgressbar(rootView,R.id.progressBarRY);

        mProgressBarDX  = initProgressbar(rootView,R.id.progressBarDpadX);
        mProgressBarDY  = initProgressbar(rootView,R.id.progressBarDpadY);

        mTogleA = initToggleButton(rootView, R.id.toggleButtonA);
        mTogleB = initToggleButton(rootView, R.id.toggleButtonB);

        mTogleX = initToggleButton(rootView, R.id.toggleButtonX);
        mTogleY = initToggleButton(rootView, R.id.toggleButtonY);

        mTogleL1 = initToggleButton(rootView, R.id.toggleButtonL1);
        mTogleL2 = initToggleButton(rootView, R.id.toggleButtonL2);

        mTogleR1 = initToggleButton(rootView, R.id.toggleButtonR1);
        mTogleR2 = initToggleButton(rootView, R.id.toggleButtonR2);

        mTogleStart = initToggleButton(rootView, R.id.toggleButtonStart);
        mTogleSelect = initToggleButton(rootView, R.id.toggleButtonSelect);

        mTogleCL = initToggleButton(rootView, R.id.toggleButtonCl);
        mTogleCR = initToggleButton(rootView, R.id.toggleButtonCR);

        updateGamePad();

        return rootView;
    }

    private ToggleButton initToggleButton(View view, int id)
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
        pBar.setProgress(50 + (int) (f * 100.0f));
    }

    private void updateToggleButton(ToggleButton button, boolean on)
    {
        button.setChecked(on);
    }

    private void updateGamePad()
    {
        ArrayList<InputDevice> gameControlers = gamepad.getGameControllers();
        if( gameControlers.size() == 0) {
            mPadView.setVisibility(View.INVISIBLE);
            mTextGamePadName.setText("GamePadList: no gamepad connected!");
        }else {
            mPadView.setVisibility(View.VISIBLE);
            mTextGamePadName.setText("GamePadList: " + gameControlers.get(0).getName());
        }

        updateProgressBarValue(mProgressBarLX, gamepadInput.gamepadAxis.leftControleStickX);
        updateProgressBarValue(mProgressBarLY, gamepadInput.gamepadAxis.leftControleStickY);
        updateProgressBarValue(mProgressBarRX, gamepadInput.gamepadAxis.rightControleStickX);
        updateProgressBarValue(mProgressBarRY, gamepadInput.gamepadAxis.rightControleStickY);
        updateProgressBarValue(mProgressBarDX, gamepadInput.gamepadAxis.dpadControleStickX);
        updateProgressBarValue(mProgressBarDY, gamepadInput.gamepadAxis.dpadControleStickY);

        updateToggleButton(mTogleA, gamepadInput.isKeyDown(GamepadInput.GamePadKey.A));
        updateToggleButton(mTogleB, gamepadInput.isKeyDown(GamepadInput.GamePadKey.B));
        updateToggleButton(mTogleX, gamepadInput.isKeyDown(GamepadInput.GamePadKey.X));
        updateToggleButton(mTogleY, gamepadInput.isKeyDown(GamepadInput.GamePadKey.Y));
        updateToggleButton(mTogleL1, gamepadInput.isKeyDown(GamepadInput.GamePadKey.L1));
        updateToggleButton(mTogleL2, gamepadInput.isKeyDown(GamepadInput.GamePadKey.L2));
        updateToggleButton(mTogleR1, gamepadInput.isKeyDown(GamepadInput.GamePadKey.R1));
        updateToggleButton(mTogleR2, gamepadInput.isKeyDown(GamepadInput.GamePadKey.R2));
        updateToggleButton(mTogleStart, gamepadInput.isKeyDown(GamepadInput.GamePadKey.START));
        updateToggleButton(mTogleSelect, gamepadInput.isKeyDown(GamepadInput.GamePadKey.SELECT));
        updateToggleButton(mTogleCL, gamepadInput.isKeyDown(GamepadInput.GamePadKey.THUMBL));
        updateToggleButton(mTogleCR, gamepadInput.isKeyDown(GamepadInput.GamePadKey.THUMBR));
    }

    public GamepadInput.GamePadAxis onGenericMotionEvent(MotionEvent event) {
        GamepadInput.GamePadAxis ret = gamepadInput.onGenericMotionEvent(event);
        if (ret!= null) updateGamePad();
        return ret;
    }

    public GamepadInput.GamePadKey onKey(int keyCode, KeyEvent event,boolean down) {
        GamepadInput.GamePadKey ret = gamepadInput.onKey(keyCode, event, down);
        if (ret != null) updateGamePad();
        return ret;
    }

}
