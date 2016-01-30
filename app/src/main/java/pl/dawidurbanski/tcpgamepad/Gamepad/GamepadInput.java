package pl.dawidurbanski.tcpgamepad.GamePad;

import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dawid on 23.01.2016.
 *
 * http://developer.android.com/training/game-controllers/controller-input.html
 */
public class GamePadInput {

    //SUPPORTED KEYS
    public enum GamePadKey
    {
        START,
        SELECT,
        X,
        Y,
        A,
        B,
        L1,
        R1,
        L2,
        R2,
        THUMBL,
        THUMBR
    }

    //true = down
    public boolean KeysStatus[];

    //SUPPORTED AXIS
    public class GamePadAxis
    {
        public float leftControleStickX=0f,  leftControleStickY=0f;
        public float rightControleStickX=0f, rightControleStickY=0f;
        public float dpadControleStickX=0f,  dpadControleStickY=0f;

        public String toString()
        {
            return
              String.format("%+.01f ", leftControleStickX)+ ","
            + String.format("%+.01f ", leftControleStickY)+ " "
            + " "
            + String.format("%+.01f ", rightControleStickX)+ ","
            + String.format("%+.01f ", rightControleStickY)+ " "
            + " "
            + String.format("%+.01f ", dpadControleStickX)+ ","
            + String.format("%+.01f ", dpadControleStickY)+ " ";
        }
    }

    public GamePadAxis gamepadAxis = new GamePadAxis();

    //map key code to GamePadKey
    HashMap<Integer,GamePadKey> keysMap = new HashMap<>();

    public GamePadInput()
    {
        keysMap.put(KeyEvent.KEYCODE_BUTTON_START, GamePadKey.START);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_SELECT, GamePadKey.SELECT);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_X, GamePadKey.X);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_Y, GamePadKey.Y);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_A, GamePadKey.A);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_B, GamePadKey.B);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_L1, GamePadKey.L1);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_R1, GamePadKey.R1);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_L2, GamePadKey.L2);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_R2, GamePadKey.R2);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_THUMBL, GamePadKey.THUMBL);
        keysMap.put(KeyEvent.KEYCODE_BUTTON_THUMBR, GamePadKey.THUMBR);

        KeysStatus = new boolean[GamePadKey.values().length];
        for( int i=0; i<KeysStatus.length;i++)
            KeysStatus[i]=false;
    }

    interface KeyListener {  void onKey(GamePadKey key,boolean down);    }
    private List<KeyListener> keylisteners = new ArrayList<>();
    public void addOnKeyListener(KeyListener toAdd) { keylisteners.add(toAdd);    }

    interface AxisListener {  void onMove(GamePadAxis axis);    }
    private List<AxisListener> axisListeners = new ArrayList<>();
    public void addOnAxisListener(AxisListener toAdd) {    axisListeners.add(toAdd);  }

    public GamePadKey onKey(int keyCode, KeyEvent event,boolean down)
    {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD)
            return null;
        if (event.getRepeatCount() != 0)
            return null;

        GamePadKey key = keysMap.get(keyCode);
        if(key == null) {
            Log.w(GamePadInput.class.getName(),"KEY <UNKNOWN>"+keyCode);
            return null;
        }

        KeysStatus[key.ordinal()]=down;// pressed released

        for (KeyListener kl : keylisteners)
          kl.onKey(key,down);

        return key;
    }

    public boolean isKeyDown(GamePadKey key)
    {
        return KeysStatus[key.ordinal()];
    }

    public boolean isKeyUp(GamePadKey key)
    {
        return !KeysStatus[key.ordinal()];
    }

    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null)
        {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
    private GamePadAxis processJoystickInput(MotionEvent event,    int historyPos)
    {
        InputDevice mInputDevice = event.getDevice();

        // Update the ship object based on the new x and y values
        gamepadAxis.leftControleStickX =getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
        gamepadAxis.leftControleStickY =getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);

        gamepadAxis.rightControleStickX=getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        gamepadAxis.rightControleStickY=getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);

        gamepadAxis.dpadControleStickX =getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        gamepadAxis.dpadControleStickY =getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);

        for (AxisListener al : axisListeners)
            al.onMove(gamepadAxis);

        return gamepadAxis;
    }

    public GamePadAxis onGenericMotionEvent(MotionEvent event)  {

        // Check that the event came from a game controller
        if (   (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                && event.getAction() == MotionEvent.ACTION_MOVE  )
        {
            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1);
            return gamepadAxis;
        }
        return null;
    }
}
