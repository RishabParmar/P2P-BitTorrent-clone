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
        BitSet bitSet = new BitSet(8);
        bitSet.set(e.getValue(), true);
        return  bitSet.toByteArray()[0];
    }

    private byte[] getPayload(Enums.MessageTypes e, T o) {
        if(e == Enums.MessageTypes.BITFIELD) {
            boolean[] bitfield = (boolean[]) o;
            BitSet bitset = new BitSet(bitfield.length);
            for(int i = 0; i < bitset.size(); i++) {
                if(bitfield[i]) bitset.set(i, true);
            }
            return bitset.toByteArray();
        }
        //  ---------------------- Handle piece content !!!! --------------------------------
        return SerializationUtils.serialize((Serializable) o);
    }

    private int getMessageLength() {
        // this.payload.length will give number of bytes and 1 byte for type and 4 bytes for messageLength itself
        return this.payload.length + 5;
    }

    public byte[] getBytes() {
        byte[] res = new byte[getMessageLength()];
        byte[] messageLengthBytes = ByteBuffer.allocate(4).putInt(this.messageLength).array();
        for(int i = 0; i < res.length; i++) {
            if(i < 4) res[i] = messageLengthBytes[i];
            else if(i == 5) res[i] = this.type;
            else res[i] = this.payload[i-5];
        }
        return res;
    }
}