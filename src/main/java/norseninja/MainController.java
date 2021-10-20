package norseninja;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import norseninja.util.EditUserDialog;

public class MainController {
    private TcpClient tcpClient;
    private ArrayList<Message> messages;
    private boolean running = false;

    @FXML TextArea textArea;
    @FXML TextField inputField;
    @FXML Label statusLabel;
    @FXML ChoiceBox<String> userBox;

    /**
     * Set this class' tcpClient.
     * @param tcpClient TcpClient
     */
    public void setTcpClient(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
        this.messages = new ArrayList<>();
        setup();
        run();
    }

    /**
     * Sets the keyEventListener for the inputField, and gives it focus.
     */
    public void setup() {
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessageButtonClicked();
            }
        });
        inputField.requestFocus();
    }

    /**
     * Loads all recipients.
     */
    private void loadAllRecipients() {
        userBox.getItems().setAll(this.tcpClient.getAllUsers());
        userBox.getSelectionModel().selectFirst();
    }

    /**
     * Sends a message containing the text from the inputField to selected user.
     */
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

    /**
     * Logs the user out and returns to the login screen.
     *
     * @throws IOException if login screen couldn't be loaded.
     */
    @FXML
    public void logoutButtonClicked() throws IOException {
        running = false;
        App.logout();
    }

    /**
     * Logs the user out and exits the application.
     */
    @FXML
    public void exitApplication() {
        if (tcpClient.logout()) {
            running = false;
        }
    }

    /**
     * Returns the user to the login screen.
     */
    private void connectionError() {
        this.running = false;
        try {
            App.connectionError();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs a background thread to continuously fetch and display new messages,
     * and confirm continued server connection.
     */
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
                while (running) {
                    getMessages();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            Thread t = new Thread(taskToBeExecutedOnAnotherThread);
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Fetches and displays new messages.
     */
    private void getMessages() {
        List<Message> newMessages;
        if (this.tcpClient.checkConnection()) {
            if (this.messages.isEmpty()) {
                newMessages = this.tcpClient.getMessages(null);
            } else {
                newMessages = this.tcpClient
                        .getMessages(this.messages
                                .get(this.messages.size() - 1)
                                .getTimeStamp());
            }
            newMessages.forEach(m -> {
                LocalTime t = m.getTimeStamp();
                textArea.appendText("<"
                        + t.format(DateTimeFormatter.ofPattern("kk:mm:ss"))
                        + ": from " + m.getFromUser() + ", to "
                        + m.getToUser() + "> "
                        + m.getMessageText()
                        + "\n");
                this.messages.add(m);
            });
        } else {
            connectionError();
        }
    }
}