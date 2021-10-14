package norseninja;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;


public class LoginController {
    private TcpClient tcpClient;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    public void setTcpClient(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
        setup();
    }

    private void setup() {
        if (this.tcpClient.connectToServer()) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Connected");
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Disconnected");
        }

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

        usernameTextField.setOnKeyPressed( event -> {
            if( event.getCode() == KeyCode.ENTER ) {
                try {
                    loginButtonClicked();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } );

        passwordField.setOnKeyPressed( event -> {
            if( event.getCode() == KeyCode.ENTER ) {
                try {
                    loginButtonClicked();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } );
    }

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
                App.login();
            }
        }
    }

    public void exitApplication() {
        this.tcpClient.stop();
    }

}
