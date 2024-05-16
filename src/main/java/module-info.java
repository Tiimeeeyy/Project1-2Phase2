module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;
    requires jzy3d.api;

    opens ui to javafx.fxml;
    exports ui;
}