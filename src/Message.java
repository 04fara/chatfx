import java.io.Serializable;

class Message implements Serializable {
    private String extension;
    private byte[] data;

    Message(String extension, byte[] data) {
        this.extension = extension;
        this.data = data;
    }

    String getExtension() {
        return extension;
    }

    void setData(byte[] data) {
        this.data = data;
    }

    byte[] getData() {
        return data;
    }
}
