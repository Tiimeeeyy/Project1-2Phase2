module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;

    opens ui to javafx.fxml;
    exports ui;
}