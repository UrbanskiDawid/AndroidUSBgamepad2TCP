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

    private static int messageLen = 32;

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

        byte [] ret =new byte[] {
                (byte) (crcShort & 0xff),
                (byte) ((crcShort >> 8) & 0xff)
        };
        Log.e("CRC", byteArrayAsInts(ret,0,2));
        return ret;
    }

    /*
     *BYTE:    0  1  2  3 |  4  5  6  7 |  8  9 10 11 | 12 13 14 15 | 16 17 18 19 | 20 21 | 22 23 | 24 25 | 26 27 | 28 29 | 30  31
     *VALUE:     prefix   |     axis1   |    axis2    |   axis3     |    axis4    |          not in use                   |   CRC
     */
    static public byte [] generate(float axis1,float axis2,float axis3,float axis4) {

       ByteBuffer ret = ByteBuffer.allocate(messageLen);
       ret.order(ByteOrder.BIG_ENDIAN);

       ret.put(prefix);        // 0- 3 - (8bytes)
       ret.putFloat(axis1);    // 4- 7 - (4bytes)
       ret.putFloat(axis2);    // 8-11 - (4bytes)
       ret.putFloat(axis3);    //12-15 - (4bytes)
       ret.putFloat(axis4);    //16-19 - (4bytes)
       ret.put(notInUse);      //24-29 - (6bytes)
       ret.put(calculateCRC16(ret.array(), 0, ret.array().length)); //29 - 23 CRC (3bytes)

       return ret.array();
    }

    static public String toString(byte [] message)   {
        return Base64.encodeToString(message, Base64.DEFAULT);
    }

    static public String byteArrayAsInts(byte [] message,int from,int to)   {
        String ret = "";
        for(int i=from;i<to;i++){  //for (byte b : message) {
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
