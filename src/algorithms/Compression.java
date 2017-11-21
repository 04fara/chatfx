package algorithms;

interface Compression {
    byte[] compress(byte[] byteArray);

    byte[] decompress(byte[] byteArray);
}
