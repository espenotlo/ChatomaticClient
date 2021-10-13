package norseninja;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private TcpClient tcpClient;
    private ArrayList<Message> messages;
    private boolean running = false;

    @FXML TextArea textArea;
    @FXML TextField inputField;
    @FXML Label statusLabel;
    @FXML ChoiceBox<String> userBox;

    public void setTcpClient(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
        this.messages = new ArrayList<>();
        run();
        inputField.setOnKeyPressed( event -> {
            if( event.getCode() == KeyCode.ENTER ) {
                sendMessageButtonClicked();
            }
        });
    }

    private void loadActiveRecipients() {
        userBox.getItems().setAll(this.tcpClient.getActiveUsers());
        userBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void sendMessageButtonClicked() {
        String messageText = inputField.getText();
        String recipient = userBox.getSelectionModel().getSelectedItem();
        if (!messageText.isBlank()) {
            if (tcpClient.sendMessage(recipient, messageText)) {
                statusLabel.setText("message sent");
                inputField.setText("");
            } else {
                statusLabel.setText("unable to send message");
            }
        }
    }

    @FXML
    public void logoutButtonClicked() throws IOException {
        running = false;
        App.logout();
    }

    @FXML
    public void exitApplication() {
        if (tcpClient.logout()) {
            running = false;
        }
        System.exit(0);
    }

    private void connectionError() {
        this.running = false;
        try {
            App.connectionError();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        if (!running) {
            running = true;
            userBox.focusedProperty().addListener(e -> loadActiveRecipients());
            loadActiveRecipients();
            Runnable taskToBeExecutedOnAnotherThread = () -> {
                long threadId = Thread.currentThread().getId();
                System.out.println("Fetching messages on thread #" + threadId);
                while (running) {
                    getMessages();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread sleep interrupted... Oh, well...");
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Done fetching messages on thread #" + threadId);
            };
            Thread t = new Thread(taskToBeExecutedOnAnotherThread);
            t.setDaemon(true);
            t.start();
        }
    }

    private void getMessages() {
        try {
            List<Message> newMessages;
            if (this.tcpClient.pingServer()) {
                if (this.messages.isEmpty()) {
                    newMessages = this.tcpClient.getMessages(null);
                } else {
                    newMessages = this.tcpClient.getMessages(this.messages.get(this.messages.size() - 1).getTimeStamp());
                }
                newMessages.forEach(m -> {
                    LocalTime t = m.getTimeStamp();
                    textArea.appendText("<" + t.format(DateTimeFormatter.ofPattern("kk:mm:ss")) + ": from " + m.getFromUser() + ", to " + m.getToUser() + "> " + m.getMessageText() + "\n");
                    this.messages.add(m);
                });
            } else {
                connectionError();
            }
        } catch (ConnectException e) {
            connectionError();
        }
    }
}