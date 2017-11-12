import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

abstract class NetworkConnection {
    private ConnectionThread connThread = new ConnectionThread();
    private Consumer<Message> onReceiveCallback;

    protected abstract boolean isServer();

    protected abstract String getIP();

    protected abstract int getPort();

    NetworkConnection(Consumer<Message> onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
        connThread.setDaemon(true);
    }

    void startConnection() throws Exception {
        connThread.start();
    }

    void closeConnection() throws Exception {
        if (connThread.socket != null) connThread.socket.close();
    }

    void send(Serializable data) throws Exception {
        connThread.out.writeObject(data);
    }

    private class ConnectionThread extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        @Override
        public void run() {
            try {
                socket = isServer() ? new ServerSocket(getPort()).accept() : new Socket(getIP(), getPort());
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                socket.setTcpNoDelay(true);
                while (true) {
                    Message message = (Message) in.readObject();
                    onReceiveCallback.accept(message);
                }
            } catch (Exception e) {
                onReceiveCallback.accept(new Message(null, "Connection closed".getBytes()));
            }
        }
    }
}
