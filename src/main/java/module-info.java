module norseninja {
    requires javafx.controls;
    requires javafx.fxml;

    opens norseninja to javafx.fxml;
    exports norseninja;
}