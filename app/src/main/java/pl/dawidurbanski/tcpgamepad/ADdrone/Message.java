package pl.dawidurbanski.tcpgamepad.ADdrone;

import android.util.Base64;
import android.util.Log;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by Dawid on 24.01.2016.
 */
public class Message {

    /* Message prefix three of:
    Bin         Dec Hex Znak
    0010 0100	36	24	$
    */
   private static byte prefix  [] = {'$','$','$'};

    private static int messageLen = 32;

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
        return new byte[] {(byte) (crcShort & 0xff), (byte) ((crcShort >> 8) & 0xff)};
    }

    static public String byte2str(byte b,int width)
    {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    /*
     *BYTE:    0 1 | 2 3 | 4 5 | 6 7 8 9 | 10 11 12 13 | 14 15 16 17 | 18 19 20 21 | 22  23 24 25 | 26  27 28  29| 30 31 32
     *VALUE:        prefix     | axisX   |   axisY     |  throttle   |          not in use                       |   CRC 32
     *
     * prefix:   0010 0100 | 0010 0100 | 0010 0100
     */
   static public byte [] generate(float axis1,float axis2,float axis3) {

       ByteBuffer ret = ByteBuffer.allocate(messageLen);
       float zeroF = 0;

       ret.put(prefix[0]); //0-2 (2bytes)
       ret.put(prefix[1]); //2-3 (2bytes)
       ret.put(prefix[2]); //4-5 (2bytes)
       ret.putFloat(axis1);    //6-9   - (4bytes)
       ret.putFloat(axis2);    //10-13 - (4bytes)
       ret.putFloat(axis3);    //14-17 - (4bytes)
       ret.putFloat(zeroF);    //18-21 - (4bytes)
       ret.putFloat(zeroF);    //22-25 - (4bytes)
       ret.putFloat(zeroF);    //26-29 - (4bytes)
       byte [] crc =calculateCRC16(ret.array(), 0, ret.array().length);
       ret.put(crc);//29 - 23 CRC (3bytes)

       String strBin="",strInt="";
       for(byte b : ret.array())
       {
           strBin += byte2str(b,8)+",";
           strInt += ""+((int)b & 0xFF)+",";
       }
       Log.i("ADdroid", "message generated: '"+strBin+")");
       Log.i("ADdroid", "message generated: '"+strInt+")");
       Log.i("ADdroid", "message CRC: "+crc+")");
       return ret.array();
   }

    static public String generateString(float axis1,float axis2,float axis3) {
        byte array []  = generate(axis1,axis2,axis3);
        return Base64.encodeToString(array, Base64.DEFAULT);
    }
}
