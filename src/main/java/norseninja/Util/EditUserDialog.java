package norseninja.Util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import norseninja.TcpClient;

import java.util.HashMap;

public class EditUserDialog extends Dialog<String[]> {
    private final Mode mode;
    private final TcpClient tcpClient;
    private final TextField nameField = new TextField();
    private String name;
    private final GridPane gridPane = new GridPane();

    public enum Mode {
        NAME, PASSWORD
    }

    public EditUserDialog(TcpClient tcpClient, Mode mode) {
        super();
        this.mode = mode;
        this.tcpClient = tcpClient;
        if (this.mode.equals(Mode.NAME)) {
            this.name = this.tcpClient.getMe();
        }
        showContent();
    }

    private void showContent() {
        Stage stage = (Stage) getDialogPane().getScene().getWindow();

        //Create save button
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        Node saveButton = getDialogPane().lookupButton(saveButtonType);

        //Create gridPane
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10,50,10,10));

        Label statusLabel = new Label("");
        statusLabel.setTextFill(Color.RED);

        VBox statusBox = new VBox();
        statusBox.setAlignment(Pos.CENTER);
        statusBox.getChildren().add(statusLabel);

        gridPane.add(statusBox,0,0,2,1);

        PasswordField oldPwField = new PasswordField();
        PasswordField newPwField = new PasswordField();
        if (this.mode.equals(Mode.NAME)) {
            stage.setTitle("Change Display Name");

            Label nameLabel = new Label("Display name: ");
            nameField.setPromptText("Display name");
            nameField.setText(this.name);
            nameField.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean duplicateName = false;
                for (String existingName : tcpClient.getAllUsers()) {
                    if (existingName.equals(newValue) && !newValue.equals(this.name)) {
                        duplicateName = true;
                        break;
                    }
                }
                if (duplicateName) {
                    statusLabel.setText("This name is taken");
                    saveButton.setDisable(true);
                } else {
                    statusLabel.setText("");
                    saveButton.setDisable(false);
                }
            });
            gridPane.add(nameLabel,0,1);
            gridPane.add(nameField,1,1);
        } else if (this.mode.equals(Mode.PASSWORD)) {
            stage.setTitle("Change Password");
            saveButton.setDisable(true);

            oldPwField.setPromptText("Old password");
            newPwField.setPromptText("New password");

            HashMap<Integer, Integer> validInput = new HashMap<>();
            oldPwField.textProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue.isBlank() || !this.tcpClient.checkPassword(newValue)) {
                    statusLabel.setText("Wrong password");
                    validInput.remove(1);
                } else {
                    statusLabel.setText("");
                    validInput.put(1,1);
                }
                saveButton.setDisable(validInput.size() != 2);
            });
            newPwField.textProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue.isBlank() || newValue.length() < 5 || newValue.length() > 14) {
                    statusLabel.setText("New password must be 5-14 characters");
                    validInput.remove(2);
                } else {
                    statusLabel.setText("");
                    validInput.put(2,2);
                }
                saveButton.setDisable(validInput.size() != 2);
            });

            gridPane.add(new Label("Old password: "),0,1);
            gridPane.add(oldPwField,1,1);

            gridPane.add(new Label("New password: "),0,2);
            gridPane.add(newPwField,1,2);
        }

        getDialogPane().setContent(gridPane);

        setResultConverter(
                (ButtonType button) -> {
                    String[] response = null;
                    if (this.mode == Mode.NAME && button == saveButtonType) {
                        response = new String[]{nameField.getText()};
                    } else if (this.mode == Mode.PASSWORD && button == saveButtonType) {
                        response = new String[]{oldPwField.getText(), newPwField.getText()};
                    }
                    return response;
                }
        );
    }

}
