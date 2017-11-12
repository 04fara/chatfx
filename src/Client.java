import java.util.function.Consumer;

class Client extends NetworkConnection {
    private String ip;
    private int port;

    Client(String ip, int port, Consumer<Message> onReceiveCallback) {
        super(onReceiveCallback);
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected boolean isServer() {
        return false;
    }

    @Override
    protected String getIP() {
        return ip;
    }

    @Override
    protected int getPort() {
        return port;
    }
}
