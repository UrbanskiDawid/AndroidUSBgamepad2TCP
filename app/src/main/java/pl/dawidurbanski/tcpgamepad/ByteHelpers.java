package pl.dawidurbanski.tcpgamepad;

import java.util.ArrayList;

/**
 * Created by dawid on 08.05.2016.
 */
public class ByteHelpers {

    public static int byteToInt(byte b) {
        return ((int) b & 0xFF);
    }

    public static short byteToShort(byte b) {
        return ((short) (b & 0xff));
    }

    public static String byteToHexString(byte b) {
        String ret = "";
        int intVal = b & 0xff;
        if (intVal < 0x10) ret += "0";
        ret += Integer.toHexString(intVal);
        return ret;
    }

    public static String byteToIntString(byte b) {
        int I = ((int) b & 0xFF);
        return String.format("%03d", I);
    }

    public static boolean ByteArrayStartsWith(ArrayList<Byte> ar, byte[] prefix) {
        if(prefix.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (ar.get(i) != prefix[i])
                return false;
        }
        return true;
    }

    public static String ByteArrayToHexString(byte[] in) {
        String ret="";
        for(byte b:in) { ret+=byteToHexString(b); }
        return ret;
    }

    public static String ByteToBinString(byte b) {
        return "0b" + String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
}
