package pl.dawidurbanski.tcpgamepad.ADdrone;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by dawid on 08.05.2016.
 */
public class DebugData {

    /* messageFormat
            # example: ('$$$$', 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1000, 10, 10, 65535)
            # '<'         encoding = network = big endian
    # 0     '4s'        # preamble $$$$
    # 1,2,3 '3f'        # roll, pitch, yaw
    # 4,5,6 '3f'        # lat, lon, alt
    # 7     'f'         # speed
    # 8     'H'         # controllerState
    # 9     'BB'        # battery(tricky), flags(GPS fix | GPS 3D fix | low bat. vol. | nu | nu | nu | solver1 | solver2
    # 6     'H'         # crc
    */

    public static final byte [] preamble = {'$','$','$','$'};
    public static final int messageLen = 8;

    float roll, pitch, yaw;
    float lat, lon, alt;
    float speed;
    short controllerState;
    short battery;
    short flags;
    short CRC;

    DebugData(byte [] arr, boolean BigEndian) throws Exception {

        if(arr.length!=messageLen) throw new Exception("wrong input="+arr.length+". "+messageLen+"was expected.");

        ByteBuffer wrapped = ByteBuffer.wrap(arr);
        if(BigEndian)  wrapped.order(ByteOrder.BIG_ENDIAN);
        else           wrapped.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0;i<preamble.length;i++) {
            byte c = wrapped.get();
            if(c!=preamble[i]) throw new Exception("wrong preamble");
        }

        roll = wrapped.getFloat();
        pitch= wrapped.getFloat();
        yaw  = wrapped.getFloat();
        lat  = wrapped.getFloat();
        lon  = wrapped.getFloat();
        alt  = wrapped.getFloat();
        speed= wrapped.getFloat();
        controllerState=((short) (wrapped.get() & 0xff));
        battery=((short) (wrapped.get() & 0xff));
        flags=((short) (wrapped.get() & 0xff));
        CRC= ((short) (wrapped.get() & 0xff));
    }

    /*
    Convert debug data to easy to read string.
     */
    public String toString() {
        return
            "r/p/y: ("+roll+","+pitch+","+yaw  +"), "+
            "la/lo/al: ("+lat  +","+lon+","+alt  +"), "+
            "speed:"+speed+", "+
            "controllerState:"+controllerState+", "+
            "bat:"+battery+", "+
            "0b"+Integer.toBinaryString(0xFFFF & flags)+", "+
            "CRC:"+CRC;
    }
}
