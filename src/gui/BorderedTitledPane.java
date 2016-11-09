package gui;

import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

/**
 * Places content in a bordered pane with a title.
 */
// Source: https://gist.github.com/jewelsea/2838292

class BorderedTitledPane extends StackPane
{
  BorderedTitledPane(String titleString, Node content)
  {
    Button title = new Button(" " + titleString + " ");
    title.getStyleClass().add("bordered-titled-title");
    StackPane.setAlignment(title, Pos.TOP_CENTER);

    StackPane contentPane = new StackPane();
    content.getStyleClass().add("bordered-titled-content");
    contentPane.getChildren().add(content);

    getStyleClass().add("bordered-titled-border");
    getChildren().addAll(title, contentPane);
  }
}
