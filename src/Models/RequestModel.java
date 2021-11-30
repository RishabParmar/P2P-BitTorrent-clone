package Models;

import Utils.Enums;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class RequestModel<T> {
    public int messageLength;
    public byte type;
    public byte[] payload;

    public RequestModel(Enums.MessageTypes e, T o) {
        // Type:
        this.type = getType(e);
        this.payload = getPayload(e, o);
        this.messageLength = getMessageLength();
    }

    private byte getType(Enums.MessageTypes e) {
//        BitSet bitSet = new BitSet(8);
//        bitSet.set(e.getValue(), true);
//        return  bitSet.toByteArray()[0];
        byte type = (byte)e.getValue();
        int x = type;
        System.out.println(type + " " + x);
        return type;
    }

    private byte[] getPayload(Enums.MessageTypes e, T o) {
        byte[] res = null;
        try{
            if(e == Enums.MessageTypes.BITFIELD) {
                boolean[] bitfield = (boolean[]) o;
                BitSet bitset = new BitSet(bitfield.length);
                System.out.println("bitsetlen: " + bitset.size());
                System.out.println("size: " + bitfield.length + " " + bitset.size());
                for(int i = 0; i < bitset.size(); i++) {
                    if(bitfield[i]) bitset.set(i, true);
                }
                res = bitset.toByteArray();
                System.out.println("res: " + res.length);
            } else if(e == Enums.MessageTypes.REQUEST) {
                res = ByteBuffer.allocate(4).putInt((int)o).array();
            }
        } catch (Exception ee) {
            System.out.println("1234 " + ee);
        }
        return res;
        //  ---------------------- Handle piece content !!!! --------------------------------
        //return SerializationUtils.serialize((Serializable) o);
    }

    private int getMessageLength() {
        // this.payload.length will give number of bytes and 1 byte for type and 4 bytes for messageLength itself
        return this.payload != null ? this.payload.length + 5 : 5;
    }

    public byte[] getBytes() {
        System.out.println("messageLength: " + getMessageLength());
        byte[] res = new byte[getMessageLength()];
        try {
            byte[] messageLengthBytes = ByteBuffer.allocate(4).putInt(this.messageLength).array();
            for(int i = 0; i < res.length; i++) {
                if(i < 4) res[i] = messageLengthBytes[i];
                else if(i == 5) res[i] = this.type;
                else res[i] = this.payload[i-4];
            }
        } catch (Exception e) {
            System.out.println("asdfgh" + e);
        }
        return res;
    }

}