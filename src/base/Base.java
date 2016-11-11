/* Program: Threadneedle
 *
 * Base class containing objects entire program has access to. Use sparingly.
 *
 * resetAll() to return to initial parameters.
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

package base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.*;
import java.util.stream.*;
import java.io.*;

import au.com.bytecode.opencsv.CSVReader;


public final class Base
{
  @Expose public static String VERSION     = "0.1";
  @Expose public static boolean   debug    = false;
  @Expose public static int       seed     = 271828183; // RNG default seed 

  public final static String AGENT_ROOT  = "Thread.core"; // Agent class
  public final static String MAIN_CONFIG = ".Threadneedle";

  // Current simulation step. Steps start from 1, partly to avoid some
  // divide by 0 errors at simulation start, and why not since it also 
  // corresponds with real world calender conventions.

  public static int step = 1;

  public static int interval = 50;

  public static Random random = new Random(seed);

  public static  String         debugFileName  = null;
  public static  boolean        debug_internal = false; 
  private static BufferedWriter debugfptr      = null;

  public static boolean profile = false;	   // turn on profiling information

  // Default deposits (of money) for initialisation.
  public static long DEFAULT_MARKET_DEPOSIT = 100;

  // This puts an implicit limit on the number of Agents in a single instance
  // of 1,000,000. 

  private static Integer latestId = 1;        // Agent id base
  private static Integer wid      = 1000000;  // Widget id base

  // Provide user friendly time periods for interaction with simulation

  public enum Time
  {
     DAY(1),
     MONTH(30),
     YEAR(360);

     private final int period;

     Time(int period)
     {
        this.period = period;
     }

     public int period(){return period;}

     public static boolean endOfMonth()
     {
        return (step % MONTH.period()) == 0;
     }

     public static boolean endOfYear()
     {
        return (step % YEAR.period()) == 0;
     }
  };


  /**
   * Reset base parameters back to initial configuration.
   */
  public static void resetAll()
  {
    step     = 1;
    latestId = 1;
    wid      = 1000000;
  }

  /**
   * Print out debug statement if debugging enabled.
   * 
   * @param text Text to print out
   */

  public static void DEBUG(String text)
  {
    if (debug)
    {
      String output = "DBG[" + step + "]:" + text;

      if (debugfptr == null)
        System.out.println(output);
      else
      {
        try
        {
          debugfptr.write(output + "\n");
          debugfptr.flush();
        }
        catch (IOException e)
        {
          System.err.println("Failed to write to debug file: "
                             + debugFileName);
          System.err.println(output);
        }
      }
    }
  }

  public static void DEBUGI(String text)
  {
    if (debug_internal)
    {
      String output = "DBG[" + step + "]:" + text;
      System.out.println(output);
    }
  }

  public static void toggleDebug()
  {
    debug = !debug;
  }

  /**
   * Set file for debug output.
   *
   * @param filename File as debug file to use
   */

  public static void setDebugFile(String filename)
  {
    /*
     * If there is already a debug file open, check to see that the new
		 * filename is different, and close the old one as necessary.
		 */
    try
    {
      if (debugfptr != null)
      {
        if (filename.equals(debugFileName))
        {
          System.out.println("Debug output file unchanged "
                             + filename);
          return;
        }
        else
        {
          System.out.println("Closing debug file " + debugFileName);
          debugfptr.close();
          debugfptr = null;
        }
      }

      debugFileName = filename;

      File file = new File(filename);

      FileWriter fw = new FileWriter(file);
      debugfptr = new BufferedWriter(fw);

    }
    catch (IOException e)
    {
      System.err.println("Failed to set debug file: " + filename);
    }
  }

  /**
   * Clear debug file, reset to command line.
   */
  public static void clearDebugFile()
  {
    // Is there an existing debug file to close
    if (debugfptr == null)
      return;

    try
    {
      System.out.println("Closing debug file: " + debugFileName);

      debugfptr.flush();
      debugfptr.close();

      debugfptr = null;
      debugFileName = null;
    }
    catch (IOException e)
    {
      System.err.println("Failed to clear debug file: " + debugFileName);
      System.err.println(e.getMessage());
    }
  }

  /**
   * Provide a single random number generator for the simulation.
   *
   * @param s seed for rng
   */
  public static void setRandom(int s)
  {
    seed = s;
    random = new Random(seed);
  }

  /**
   * Assign simulation engine wide unique ID.
   *
   * @return unique ID for agent.
   */
  public static int assignID()
  {
    return latestId++;
  }

  /**
   * Public ability to set the ID for loading from file.
   *
   * @param id Set the latest ID number to issue to agents
   */
  public static void setID(int id)
  {
    latestId = id;
  }

  /**
   * Return the next ID number that will be assigned. Used for assigning
   * default names to agents.
   *
   * @return Next ID number
   */
  public static int getNextID()
  {
    return latestId;
  }

  /**
   * Assign a widget ID 
   *
   * @return Widget ID for use
   */

  public static int assignWidgetID()
  {
    return wid++;
  }

  /**
   * Return sum of integer array.
   *
   * @param  array Array to sum
   *
   * @return total of contents
   */

  public static int sum(int[] array)
  {
    return IntStream.of(array).parallel().sum();
  }

  /**
   * Return sum of array from index
   *
   * @param array array to sum
   * @param index to sum from
   * @return total
   */

  public static int sum(int[] array, int index)
  {
    int sum = 0;

    for (int i = index; i < array.length; i++)
      sum += array[i];

    return sum;
  }

  public static long sum(long[] array)
  {
    return LongStream.of(array).parallel().sum();
  }

  public static long sum(long[] array, int index)
  {
    long sum = 0;

    for (int i = index; i < array.length; i++)
      sum += array[i];

    return sum;
  }

  public static double sum(double[] array)
  {
    return DoubleStream.of(array).parallel().sum();
  }

  /**
   * Sanitize user input to be positive or 0 (if negative or NaN)
   *
   * @param text Text input from gui
   * @return integer value for supplied input
   */
  public static int positiveInput(String text)
  {
    int no;

    text.replaceAll("\\s+","");

    try
    {
      no = Integer.parseInt(text);
    }
    catch (NumberFormatException e)
    {
      no = 0;
    }

    if (no < 0) no = 0;

    return no;
  }

  /**
   * Load an int data set from specified external file.
   *
   * @param filename  filename with series
   * @return data loaded from file
   */

  public ArrayList<Double> loadSeries(String filename)
  {
    CSVReader csv; // CSV file reader
    String[] line;
    int i;

    ArrayList<Double> series = new ArrayList<>(600);

    try
    {
      csv = new CSVReader(new FileReader(filename));

      while ((line = csv.readNext()) != null)
      {
        // Ignore blank lines
        if (((line.length == 1) && (line[0].trim().length() == 0))
            || line[0].charAt(0) == '#')
          continue;

        // Remove spaces
        for (i = 0; i < line.length; i++)
          line[i] = line[i].trim();

        assert (i > 1) : "Too many fields in data series file: "
                         + filename;

        series.add(Double.valueOf(line[0]));
      }
      csv.close();
      System.out
        .println("File: " + filename + " loaded " + series.size());
      return series;
    }
    catch (Exception e)
    {
      System.out.println("** Failed to load specified series file: "
                         + filename + " **");
      return null;
    }
  }
}
