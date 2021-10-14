package norseninja;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import norseninja.Util.EditUserDialog;

import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        setup();
        run();
    }

    public void setup() {
        inputField.setOnKeyPressed( event -> {
            if( event.getCode() == KeyCode.ENTER ) {
                sendMessageButtonClicked();
            }
        });
        inputField.requestFocus();
    }

    private void loadActiveRecipients() {
        userBox.getItems().setAll(this.tcpClient.getActiveUsers());
        userBox.getSelectionModel().selectFirst();
    }

    private void loadAllRecipients() {
        userBox.getItems().setAll(this.tcpClient.getAllUsers());
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
    private void editDisplayNameButtonClicked() {
        EditUserDialog dialog = new EditUserDialog(tcpClient, EditUserDialog.Mode.NAME);
        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (this.tcpClient.changeDisplayName(result.get()[0])) {
                statusLabel.setText("Name changed successfully");
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setTitle("Chatomatic Client - Logged in as " + result.get()[0]);
            } else {
                statusLabel.setText("Error: name change failed");
            }
        }

    }

    @FXML
    private void changePasswordButtonClicked() {
        EditUserDialog dialog = new EditUserDialog(tcpClient, EditUserDialog.Mode.PASSWORD);
        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent() && result.get().length == 2) {
            if (this.tcpClient.changePassword(result.get()[0], result.get()[1])) {
                statusLabel.setText("Password updated successfully");
            } else {
                statusLabel.setText("Error: password change failed");
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
            userBox.focusedProperty().addListener(e -> {
                if (userBox.isFocused()) {
                    loadAllRecipients();
                }
            });
            loadAllRecipients();
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