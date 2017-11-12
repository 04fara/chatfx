import javafx.application.*;
import javafx.geometry.Rectangle2D;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.collections.*;
import javafx.geometry.Pos;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;

public class ChatApp extends Application {
    private int PORT = 8080;
    private String IP = "127.0.0.1";
    private ObservableList<String> messages = FXCollections.observableArrayList();
    private ListView<String> chat = new ListView<>(messages);
    private FileChooser fileChooser = new FileChooser();
    private boolean isServer;
    private NetworkConnection connection;

    private Server createServer() {
        return new Server(PORT, message -> Platform.runLater(() -> receiveMessage(message)));
    }

    private Client createClient() {
        return new Client(IP, PORT, message -> Platform.runLater(() -> receiveMessage(message)));
    }

    private Parent createSelectWindow(final Stage stage) {
        stage.setTitle("Chat Application");
        Button server = new Button("Server");
        server.setOnAction(event -> {
            isServer = true;
            connection = createServer();
            setChatWindow(stage);
        });
        Button client = new Button("Client");
        client.setOnAction(event -> {
            isServer = false;
            connection = createClient();
            setChatWindow(stage);
        });
        HBox root = new HBox(10, server, client);
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(100, 50);
        return root;
    }

    private void setChatWindow(final Stage stage) {
        try {
            connection.startConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stage.hide();
        stage.setScene(new Scene(createChatWindow(stage)));
        centerStage(stage, 600, 600);
        stage.show();
    }

    private Parent createChatWindow(final Stage stage) {
        stage.setTitle(isServer ? "Server" : "Client");
        chat.setPlaceholder(new Label("Start messaging"));
        chat.setMouseTransparent(true);
        chat.setFocusTraversable(false);
        Button attach = new Button("\uD83D\uDCCE");
        attach.prefWidth(30);
        attach.setOnAction(event -> attach(stage, fileChooser));
        TextField inputField = new TextField();
        inputField.setPrefWidth(535);
        inputField.setOnAction(event -> sendMessage(inputField));
        Button send = new Button("\uD83E\uDC61");
        send.prefWidth(30);
        send.setOnAction(event -> sendMessage(inputField));
        HBox input = new HBox(10, attach, inputField, send);
        VBox root = new VBox(10, chat, input);
        root.setPrefSize(600, 600);
        return root;
    }

    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void attach(final Stage stage, FileChooser fileChooser) {
        try {
            File selectedFile;
            if ((selectedFile = fileChooser.showOpenDialog(stage)) != null) {
                String ext = getFileExtension(selectedFile.getName());
                connection.send(new Message(ext, Files.readAllBytes(selectedFile.toPath())));
                messages.add(ext + " file was sent");
            }
        } catch (Exception e) {
            messages.add("File could not be sent!");
        }
    }

    private void sendMessage(TextField inputField) {
        if (!inputField.getText().isEmpty()) {
            String message = (isServer ? "Server: " : "Client: ") + inputField.getText();
            inputField.clear();
            try {
                connection.send(new Message("-1", message.getBytes()));
                messages.add(message);
            } catch (Exception e) {
                messages.add(message + " (Failed to send!)");
            }
        }
    }

    private void receiveMessage(Message message) {
        if (message != null && message.getExtension() != null)
            if (!message.getExtension().equals("-1")) {
                try {
                    String path = "file.";
                    int i = 1;
                    while (Files.exists(Paths.get(path + message.getExtension())))
                        path = "file (" + i++ + ").";
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(path + message.getExtension()));
                    fileOutputStream.write(message.getData());
                    fileOutputStream.close();
                    messages.add(message.getExtension() + " file was received");
                } catch (Exception ex) {
                    System.out.println("SOMETHING WENT WRONG!");
                    ex.printStackTrace();
                }
            } else messages.add(new String(message.getData(), Charset.forName("UTF-8")));
        else messages.add("Connection closed");
    }

    @Override
    public void init() throws Exception {
        chat.setPrefHeight(565);
        chat.setCellFactory(cell -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    if (!item.startsWith("Server") && !item.startsWith("Client")) {
                        setStyle("-fx-font-weight: bold;");
                        setAlignment(Pos.CENTER);
                    } else if (item.startsWith(isServer ? "Client" : "Server"))
                        setAlignment(Pos.CENTER_LEFT);
                    else setAlignment(Pos.CENTER_RIGHT);
                    if (item.equals("Connection closed")
                            || item.endsWith(" (Failed to send!)")
                            || item.equals("File could not be sent!")) setTextFill(Color.RED);
                }
            }
        });
    }

    @Override
    public void stop() throws Exception {
        if (connection != null) connection.closeConnection();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createSelectWindow(primaryStage)));
        centerStage(primaryStage, 100, 50);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void centerStage(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}