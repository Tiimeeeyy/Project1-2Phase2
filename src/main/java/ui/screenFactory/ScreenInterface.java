package ui.screenFactory;

import javafx.scene.Parent;

/**
 * Interface representing a screen in the application.
 */
public interface ScreenInterface {

    /**
     * Gets the root node of the screen.
     *
     * @return the root node
     */
    Parent getRoot();
}
