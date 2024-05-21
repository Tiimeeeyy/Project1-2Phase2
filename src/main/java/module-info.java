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
    requires gluegen.rt;

    opens ui to javafx.fxml;
    opens ui.controller to javafx.fxml;
    exports ui;
    exports ui.controller to javafx.fxml;
    exports engine.bot.rule_based;
}