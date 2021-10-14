package norseninja;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Stage stage;
    private static Scene scene;
    private static TcpClient tcpClient;
    private static MainController mainController;
    private static LoginController loginController;
    private static final String LOGIN_VIEW = "loginView";
    private static final String MAIN_VIEW = "mainView";
    private static String currentView = LOGIN_VIEW;

    @Override
    public void start(Stage stage) throws IOException {
        tcpClient = new TcpClient("83.243.162.56", 1301);

        stage.setOnCloseRequest(event -> stop());

        App.stage = stage;

        scene = new Scene(loadFxml(LOGIN_VIEW));
        stage.setScene(scene);
        setSize(200,250);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        currentView = fxml;
        scene.setRoot(loadFxml(fxml));
    }

    /**
     * Returns a parent node of a .fxml.
     * @param fxml the name of the .fxml file to be loaded.
     * @return {@code Parent} the loaded fxml.
     * @throws IOException exception if fxml could not be loaded.
     */
    private static Parent loadFxml(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent parent = fxmlLoader.load();
        if (fxml.equals(MAIN_VIEW)) {
            App.mainController = fxmlLoader.getController();
            mainController.setTcpClient(tcpClient);
            stage.setTitle("Chatomatic Client - Logged in as " + tcpClient.getMe());
        } else if (fxml.equals(LOGIN_VIEW)) {
            App.loginController = fxmlLoader.getController();
            loginController.setTcpClient(tcpClient);
        }
        return parent;
    }

    @Override
    public void stop() {
        if (currentView.equals(MAIN_VIEW)) {
            mainController.exitApplication();
        } else {
            loginController.exitApplication();
        }


        System.exit(0);
    }

    static void logout() throws IOException {
        tcpClient.logout();
        setRoot(LOGIN_VIEW);
        setSize(200, 250);
    }

    static void connectionError() throws IOException {
        setRoot(LOGIN_VIEW);
        setSize(200,250);
        tcpClient.stop();
    }

    static void login() throws IOException {
        setRoot(MAIN_VIEW);
        mainController.setTcpClient(loginController.getTcpClient());
        setSize(800,450);
    }

    public static void main(String[] args) {
        launch();
    }

    static void setSize(int width, int height) {
        scene.getWindow().setWidth(width);
        scene.getWindow().setHeight(height);
        scene.getWindow().centerOnScreen();
    }


}