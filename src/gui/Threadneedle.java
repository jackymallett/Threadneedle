/* Program: Threadneedle
 *
 * Main entry point for Threadneedle Simulations.
 *
 * Author : Jacky Mallett
 * Date   : September 2013
 */

package gui;

import java.lang.*;
import java.util.*;
import java.io.*;

import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.util.logging.Level;
import java.util.logging.Logger;

import charts.*;

public class Threadneedle extends Application
{
  static public Preferences preferences = new Preferences();

  private final String defaultPreferencesFile = ".threadpref";

  public static void main(String[] args)
  {
    Application.launch(Threadneedle.class, args);
  }

  @Override
  public void start(Stage primaryStage)
  {
    String batchfile = null;
    boolean cmdline = false;
    boolean showCharts = false;


    // determine network exists
    // send packet to server: provide ip,mac, local information
    // cookie hunting??
    // request version #

    // Display as part of screen this version/current version
    //   - significant changes


    try
    {
      Map<String, String> args = getParameters().getNamed();
      List<String> args2 = getParameters().getUnnamed();

      // todo: parameterise modelconfig file for cli/batch
      // Process command line arguments:
      // --b=<file>   Batch file mode
      // --cl         Command line (no gui)

      if (args.containsKey("b"))
      {
        batchfile = args.get("b");
        cmdline = true;
      }

      for (String a : args2)
      {
        if (a.equals("--cl")) cmdline = true;
        if (a.equals("--charts")) showCharts = true;
      }

      // Get initial setup information. For batch runs
      // contents of modelConfig are used directly, gui runs
      // allow the user to change parameters 

      ModelConfig m = new ModelConfig();

      if (batchfile == null)
        m.showAndWait();

      ChartController charts = new ChartController();
      MainController  mc = new MainController(m, charts);

      // Load default user preferences, and refresh charts
      // to display them. If the default preference file
      // doesn't exist create it.
      File f = new File(defaultPreferencesFile);

      if(!f.exists())
         preferences.savePreferences(defaultPreferencesFile);
      else
         preferences.loadPreferences(defaultPreferencesFile);

      // Set hook to save preferences on exit.
      Runtime.getRuntime()
           .addShutdownHook(new Thread()
            {
              public void run()
              {
                preferences.savePreferences(defaultPreferencesFile);
              }
            });

      charts.refresh();

      ScreenSettings sc = new ScreenSettings();
      // Todo: size to screen
      mc.setX(50);
      mc.setY(50);

      //this.setWidth(300.0); 

      // Start command line interface as a separate thread if
      // specified.
      if(cmdline) 
      {
        CLI cli = new CLI(null, charts, batchfile);
        Thread t = new Thread(cli);
        t.setName("CLI-Thread");
        t.start();
      }

      if(batchfile == null)
      {
       charts.display();
       charts.setX(sc.screenWidth);
       charts.setY(100);
       charts.setHeight(700);
       charts.setResizable(true);
       mc.showAndWait();
      }
      else if (showCharts)
      {
        charts.display();
        charts.setX(250);
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(Threadneedle.class.getName()).log(Level.SEVERE,
                                                         null, ex);
    }
  }
}
