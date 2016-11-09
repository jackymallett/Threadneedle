/* Program: Threadneedle
 *
 * StepChart provides an adapted line chart which displays
 * handles statistic display.
 *
 * Author  :  (c) Jacky Mallett
 * Date    :  November 2014
 */

package charts;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import statistics.Statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

// todo: add property to limit view over time

/**
 * Extended version of LineChart providing auto-update from statistic values
 * from simulation.
 */

public class StepChart extends LineChart
{
  @FXML public boolean  summation;  // Display additional series, summing other series.

  public boolean enabled = true;    // Whether chart is being displayed or not

  private      StepSeries sumSeries;  // Sum of series on graph
  private long x            = 0;      // Current x value for graph
  private long  MAX_DATA_PTS = 1200 ;

  // Map of statistics displayed by chart and associated data sets

  public HashMap<String, StepSeries> series = new HashMap<>();

  // List of statistics displayed by chart, specified in chartcontroller.fxml

  public ArrayList<String> list = new ArrayList<>();
  ObservableList<String> statistics = FXCollections.observableArrayList(list);


  // chartData container for XYChart interface

  private ObservableList<XYChart.Series<Long, Long>> chartData = FXCollections
    .observableArrayList();


  public StepChart()
  {
    super(new NumberAxis(), new NumberAxis());

    setData(chartData);
    setLegendVisible(true);
    setCache(true);
    setAnimated(false);

    NumberAxis yAxis = (NumberAxis) getYAxis();
    NumberAxis xAxis = (NumberAxis) getXAxis();

    setVerticalGridLinesVisible(false);

    xAxis.setForceZeroInRange(false);

    yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis)
    {
      @Override
      public String toString(Number obj)
      {
        return String.format("%6.0f", obj);
      }
    });

    // Print the numeric series being shown on the chart to the command
    // line if the user selects the chart.

    addEventHandler(MouseEvent.MOUSE_PRESSED,
                    event -> {
                      for(StepSeries s : series.values())
                          s.printSeries(); 
                    event.consume();
                    });

    // Step charts can be configured with a list of the statistics they
    // should listen for in chartcontroller.fxml

    statistics.addListener(new ListChangeListener<String>()
    {
      @Override
      public void onChanged(
        ListChangeListener.Change<? extends String> change)
      {
        // This assumes all changes are additions to the list.
        while (change.next())
        {
          if (change.wasAdded())
          {
            for (String name : change.getAddedSubList())
            {
              Statistic statistic = Statistic.getStatistic(name);

              if (statistic != null)
              {
                addSeries(name, statistic);
                updateLegend();
              }
            }
          }
        }
      }
    });


    yAxis.setAutoRanging(true);

    yAxis.setLowerBound(0);
    yAxis.setUpperBound(5);

    setCreateSymbols(false);
  }



  /**
   * Constructor for chart outside of chart controller, used to render
   * a copy of the supplied chart in background for saving to file.
   *
   * @param chart   Chart to create a copy of 
   */
  public StepChart(StepChart chart, int maxwidth, int maxheight)
  {
    super(new NumberAxis(), new NumberAxis());

    setData(chartData);
    setLegendVisible(true);
    setCache(true);
    setTitle(chart.getTitle());
    setAnimated(false);

    setMaxSize(maxwidth, maxheight);
    setMinSize(chart.getMinWidth(), chart.getMinHeight());
    setPrefSize(maxwidth, maxheight);

    NumberAxis yAxis = (NumberAxis) getYAxis();
    NumberAxis xAxis = (NumberAxis) getXAxis();

    xAxis.setAutoRanging(true);
    xAxis.setForceZeroInRange(false);

    yAxis.setAutoRanging(true);
    //xAxis.setLowerBound(0);
    //xAxis.setUpperBound(MAX_DATA_PTS);

    yAxis.setLowerBound(0);
    yAxis.setUpperBound(5);

    setCreateSymbols(false);

    setVerticalGridLinesVisible(false);

    yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis)
    {
      @Override
      public String toString(Number obj)
      {
        return String.format("%6.0f", obj);
      }
    });

    for(StepSeries stepseries : chart.series.values())
    {
       addSeries(stepseries.stat.name, stepseries.stat);

       for(int i = 0; i < stepseries.stat.size(); i++)
       {
         XYChart.Data data = new XYChart.Data<>((long) (stepseries.stat.size() - i), stepseries.stat.get(i));
         this.series.get(stepseries.stat.name).series.getData().add(data);

         if((Long)data.getYValue() > yAxis.getUpperBound())
            yAxis.setUpperBound((Long)data.getYValue()+1);

       }
    }
    xAxis.setLowerBound(0);
    updateLegend();
 }


 public StepChart(Axis<Number> xaxis, Axis<Number> yaxis)
 {
   super(xaxis, yaxis);
   setData(chartData);
 }


  /**
   * Reload the statistics object displayed by the chart, and reset display.
   */
  public void reload()
  {
    // As the simulation reset mechanism creates new objects, the
    // statistic object has to be reloaded.

    x = 0;
    //list.clear();
    statistics.clear();

    if (sumSeries != null)
      sumSeries.series.getData().clear();

    for (StepSeries s : series.values())
    {
      s.series.getData().clear(); // clear the underlying list
      String name = s.stat.name;
      // get updated statistic
      s.stat = Statistic.getStatistic(s.stat.name, s.stat.group, s.stat.type);

      if (s.stat == null)
      {
        throw new RuntimeException("Error: No statistic matching "
                                   + name);
      }
    }

    NumberAxis xAxis = (NumberAxis) getXAxis();
    xAxis.setLowerBound(0);
    xAxis.setUpperBound(MAX_DATA_PTS);
  }

  // Beans setter/getters for updating the ObservableList from fxml

  public ObservableList<String> getStatistics()
  {
    return statistics;
  }

  public void setStatistics(ObservableList<String> name)
  {
    for (String s : name)
    {
      if (s.length() > 0)
        statistics.add(s);
    }
  }

  /** 
   * Consider as performance improvement??
  public void layoutPlotChildren()
  {
     System.out.println("move updating XYChart to here??");
     super.layoutPlotChildren();
  }
  **/

  /**
   * Add a series and its associated statistic to the chart. This will
   * fail silently if a series with an identical name is already present,
   * in order to simplify reload handling.
   *
   * @param name Name for series (also used as key)
   * @param stat statistical data holder
   */

  public void addSeries(String name, Statistic stat)
  {
    if (series.get(name) == null)
    {
      XYChart.Series<Long, Long> s = new XYChart.Series<>();
      s.setName(name);
      chartData.add(s);

      // The summation series does not have a statistic, and is held
      // separately to the other chart series.

      if (stat == null)
        sumSeries = new StepSeries(s, null);
      else
        series.put(name, new StepSeries(s, stat));

      updateLegend();
    }
  }

  /**
   * Update chart with latest point(s) in its statistics. Turn on auto-ranging
   * when data starts arriving.
   */
  public void inc_x()
  {
    long sum = 0;
    NumberAxis xAxis = (NumberAxis) getXAxis();
    NumberAxis yAxis = (NumberAxis) getYAxis();

    for (StepSeries s : series.values())
    {
      s.series.getData().add(new XYChart.Data<>(x, s.stat.get()));

      sum += s.stat.get();

      if (s.series.getData().size() > MAX_DATA_PTS)
      {
        XYChart.Data data = s.series.getData().get(0);
        s.series.getData().remove(0);
        removeDataItemFromDisplay(s.series, data); 

        xAxis.setLowerBound(x - MAX_DATA_PTS);
        xAxis.setUpperBound(x - 1);

      }
      else if (s.series.getData().size() > xAxis.getUpperBound())
      {
        xAxis.setUpperBound(x - 1);
      }

      if(s.stat.get() > yAxis.getUpperBound())
         yAxis.setUpperBound(s.stat.get() + 1);
    }

    // Provide series summation if required

    if (sumSeries != null)
    {
      sumSeries.series.getData().add(new XYChart.Data<>(x, sum));

      if (sumSeries.series.getData().size() > MAX_DATA_PTS)
      {
        sumSeries.series.getData().remove(0);
//        xAxis.setLowerBound(x - MAX_DATA_PTS);
//        xAxis.setUpperBound(x - 1);
      }
      else if (sumSeries.series.getData().size() > xAxis.getUpperBound())
      {
//        xAxis.setUpperBound(x - 1);
      }
    }
    x++;
  }

  /**
   * Holder for series/statistics pairs.
   */
  protected class StepSeries
  {
    XYChart.Series<Long, Long> series;
    Statistic                  stat;

    StepSeries(XYChart.Series<Long, Long> series, Statistic stat)
    {
      this.stat = stat;
      this.series = series;
    }

    void printSeries()
    {
       System.out.print(stat.name + ":");
       for(int i = 0 ; i < series.getData().size(); i++)
       {
          System.out.print(" " + series.getData().get(i).getYValue());
       }
       System.out.println();
    }

    void printSeries(File file) throws java.io.IOException
    {
      file.createNewFile();

      BufferedWriter bw 
           = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), false));

      StringBuilder sb = new StringBuilder();
      sb.append(stat.name);
      sb.append(';');
      for (int i = 0; i < series.getData().size(); i++)
      {
        sb.append(series.getData().get(i).getYValue());
        sb.append(';');
      }
      sb.append('\n');
      bw.write(sb.toString());

      bw.close();
    }
  }

  /*
   * FXML setters/getters
   */

  public void setEnabled(boolean value) { enabled = value; }

  public boolean getEnabled() { return enabled; }

  public void setSummation(boolean value)
  {
    summation = value;
    if (summation)
    {
      //TODO: extend to all line styles
      addSeries("Total", null);
      sumSeries.series.getNode().setStyle("-fx-stroke-width: 0.75; -fx-stroke: black; -fx-stroke-dash-array: 8 4");
    }
  }

  public boolean getSummation() { return summation; }


  public void setMaxDataPts(long MAX_DATA_PTS)
  {
    this.MAX_DATA_PTS = MAX_DATA_PTS;
  }

}
