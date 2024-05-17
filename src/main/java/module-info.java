module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;
    requires jzy3d.api;
    requires jzy3d.javafx;
    requires org.jfree.chart3d;
    requires org.jfree.chart3d.fx;
    requires javafx.swing;

    opens ui to javafx.fxml;
    exports ui;
}