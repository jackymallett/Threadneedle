/*
 * Program: Threadneedle
 *
 * BorderedTitledPane  Places content in a bordered pane with a title
 *
 * Source: https://gist.github.com/jewelsea/2838292
 *
 * Author : Copyright (c) Jacky Mallett
 * Date   : September 2014
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 */
package gui;

import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;


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
