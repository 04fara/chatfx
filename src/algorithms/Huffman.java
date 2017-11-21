package algorithms;

import java.util.*;

public class Huffman implements Compression {
    //Node of algorithms.Huffman tree
    private class Node {
        private byte b; //Node is associated with byte
        private Node left, right; //children
        private int count; //how many times this node with the byte occurs in text
        private ArrayList<Integer> code; //code of this byte

        //empty constructor
        Node() {
            this.left = null;
            this.right = null;
            this.b = 0;
            this.count = 0;
            this.code = new ArrayList<>();
        }

        //constructor with given children, byte and count
        Node(Node left, Node right, byte b, int count) {
            this.left = left;
            this.right = right;
            this.b = b;
            this.count = count;
            this.code = new ArrayList<>();
        }

        //set given byte for this node
        void setByte(byte b) {
            this.b = b;
        }

        //get byte of this node
        byte getByte() {
            return this.b;
        }

        //get left child (equals to transition by bit = 0)
        Node getLeft() {
            return this.left;
        }

        //set left child
        void setLeft(Node node) {
            this.left = node;
        }

        //get right child (equals to transition by bit = 1)
        Node getRight() {
            return this.right;
        }

        //set right child
        void setRight(Node node) {
            this.right = node;
        }

        //get count
        int getCount() {
            return this.count;
        }

        //set code of current node
        void setCode(Node parent) {
            this.code = new ArrayList<>(parent.getCode());
            this.code.add(parent.getLeft() == this ? 0 : 1);
        }

        //get code of current node
        ArrayList<Integer> getCode() {
            return this.code;
        }
    }

    //convert byte to bits sequence
    private ArrayList<Integer> byteToBits(byte b) {
        Integer value = b + 128;
        ArrayList<Integer> bits = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            bits.add(0, value & 1);
            value = value >> 1;
        }
        return bits;
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

    //convert bits sequence to byte
    private byte bitsToByte(ArrayList<Integer> bits) {
        int value = 0;
        for (int i = 0; i < 8; i++)
            value |= bits.get(i) << (7 - i);
        return (byte) (value - 128);
    }

    //get bit at given position from byteArray
    private int getBit(byte[] byteArray, long pos) {
        int value = byteArray[(int) (pos / 8)] + 128;
        return (value >> (7 - pos % 8)) & 1;
    }

    //show HuffmanTree
    private void showTree(Node curNode) {
        if (curNode.getLeft() == null)
            System.out.println(curNode.getByte() + " " + curNode.getCode());
        else {
            showTree(curNode.getLeft());
            showTree(curNode.getRight());
        }
    }

    //convert algorithms.Huffman Tree to bits sequence
    private void huffmanTreeToBits(Node curNode, ArrayList<Integer> bits, ArrayList<Byte> bytes) {
        //each node has 2 children or 0 children
        // if it has 0 children, curNode.getLeft == null
        if (curNode.getLeft() == null) {
            //so Node has 0 children
            bits.add(0); //add 0 to bits sequence
            bits.addAll(byteToBits(curNode.getByte())); //add Node number as bits sequence to encodedAsBits
            //add 'ready' bytes
            while (bits.size() >= 8) {
                bytes.add(bitsToByte(bits));
                for (int i = 0; i < 8; i++)
                    bits.remove(0);
            }
        } else {
            //so Node has 2 children
            bits.add(1); //add 1 to bits sequence
            //add 'ready' bytes
            while (bits.size() >= 8) {
                bytes.add(bitsToByte(bits));
                for (int i = 0; i < 8; i++)
                    bits.remove(0);
            }
            huffmanTreeToBits(curNode.getLeft(), bits, bytes); //go to left child
            huffmanTreeToBits(curNode.getRight(), bits, bytes); //go to right child
        }
    }

