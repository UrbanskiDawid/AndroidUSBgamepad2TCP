package pl.dawidurbanski.tcpgamepad.ADdrone;

import android.util.Base64;
import android.util.Log;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * Created by Dawid on 24.01.2016.
 */
public class Message {

    /* Message prefix four of:
    Bin         Dec Hex Char
    0010 0100	36	24	$
    */
    private static byte prefix  [] = {'$','$','$','$'};

    private static byte dummy  = 0;
    private static short commandManual = 1000;
    private static byte solverModeStabilization = 10;

    /* not in use part
    Bin          Dec Hex
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    1111 1111    255 FF
    */
    private static byte [] notInUse = {
            (byte)255,(byte)255, (byte)255,(byte)255,(byte)255,
            (byte)255,(byte)255, (byte)255,(byte)255,(byte)255
    };

    private static int messageLen = 38;

    //return 2bytes
    public static byte[] calculateCRC16(byte[] buff, int start, int end) {
        int crcShort = 0;
        for (int i = start; i < end; i++) {
            crcShort = ((crcShort  >>> 8) | (crcShort  << 8) )& 0xffff;
            crcShort ^= (buff[i] & 0xff);
            crcShort ^= ((crcShort & 0xff) >> 4);
            crcShort ^= (crcShort << 12) & 0xffff;
            crcShort ^= ((crcShort & 0xFF) << 5) & 0xffff;
        }
        crcShort &= 0xffff;

        return new byte[] {
                (byte) (crcShort & 0xff),
                (byte) ((crcShort >> 8) & 0xff)
        };
    }

    /*
     *BYTE:    0  1  2  3 |  4  5  6  7 |  8  9 10 11 | 12 13 14 15 | 16 17 18 19 | 20 21 | 22 23 | 24 25 | 26 27 | 28 29 | 30  31
     *VALUE:     prefix   |     axis1   |    axis2    |   axis3     |    axis4    |          not in use                   |   CRC
     */
    static public byte [] generate(float roll, float pitch, float yaw, float throttle) {

       ByteBuffer ret = ByteBuffer.allocate(messageLen);
       ret.order(ByteOrder.BIG_ENDIAN);

        ret.put(prefix);
        ret.putFloat(roll);    // 0 - 3
        ret.putFloat(pitch);    // 4 - 7
        ret.putFloat(yaw);    // 8 - 11
        ret.putFloat(throttle);    // 12 - 15
        ret.putShort(commandManual); // 16 - 17
        ret.put(solverModeStabilization); // 18
        // rest of packet is dummy
        for (int i = 0; i < 32 - 18 - 1; i++)
        {
            ret.put(dummy);
        }

        // CRC calculated only from payload data
        byte [] crc = calculateCRC16(ret.array(), 4, 31);
        ret.put(crc);

       return ret.array();
    }

    static public String toString(byte [] message)   {
        return Base64.encodeToString(message, Base64.DEFAULT);
    }

    static public String byteArrayAsInts(byte [] message,int from,int to)   {
        String ret = "";
        for(int i=from;i<to;i++){
            int I = ((int) message[i] & 0xFF);
            ret += "" + String.format("%03d", I) + ",";
        }
        if(ret.endsWith(","))  ret=ret.substring(0,ret.length()-1);//remove last ','
        return ret;
    }

    static public String toStringAsInts(byte [] message)   {
        if(message.length!=32) return"";
        return
                byteArrayAsInts(message, 0, 4)//prefix
          +"|"+ byteArrayAsInts(message, 4, 8)//axis1
          +"|"+ byteArrayAsInts(message, 8,12)//axis2
          +"|"+ byteArrayAsInts(message,12,16)//axis3
          +"|"+ byteArrayAsInts(message,16,20)//axis4
          +"|"+ byteArrayAsInts(message,20,30)//notInUse
          +"|"+ byteArrayAsInts(message,30,32);//CRC
    }

    static public String toStringAsBinary(byte [] message)   {
        String ret = "";
        for (byte b : message) {
            ret += "" +  String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0') + ",";
        }
        return ret;
    }
}
