package pl.dawidurbanski.tcpgamepad.Gamepad;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.InputDevice;

import java.util.ArrayList;

/**
 * Created by Dawid on 23.01.2016.
 */
public class Gamepad {

    private static ArrayList<InputDevice> devices = new ArrayList<>();

    public ArrayList<InputDevice> getGameControllers()
    {
        devices = new ArrayList<>();

        for (int deviceId : InputDevice.getDeviceIds()) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if ( ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    ||
                 ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
               )
            {
                // This device is a game controller. Store its device ID.
                devices.add(dev);
                Log.w("dawid", "found gamepad: " + dev.getName());
            }
        }
        return devices;
    }
}
