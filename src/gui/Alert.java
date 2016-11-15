/**
 * Program: Threadneedle
 *
 * Alert class.
 *
 * Provide a popup dialog.
 *
 * Author : Copyright (c) Jacky Mallett
 * Date   : August 2014
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Alert extends Stage
{

  /**
   * CUSTOM MESSAGE DIALOG WINDOW
   *
   * @param title   Window title
   * @param message Message to display
   * @param width   Width for alert window
   * @param height  Height of alert window
   * @param icon    Optional icon.
   */
  public Alert(String title, String message, int width, int height, Image icon)
  {
    super();

    /* get stylesheet path */
    String stylesheet = Alert.class.getResource(
      "../../resources/alert.css").toExternalForm();

    /* layout */
    BorderPane layout = new BorderPane();

    /* layout -> center */
    TextArea textArea = new TextArea(message);
    textArea.setWrapText(true);
    textArea.setEditable(false);
    textArea.setId("textArea");
    textArea.getStylesheets().add(stylesheet);

    /* layout -> bottom */
    Button ok = new Button("OK");
    ok.setPrefWidth(60);
    ok.setOnAction(ae -> close());

    /* add items to the layout */
    layout.setCenter(textArea);
    layout.setBottom(ok);

    BorderPane.setAlignment(ok, Pos.CENTER);
    BorderPane.setMargin(textArea, new Insets(10, 10, 10, 10));
    BorderPane.setMargin(ok, new Insets(10, 10, 10, 10));

    /* create scene */
    Scene scene = new Scene(layout, width, height);

    /* set stage preferences */
    this.setScene(scene);
    this.setTitle(title);
    this.setResizable(false);
    if (icon != null)
    {
      this.getIcons().add(icon);
    }

    /* un-comment line below if you want dialog to be always on top */
    // this.initModality(Modality.APPLICATION_MODAL);
  }
}
