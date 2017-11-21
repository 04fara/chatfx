package algorithms;

import java.util.*;

class ParityCheck implements Encoding {
    @Override
    public byte[] encode(byte[] byteArray) {
        byte[] res = new byte[byteArray.length * 2]; //creating result array
        for (int i = 0; i < byteArray.length; i++) {
            BitSet tmp = encodeByte(byteArray[i]);
            byte[] x = tmp.toByteArray();
            //adding data
            res[i * 2] = x[0];
            //adding byte which contains control bit
            res[i * 2 + 1] = x[1];
        }
        return res;
    }

    @Override
    public byte[] decode(byte[] byteArray) {
        int numErr = 0, maxErr = 10;
        byte[] res = new byte[byteArray.length / 2];
        for (int i = 0; i < res.length; i++) {
            byte[] tmp = new byte[2];
            tmp[0] = byteArray[i * 2];
            tmp[1] = byteArray[i * 2 + 1];
            BitSet x = BitSet.valueOf(tmp);
            res[i] = tmp[0];
            //if data was damaged
            if (!check(x)) numErr++;
        }
        if (numErr < maxErr) return res;
        else return null; // if too much errors - return null
    }

    private BitSet encodeByte(byte b) {
        BitSet in = BitSet.valueOf(new byte[]{b});
        BitSet res = new BitSet(16); //creating resulting BitSet
        boolean an = false;
        for (int i = 0; i < 8; i++) {
            res.set(i, in.get(i));
            //calculating xor-sum
            an ^= in.get(i);
        }
        res.set(8, an); //adding control bit
        res.set(12, true); //fix, cause if control bit is zero - res doesn't create the second Byte
        return res;
    }

    private boolean check(BitSet in) {
        boolean res = false;
        for (int i = 0; i < 8; i++)
            //calculating xor-sum
            res ^= in.get(i);
        return res == in.get(8); //if control bit equals xor-sum
    }
}