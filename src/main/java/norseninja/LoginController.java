package norseninja;

import java.io.IOException;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class LoginController {
    @FXML private Circle connectionCircle;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    private TcpClient tcpClient;
    private boolean running = true;
    private boolean connected = false;
    private EventHandler<KeyEvent> handler;
    private int one = 1;

    /**
     * Set this class' tcpClient.
     * @param tcpClient TcpClient
     */
    public void setTcpClient(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
        setup();
    }

    private void setup() {
        updateUI();
        setTextListeners();
    }

    private void setTextListeners() {
        this.usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                statusLabel.setText("");
            }
        });
        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                statusLabel.setText("");
            }
        });
    }

    private void setKeyListeners() {
        handler = keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                try {
                    loginButtonClicked();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        usernameTextField.addEventHandler(KeyEvent.KEY_PRESSED, handler);
        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

    @FXML
    private void clearKeyListeners() {
        usernameTextField.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
        passwordField.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

    /**
     * Returns this class' tcpClient.
     * @return {@code TcpClient}
     */
    public TcpClient getTcpClient() {
        return this.tcpClient;
    }

    @FXML
    private void loginButtonClicked() throws IOException {
        if (usernameTextField.getText().isBlank()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Please enter username");
        } else if (passwordField.getText().isBlank()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Please enter password");
        } else {
            if (!this.tcpClient.login(usernameTextField.getText(), passwordField.getText())) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Invalid credentials; try again");
            } else {
                this.running = false;
                App.login();
            }
        }
    }

    /**
     * Checks the connection to the server and updates the UI on state change.
     */
    public void updateUI() {
        Runnable taskToBeExecutedOnAnotherThread = () -> {
            while (this.running) {
                Platform.runLater(this::checkConnection);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        Thread t = new Thread(taskToBeExecutedOnAnotherThread);
        t.setDaemon(true);
        t.start();
    }

    private void checkConnection() {
        boolean connection = this.tcpClient.checkConnection();
        if (this.connected != connection) {
            this.connected = connection;
            if (connected) {
                connectionCircle.setFill(Color.GREEN);
                setKeyListeners();
            } else {
                connectionCircle.setFill(Color.RED);
                clearKeyListeners();
            }
            loginButton.setDisable(!connected);
        }
    }

    /**
     * Stops the TcpClient and the daemon threads of the controller.
     */
    public void exitApplication() {
        this.running = false;
        this.tcpClient.stop();
    }

}
