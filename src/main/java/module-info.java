module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;

    requires jzy3d.api;
    requires jzy3d.javafx;
    requires org.jfree.chart3d;
    requires org.jfree.chart3d.fx;
    requires javafx.swing;
    requires commons.math3;

    opens ui to javafx.fxml;
    opens ui.controller to javafx.fxml;
    exports ui;
    exports ui.controller to javafx.fxml;
    exports engine.bot.rule_based_old;
    exports engine.bot.rule_based_new;
    exports engine.bot.AibotGA;
    exports engine.solvers.odeSolvers;
    exports engine.solvers.odeFunctions;
    exports engine.parser;
    exports engine.bot.ml_bot.agent;
}