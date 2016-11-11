/* Program: Threadneedle
 *
 * Chart Configuration Stage for chartconfig.fxml
 *
 * Author  :  (c) Jacky Mallett
 * Date    :  November 2014
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package charts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.*;
import java.io.*;

/**
 * Bare bones controller to provide the checkbox for chart selection. Most of the
 * action is in the ChartController class
 */

class ChartConfig extends Stage implements Initializable
{
  @FXML
  public GridPane grid;

  public ChartConfig(HashMap<String, StepChart> charts)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/chartconfig.fxml"));
    fxmlLoader.setController(this);

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.setTitle("Select Charts");

      // Iterate through charts and display a checkbox
      // for them.
      int row = 0;
      for (StepChart chart : charts.values())
      {
        CheckBox cb = new CBox(chart.getTitle(), chart.getId());
        cb.setSelected(chart.enabled);
        grid.add(cb, 0, row++);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
  }

  @FXML void onOkButton(ActionEvent event)
  {
    close();
  }

  public class CBox extends CheckBox
  {
     String chartId;

     CBox(String title, String id)
     {
        super(title);
        chartId = id;
     }
  }
}
