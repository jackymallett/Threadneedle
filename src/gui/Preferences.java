/**
 * Program: Threadneedle
 *
 * Preferences class.
 *
 * Provide preference handling for gui/command line simulation settings.
 *
 * Author : Jacky Mallett (c)
 * Date   : April 2015
 *
 * Todo   : add menu item to allow configuration of directories, etc.
 */
package gui;

import charts.*;

import java.util.*;
import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Preferences
{
  @Expose String configDirectory = ".";
  @Expose HashMap<String, Boolean> charts = new HashMap<>(); // Chart settings

  /**
   * Save preferences class exportable objects to supplied filename. File
   * will be created in the path supplied.
   *
   * @param filename File to save to.
   */
  public void savePreferences(String filename)
  {
    try
    {
       FileWriter fw = new FileWriter(new File(filename));   

       BufferedWriter bw = new BufferedWriter(fw);

       for(StepChart chart : ChartController.charts.values())
       {
          charts.put(chart.getId(), chart.enabled);
       }

       Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                                    .setPrettyPrinting()
                                    .create();

       bw.write(gson.toJson(this));
       bw.close();
       fw.close();
    }
    catch (Exception e)
    {
       System.out.println("Unable to write to file: " + filename);
    }

  }

  /**
   * Load preference exportable objects from supplied filename
   *
   * @param filename  Filename to load preferences from.
   */
  public void loadPreferences(String filename)
  {
    Gson gson = new Gson();

    try
    {
       BufferedReader br = new BufferedReader(new FileReader(filename));
       Preferences pref = gson.fromJson(br, Preferences.class);

       this.configDirectory = pref.configDirectory;

       for(Map.Entry<String, Boolean> entry : pref.charts.entrySet())
       {
          ChartController.setEnabled(entry.getKey(), entry.getValue());
       }
    }
    catch (Exception e)
    {
       System.out.println("Unable to read from file " + filename);
    }

  }
}