    //build algorithms.Huffman Tree from byteArray
    private int buildHuffmanTreeFromByteArray(Node curNode, byte[] byteArray, int pos) {
        //we are now at some Node
        if (getBit(byteArray, pos) == 1) {
            //if bit is equal to 1, so it has 2 children
            pos++;

            curNode.setLeft(new Node(null, null, (byte) 0, 0)); //create left child
            curNode.getLeft().setCode(curNode); //set code to left child
            pos = buildHuffmanTreeFromByteArray(curNode.getLeft(), byteArray, pos); //go to left child

            curNode.setRight(new Node(null, null, (byte) 0, 0)); //create right child
            curNode.getRight().setCode(curNode); //set code to right child
            pos = buildHuffmanTreeFromByteArray(curNode.getRight(), byteArray, pos); //go to right child
            return pos;
        } else {
            //if bit is equal to 0, so it has 0 children
            //next 8 bits is the byte that Node is encoding
            pos++;
            ArrayList<Integer> bits = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                bits.add(getBit(byteArray, pos));
                pos++;
            }
            curNode.setByte(bitsToByte(bits));
            return pos;
        }
    }

    //initialize codes to each node of algorithms.Huffman Tree
    private void setCodesToHuffmanTreeNodes(Node curNode, Node parent) {
        if (parent != null) curNode.setCode(parent);
        if (curNode.getLeft() != null) {
            setCodesToHuffmanTreeNodes(curNode.getLeft(), curNode);
            setCodesToHuffmanTreeNodes(curNode.getRight(), curNode);
        }
    }

    //encode byteArray
    private byte[] encode(byte[] byteArray) {
        Map<Byte, Integer> count = new HashMap<>();

        //counting number of each byte type
        for (byte i : byteArray)
            if (!count.containsKey(i)) count.put(i, 1);
            else count.put(i, count.get(i) + 1);

        //Priority queue with given comparator for sorting Nodes by frequency in byteArray
        Queue<Node> q = new PriorityQueue<>(1, Comparator.comparingInt(Node::getCount));

        //get node by given byte
        Map<Byte, Node> byteToNode = new HashMap<>();

        //create Nodes for each byte
        for (Byte b : count.keySet()) {
            Node node = new Node(null, null, b, count.get(b));
            q.add(node);
            byteToNode.put(b, node);
        }

        //if there is only 1 byte, lets create 'fake' byte
        if (q.size() == 1) {
            q.add(new Node(null, null, (byte) 0, 0));
        }

        //building algorithms.Huffman tree following algorithms.Huffman algorithm
        while (q.size() > 1) {
            Node n1 = q.poll(), n2 = q.poll();
            q.add(new Node(n1, n2, (byte) 0, n1.getCount() + n2.getCount()));
        }

        //encoded byteArray as bits sequence
        ArrayList<Integer> encodedAsBits = new ArrayList<>();
        ArrayList<Byte> encodedAsBytes = new ArrayList<>();

        Node root = q.poll(); //get root of algorithms.Huffman Tree
        setCodesToHuffmanTreeNodes(root, null); //set codes to each node of Tree
        huffmanTreeToBits(root, encodedAsBits, encodedAsBytes); //convert algorithms.Huffman tree to bits sequence

        //convert each byte to his code and add to encodedAsBits;
        for (byte i : byteArray) {
            encodedAsBits.addAll(byteToNode.get(i).getCode());
            //add 'ready' bytes
            while (encodedAsBits.size() >= 8) {
                encodedAsBytes.add(bitsToByte(encodedAsBits));
                for (int j = 0; j < 8; j++)
                    encodedAsBits.remove(0);
            }
        }

        int dontUse = 0; // number of useless bits
        //add bits to the end if it is necessary
        if (encodedAsBits.size() > 0) {
            while (encodedAsBits.size() < 8) {
                encodedAsBits.add(0);
                dontUse++;
            }
            encodedAsBytes.add(bitsToByte(encodedAsBits));
        }

        encodedAsBytes.add(0, (byte) (dontUse - 128));

        byte[] encoded = new byte[encodedAsBytes.size()]; //encoded byteArray as bytes sequence

        //convert ArrayList to byte array
        for (int i = 0; i < encodedAsBytes.size(); i++)
            encoded[i] = encodedAsBytes.get(i);

        return encoded;
    }

    private byte[] decode(byte[] byteArray) {
        int dontUse = byteArray[0] + 128; //number of useless bits
        int pos = 8; //start from 8-th bit (counting from 0), because first 8 bits is for dontUse
        int bitsCnt = 8 * byteArray.length; //number of bits = 8 * number of bytes
        ArrayList<Byte> decodedAsBytesList = new ArrayList<>(); //decoded bytes

        Node root = new Node(); //create root of algorithms.Huffman Tree
        pos = buildHuffmanTreeFromByteArray(root, byteArray, pos); //build algorithms.Huffman Tree from bits sequence

        //look throw bits
        while (pos < bitsCnt - dontUse) {
            Node curNode = root;

            //go down via Tree while we can
            while (curNode.getLeft() != null) {
                if (getBit(byteArray, pos) == 0)
                    //if current bit is equal to 0, it means we should go to left child
                    curNode = curNode.getLeft();
                else
                    //if current bit is equal to 1, it means we should go to right child
                    curNode = curNode.getRight();
                pos++;
            }

            //add byte (that is encoded by current Node) to decoded bytes sequence
            decodedAsBytesList.add(curNode.getByte());
        }

        //convert ArrayList to byte array
        byte[] decodedAsBytesArray = new byte[decodedAsBytesList.size()];
        for (int i = 0; i < decodedAsBytesList.size(); i++)
            decodedAsBytesArray[i] = decodedAsBytesList.get(i);
        return decodedAsBytesArray;
    }

    @Override
    public byte[] compress(byte[] byteArray) {
        return encode(byteArray);
    }

    @Override
    public byte[] decompress(byte[] byteArray) {
        return decode(byteArray);
    }
}