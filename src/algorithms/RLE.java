package algorithms;

import java.util.ArrayList;

public class RLE implements Compression {
    @Override
    public byte[] compress(byte[] input) {
        ArrayList<Byte> counterList = new ArrayList<>();
        int it = 1;
        byte j = 1;
        while (it < input.length) {
            if (input[it] == input[it - 1] && j < 127) {
                j++;
                it++;
            } else {
                counterList.add(j);
                it++;
                j = 1;
            }
        }
        counterList.add(j);
        ArrayList<Byte> dataList = new ArrayList<>();
        int current = 0;
        int k = 0;
        while (k < counterList.size()) {
            byte count = 0;
            while (k < counterList.size() && counterList.get(k) == 1 && count < 127) {
                count++;
                k++;
            }
            if (count != 0) {
                dataList.add(count);
                for (int l = 0; l < count; l++)
                    dataList.add(input[current + l]);
                current += count;
            } else {
                dataList.add((byte) (counterList.get(k) + 128));
                dataList.add(input[current]);
                current += counterList.get(k++);
            }
        }
        byte[] compressedArray = new byte[dataList.size()];
        for (int i = 0; i < compressedArray.length; i++)
            compressedArray[i] = dataList.get(i);
        return compressedArray;
    }

    @Override
    public byte[] decompress(byte[] input) {
        ArrayList<Byte> dataList = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            if (input[i] > 0) {
                byte count = input[i];
                while (count > 0) {
                    dataList.add(input[++i]);
                    count--;
                }
            } else if (input[i] <= 0) {
                byte count = (byte) (input[i++] - 128);
                while (count >= 1) {
                    dataList.add(input[i]);
                    count--;
                }
            }
        }
        byte[] decompressedArray = new byte[dataList.size()];
        for (int i = 0; i < dataList.size(); i++)
            decompressedArray[i] = dataList.get(i);
        return decompressedArray;
    }
}