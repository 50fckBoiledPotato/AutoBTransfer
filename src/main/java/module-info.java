module com.pepper.autobtransfer {
    requires javafx.controls;
    requires javafx.fxml;
    requires bluecove;

    opens com.pepper.autobtransfer.controller to javafx.fxml;
    exports com.pepper.autobtransfer;
    exports com.pepper.autobtransfer.controller;
}
