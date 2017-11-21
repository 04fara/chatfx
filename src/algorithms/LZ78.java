package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LZ78 implements Compression {
    //Save dictionary as Prefix Tree
    private class Node {
        private Map<Integer, Node> map = new HashMap<>(); //children of current node
        private int bit;
        private Node parent; //parent Node
        private long nodeNum = 0; //Node number

        //constructor with given Nobe number
        Node(int num) {
            this.parent = null;
            this.nodeNum = num;
        }

        //constructor with given parent, bit and number
        private Node(Node par, int b, int num) {
            this.parent = par;
            this.bit = b;
            this.nodeNum = num;
        }

        //add child to current Node with given bit and number
        void addChild(int b, int num) {
            map.put(b, new Node(this, b, num));
        }

        //return child of current node with given bit
        Node getChild(int b) {
            return map.get(b);
        }

        //check if current Node has child with given bit
        boolean hasChild(int b) {
            return map.containsKey(b);
        }

        //return Node number
        long getNum() {
            return this.nodeNum;
        }

        //return bit
        int getBit() {
            return this.bit;
        }

        //return bits sequence at path from root to current Node
        ArrayList<Integer> getBits() {
            ArrayList<Integer> bits = new ArrayList<>();
            Node curNode = this;
            while (curNode.parent != null) {
                bits.add(0, curNode.getBit());
                curNode = curNode.parent;
            }
            return bits;
        }
    }

    //return bit of byteArray at given position
    private int getBit(byte[] byteArray, long pos) {
        int value = byteArray[(int) (pos / 8)] + 128;
        return (value >> (7 - pos % 8)) & 1;
    }

    //convert int to bits sequence
    private ArrayList<Integer> intToBits(long value) {
        ArrayList<Integer> bits = new ArrayList<>();
        do {
            bits.add(0, (int) (value & 1));
            value = value >> 1;
        } while (value > 0);
        return bits;
    }

    //convert bits sequence to int
    private long bitsToInt(ArrayList<Integer> bits) {
        long value = 0;
        for (int i = 0; i < bits.size(); i++)
            value += (bits.get(i) << (bits.size() - i - 1));
        return value;
    }

    //convert bits sequence to byte
    private byte bitsToByte(ArrayList<Integer> bits) {
        int value = 0;
        for (int i = 0; i < 8; i++)
            value |= bits.get(i) << (7 - i);
        return (byte) (value - 128);
    }

    @Override
    public byte[] compress(final byte[] byteArray) {
        int nodesCnt = 0; //number of nodes in Prefix Tree = number of words in dictionary
        Node root = new Node(nodesCnt++); //create root of Prefix Tree
        ArrayList<Integer> compressedAsBits = new ArrayList<>(); //bits sequence = compressed byteArray
        ArrayList<Byte> compressedAsBytes = new ArrayList<>(); //bytes sequence of compressed byteArray
        final long bitsCnt = byteArray.length * 8; //number of bits = 8 * number of bytes
        long pos = 0; //current bit position at byteArray

        //while current bit position is in byteArray
        while (pos < bitsCnt) {
            Node curNode = root;

            //first bit automatically connected to root
            if (pos == 0) {
                compressedAsBits.add(getBit(byteArray, pos));
                curNode.addChild(getBit(byteArray, pos), nodesCnt++);
                pos++;
            }

            //find the longest bits sequence, which is in dictionary
            while (pos < bitsCnt && curNode.hasChild(getBit(byteArray, pos))) {
                curNode = curNode.getChild(getBit(byteArray, pos));
                pos++;
            }

            ArrayList<Integer> curNodeNumAsBits = intToBits(curNode.getNum()); //current word number as bits sequence
            int numLen = (int) Math.ceil(Math.log(nodesCnt) / Math.log(2)); /// if there are k words in dictionary, we need upper(log2(k)) bits to code each word number
            //adding some 0 bits to beginning of current word number
            while (curNodeNumAsBits.size() < numLen)
                curNodeNumAsBits.add(0, 0);

            //add current word number (as bits) to compressedAsBits
            compressedAsBits.addAll(curNodeNumAsBits);
            if(pos < bitsCnt) {
                compressedAsBits.add(getBit(byteArray, pos));
                curNode.addChild(getBit(byteArray, pos), nodesCnt++);
            }

            //add 'full' bytes to compressedAsBytes and delete them from compressedAsBits
            while (compressedAsBits.size() >= 8) {
                compressedAsBytes.add(bitsToByte(compressedAsBits));
                for (int i = 0; i < 8; i++)
                    compressedAsBits.remove(0);
            }

            pos++;
        }

        int dontUse = 0; //number of useless bits at the end

        //add last bits to compressedAsBytes
        if (compressedAsBits.size() > 0) {
            while (compressedAsBits.size() < 8) {
                compressedAsBits.add(0);
                dontUse++;
            }
            compressedAsBytes.add(bitsToByte(compressedAsBits));
        }

        //add number of useless bits as first byte
        compressedAsBytes.add(0, (byte) (dontUse - 127));

        //convert ArrayList to byte array
        byte[] compressed = new byte[compressedAsBytes.size()];
        for (int i = 0; i < compressedAsBytes.size(); i++)
            compressed[i] = compressedAsBytes.get(i);
        return compressed;
    }

    @Override
    public byte[] decompress(byte[] byteArray) {
        int nodesCnt = 0; //number of words that are actually in dictionary = number of Nodes in Prefix Tree
        Node root = new Node(nodesCnt++); //create root of PrefixTree
        Map<Long, Node> numToNode = new HashMap<>(); //map for getting Node by its number
        numToNode.put(root.getNum(), root); //add root to map
        ArrayList<Integer> decodedAsBits = new ArrayList<>(); //bits sequence of decoded array
        ArrayList<Byte> decodedAsBytes = new ArrayList<>(); //bytes sequence of decoded array

        long pos = 8; //starts from 8th position, because first 8 bits = number of useless bits at end
        long bitsCnt = 8 * byteArray.length; //number of bits = 8 * number of bytes

        int dontUse = byteArray[0] + 128; //number of useless bits at the end

        while (pos < bitsCnt - dontUse) {
            int numLen = (int) Math.ceil(Math.log(nodesCnt) / Math.log(2)); /// if there are k words in dictionary, we need upper(log2(k)) bits to code each word number
            ArrayList<Integer> numAsBits = new ArrayList<>(); //current word number
            for (int i = 0; i < numLen; i++) {
                numAsBits.add(getBit(byteArray, pos));
                pos++;
            }
            long nodeNum = bitsToInt(numAsBits); //convect bits sequence to int
            Node curNode = numToNode.get(nodeNum); //get node by its number
            decodedAsBits.addAll(curNode.getBits()); //add node number to decodedAtB
            if (pos < bitsCnt - dontUse) {
                decodedAsBits.add(getBit(byteArray, pos)); //add next bit to decodedAsBits
                curNode.addChild(getBit(byteArray, pos), nodesCnt++); //add child with given bit to current node
                curNode = curNode.getChild(getBit(byteArray, pos)); //get new node
                numToNode.put(curNode.getNum(), curNode); //add this new node to map numToNode
            }

            //add 'full' bytes to compressedAsBytes and delete them from compressedAsBits
            while (decodedAsBits.size() >= 8) {
                decodedAsBytes.add(bitsToByte(decodedAsBits));
                for (int i = 0; i < 8; i++)
                    decodedAsBits.remove(0);
            }

            pos++;
        }

        //convert ArrayList to byte array
        byte[] decoded = new byte[decodedAsBytes.size()];
        for (int i = 0; i < decodedAsBytes.size(); i++)
            decoded[i] = decodedAsBytes.get(i);

        return decoded;
    }
}