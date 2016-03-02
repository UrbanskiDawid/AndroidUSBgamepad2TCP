package pl.dawidurbanski.tcpgamepad.ADdrone;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Dawid on 24.01.2016.
 *
 * 38 bytes long message:
 * BYTE:    0  1  2  3 |  4  5  6  7 |  8  9 10 11 | 12 13 14 15 | 16 17 18 19 | 20 21 | 22  | 23 24 25 26 27 28 29 30 31 32 33 34 35 | 36 37
 * VALUE:     prefix   |     roll    |    pitch    |     yaw     |   throttle  |  cmd  | SMS |      not in use                        |  CRC
 */
public class Message {

    /*
    Length of message in bytes.
     */
    private static int messageLen = 38;

    /* Message prefix four of:
    Bin         Dec Hex Char
    0010 0100	36	24	$
    */
    private static byte prefix  [] = {'$','$','$','$'};

    /* commandManual ??
     */
    private static short commandManual = 1000;

    /* solverModeStabilization ??
    */
    private static byte solverModeStabilization = 10;

    /* not in use part
    13 bytes. of 255
    Bin          Dec Hex
    1111 1111    255 FF
    */
    private static byte [] notInUse = {
            (byte)255,(byte)255, (byte)255,(byte)255,(byte)255,
            (byte)255,(byte)255, (byte)255,(byte)255,(byte)255,
            (byte)255,(byte)255, (byte)255
    };

    /*
    Calculate CRC for given array 2bytes
     */
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
     generate message as byte array
     */
    static public byte [] generate(float roll, float pitch, float yaw, float throttle) {

        ByteBuffer ret = ByteBuffer.allocate(messageLen);
        ret.order(ByteOrder.LITTLE_ENDIAN);

        ret.put(prefix);                  // 0- 3 prefix
        ret.putFloat(roll);               // 4- 7 roll
        ret.putFloat(pitch);              // 8-11 pitch
        ret.putFloat(yaw);                //12-15 yaw
        ret.putFloat(throttle);           //16-19 throttle
        ret.putShort(commandManual);      //20-21 commandManual
        ret.put(solverModeStabilization); //22
        ret.put(notInUse);                //23-35

        // CRC calculated only from payload data
        byte [] crc = calculateCRC16(ret.array(), 4, 31);
        ret.put(crc);                     //36-37 CRC

        return ret.array();
    }

    /*
    Convert byte array to unsigned int array.
    */
    static public String byteArrayAsInts(byte [] message,int from,int to) {
        String ret = "";
        for(int i=from;i<to;i++){
            int I = ((int) message[i] & 0xFF);
            ret += "" + String.format("%03d", I) + ",";
        }
        if(ret.endsWith(","))  ret=ret.substring(0,ret.length()-1);//remove last ','
        return ret;
    }

    /*
    Convert message to easy to read string.
     */
    static public String toStringAsInts(byte [] message)   {
        if(message.length!=messageLen) return"";
        return  byteArrayAsInts(message, 0, 4)//prefix
          +"|"+ byteArrayAsInts(message, 4, 8)//roll
          +"|"+ byteArrayAsInts(message, 8,12)//pitch
          +"|"+ byteArrayAsInts(message,12,16)//yaw
          +"|"+ byteArrayAsInts(message,16,20)//throttle
          +"|"+ byteArrayAsInts(message,20,22)//cmd
          +"|"+ byteArrayAsInts(message,22,23)//SMS
          +"|"+ byteArrayAsInts(message,23,36)//not in use
          +"|"+ byteArrayAsInts(message,36,38)//CRC
          ;
    }

    /*
    Convert message to binary string
     */
    static public String toStringAsBinary(byte [] message)   {
        String ret = "";
        for (byte b : message) {
            ret += "" +  String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0') + ",";
        }
        return ret;
    }
}
