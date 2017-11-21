package algorithms;

import java.util.ArrayList;

class Hamming implements Encoding {
    private byte[][] matrix;
    private byte[] syndromeMatrix;
    private int numberOfControlBits;
    private int codeWordLength;

    Hamming(int codeWordLength) {
        this.codeWordLength = codeWordLength;
        findNumberOfControlBits();
        constructMatrix();
    }

    @Override
    public byte[] encode(byte[] input) {
        input = prepareToHamming(input, codeWordLength);
        int p = 0;
        ArrayList<Byte> hammingCodeList = new ArrayList<>();
        while (p < input.length) {
            byte[] inputWord = new byte[codeWordLength];
            System.arraycopy(input, p, inputWord, 0, codeWordLength);
            byte[] byteCodeWithControlBits = findByteCodeWithControlBits(inputWord, codeWordLength, numberOfControlBits);
            findSyndromeMatrix(byteCodeWithControlBits);
            int j = 0;
            for (int i = 0; i < byteCodeWithControlBits.length; i++)
                if (is2Exp(i + 1))
                    byteCodeWithControlBits[i] = syndromeMatrix[j++];
            for (byte i : byteCodeWithControlBits) hammingCodeList.add(i);
            p += codeWordLength;
        }
        byte[] hammingCode = new byte[hammingCodeList.size()];
        for (int i = 0; i < hammingCodeList.size(); i++)
            hammingCode[i] = hammingCodeList.get(i);
        return hammingCode;
    }

    @Override
    public byte[] decode(byte[] input) {
        int p = 0;
        ArrayList<Byte> decodeList = new ArrayList<>();
        while (p < input.length) {
            byte[] inputWord = new byte[codeWordLength + numberOfControlBits];
            System.arraycopy(input, p, inputWord, 0, codeWordLength + numberOfControlBits);
            findSyndromeMatrix(inputWord);
            int errorPosition = 0;
            for (int i = 0; i < syndromeMatrix.length; i++)
                errorPosition += syndromeMatrix[i] * Math.pow(2, i);
            if (errorPosition > 0)
                if (inputWord[errorPosition - 1] == 1) inputWord[errorPosition - 1] = 0;
                else inputWord[errorPosition - 1] = 1;
            for (int i = 0; i < inputWord.length; i++)
                if (!is2Exp(i + 1)) decodeList.add(inputWord[i]);
            p += codeWordLength + numberOfControlBits;
        }
        byte[] decodedCode = new byte[decodeList.size()];
        for (int i = 0; i < decodeList.size(); i++)
            decodedCode[i] = decodeList.get(i);
        decodedCode = deleteAddedZeros(decodedCode, codeWordLength);
        return decodedCode;
    }

    //Finding number of parity bits which are needed for current codeWord
    private void findNumberOfControlBits() {
        int w = 0;
        numberOfControlBits = 0;
        while (w < codeWordLength)
            if (Math.pow(2, numberOfControlBits) == w + numberOfControlBits + 1) numberOfControlBits++;
            else w++;
    }

    //Adding parity bits to byte code
    private byte[] findByteCodeWithControlBits(byte[] input, int codeWordLength, int numberOfControlBits) {
        ArrayList<Byte> list = new ArrayList<>();
        for (byte i : input) list.add(i);
        for (int i = 0; i < codeWordLength + numberOfControlBits; i++)
            if (is2Exp(i + 1)) list.add(i, (byte) 0);
        byte[] byteCodeWithControlBits = new byte[codeWordLength + numberOfControlBits];
        for (int i = 0; i < list.size(); i++)
            byteCodeWithControlBits[i] = list.get(i);
        return byteCodeWithControlBits;
    }

    //Examine whether number is exponent of 2 or not
    private boolean is2Exp(int value) {
        int k = 1;
        while (value != k) {
            k *= 2;
            if (value == k) return true;
            if (k > value) return false;
        }
        return true;
    }

    //Construct transformation matrix
    private void constructMatrix() {
        matrix = new byte[numberOfControlBits][codeWordLength + numberOfControlBits];
        for (int i = 0; i < (codeWordLength + numberOfControlBits); i++) {
            StringBuilder s = new StringBuilder(Integer.toBinaryString(i + 1));
            while (s.length() < numberOfControlBits) s.insert(0, "0");
            for (int k = numberOfControlBits - 1; k > -1; k--)
                matrix[k][i] = Byte.parseByte(Character.toString(s.charAt(numberOfControlBits - k - 1)));
        }
    }

    //Finding syndrome matrix
    private void findSyndromeMatrix(byte[] byteCodeWithControlBits) {
        syndromeMatrix = new byte[numberOfControlBits];
        for (int i = 0; i < numberOfControlBits; i++) {
            byte sum = 0;
            for (int k = 0; k < byteCodeWithControlBits.length; k++)
                sum += byteCodeWithControlBits[k] * matrix[i][k];
            syndromeMatrix[i] = Byte.parseByte(Integer.toString(sum % 2));
        }
    }

    //Number of zeros which should be added to the input so that input length should multiple by codeword length
    private static int prepareToHammingCount(byte[] arr, int codeWordLength) {
        int b = arr.length % codeWordLength, p = 0;
        for (int i = arr.length - 1; i < (arr.length - 1) + codeWordLength - b; i++) p++;
        return p;
    }

    //Adding zeros to input
    private static byte[] prepareToHamming(byte[] arr, int codeWordLength) {
        byte[] arr2 = new byte[arr.length + prepareToHammingCount(arr, codeWordLength)];
        for (int i = 0; i < arr2.length; i++)
            if (i < arr.length) arr2[i] = arr[i];
            else arr2[i] = 0;
        return arr2;
    }

    //Deleting added zeros from output
    private static byte[] deleteAddedZeros(byte[] arr, int codeWordLength) {
        byte[] arr2 = new byte[arr.length - prepareToHammingCount(arr, codeWordLength) + 1];
        System.arraycopy(arr, 0, arr2, 0, arr2.length);
        return arr2;
    }
}
