/* Program: Threadneedle
 *
 * Chart Controller for chart configuration. Handles association of
 * displayed charts with Statistics, and provides Stage for 
 * chartcontroller.fxml
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

import javafx.collections.MapChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import statistics.Statistic;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * MacroController provides graphs for the simulation representing
 * macro-economic equations.
 *
 * The mapping of statistic to chart is defined in the 
 * resources/macrocontroller.fxml file
 */

public class MacroController extends Stage implements Initializable
{
  @FXML private FlowPane flowpane;
  @FXML private StepChart mvpt;

  public static HashMap<String, StepChart> charts = new HashMap<>();

  private ChartConfig cc = null;

  //private MacroConfig cc = null;

  public MacroController()
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/macrocontroller.fxml"));
    fxmlLoader.setController(this);

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.getScene().getStylesheets()
          .add("../resources/chartstyles.css");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

	/*
     * Charts are defined in in the chartcontroller fxml file, and
     * automatically populated in the vbox. Put into local list, and remove
	 * those which are not enabled from actual display.
     */
    for (Node node : flowpane.getChildren())
    {
      StepChart chart = (StepChart) node;
      charts.put(node.getId(), chart);
    }

/*
    for (StepChart chart : charts.values())
    {
      if (!chart.enabled)
        flowpane.getChildren().remove(chart);
    }
*/
    flowpane.setPrefWrapLength(600);
    flowpane.setOrientation(Orientation.HORIZONTAL);
    this.sizeToScene();
    this.setTitle("Charts"); 

	/*
     * Handler for mouse press on chart display. Put up list of available
	 * charts that display/removes charts as appropriate on ok.
	 */

    addEventHandler(MouseEvent.MOUSE_PRESSED,
                    event -> {
                      if (cc == null)
                      {
                        createChartConfig();
                      }
                      cc.show();
                      cc.toFront();
                      cc.setIconified(false);
                    });

    /*
     * Listener for statistics which are grouped into particular charts
     */

    Statistic.obsNames.addListener((MapChangeListener<String, Statistic>) change -> {
      StepChart chart;
      Statistic stat;

      // Look for new statistics with a group matching a chart in this
      // controller's list (derived from fxml file)
      if (change.wasAdded())
      {
        stat = change.getValueAdded();

        if (stat.group != null)
          chart = charts.get(stat.group);
        else
          chart = charts.get(stat.name);
          if (chart != null)
          {
            addSeries(chart, stat.name, stat);
          }
          else
          {
            //System.out.println("No chart for statistics group " + stat.group + " " + stat.name);
            //System.out.println("Defined charts are: ");
            //for(StepChart c : charts.values())
            //    System.out.println("\t" + c.getTitle() + " " + c.getId());
          }
      }
    });
  }

  private void createChartConfig()
  {
    cc = new ChartConfig(charts);
    cc.setX(getX() + getWidth()); // open to the right of chart controller

    // display or remove charts when the window is hidden / closed

    cc.setOnHidden(event -> {
          for (Node node : cc.grid.getChildren())
          {
              ChartConfig.CBox cb = (ChartConfig.CBox) node;
              charts.get(cb.chartId).setEnabled(cb.isSelected());
          }
          refresh();
    });
  }

  public void refresh()
  {
    flowpane.getChildren().clear();

    for (StepChart chart : charts.values())
    {
      if (chart.enabled)
      {
        flowpane.getChildren().add(chart);
      }
    }
    sizeToScene();
  }

  /**
   * Reload the statistics object for all charts.
   */

  public void reloadAll()
  {
    for (StepChart chart : charts.values())
    {
      chart.reload();
    }
  }

  /**
   * Toggle charts on/off in the viewing panel.
   *
   * @param chartname Name of chart
   * @param enable    True/False
   */
  public static void setEnabled(String chartname, boolean enable)
  {
    StepChart chart = charts.get(chartname);

    if (chart != null)
    {
      chart.enabled = enable;
    }
    else
      System.out.println("No chart registered with name: " + chartname);
  }

  public void display()
  {
    show();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // numberCol.setCellFactory(TextFieldTableCell.forTableColumn());
  }

  /**
   * Add a series to a specified chart.
   *
   * @param chart  Chart to add the series too
   * @param series Name of series series to add
   * @param stat   Statistic providing series.
   */
  public static void addSeries(StepChart chart, String series, Statistic stat)
  {
    if (chart != null)
      chart.addSeries(series, stat);
  }

  /**
   * Find chart matching name and add statistic to it. Used for charts which
   * are providing aggregate statistics.
   *
   * @param chartname  Name of chart
   * @param seriesname Name of series
   * @param stat       Statistic providing series
   */

  public static void addSeries(String chartname, String seriesname,
                               Statistic stat)
  {
    StepChart chart = charts.get(chartname);

    if (chart != null)
      addSeries(chart, seriesname, stat);
    else
      throw new RuntimeException("Unable to find chart: " + chartname);
  }

  public void saveAllCharts(String directory)
  {

      File dir = new File(directory);

      dir.mkdirs();

      for (StepChart chart : charts.values())
      {
        if (chart.enabled) {
          saveChart(chart, directory + "/" + chart.getTitle().trim().replace(" ", "_") + ".png");
        }
      }
  }

  /**
   * Save visible charts to png files specified filename.
   *
   * @param copy      chart to save
   * @param filename  File to save chart to.
   */

  public void saveChart(StepChart copy, String filename)
  {
     // Create a copy of the chart rendered in the background
     // in order to avoid the issue of not being able to 
     // schedule the save on the javafx thread.

     StepChart chart = new StepChart(copy, 320, 240);

     // Create a scene to attach the new chart to.
     Scene p = new Scene(chart);

     p.getStylesheets().add("../resources/chartstyles.css");
     chart.applyCss();
     chart.layout();

     try
     {
       WritableImage snap = chart.snapshot(null, null);
       ImageIO.write(SwingFXUtils.fromFXImage(snap, null), "png", new File(filename));
     }
     catch (Exception e)
     {
       System.out.println("Failed to write file: " + filename);
     }
  }

  /**
   * Increment the x value on all charts (rollover point for next round)
   */
  public void inc_x()
  {
    for (StepChart chart : charts.values())
      chart.inc_x();
  }

  public static void saveCsv(String filename, StepChart chart) throws IOException
  {
    File csv = new File(filename);

    for (StepChart.StepSeries s : chart.series.values())
    {
      s.printSeries(csv);
    }
  }
}
