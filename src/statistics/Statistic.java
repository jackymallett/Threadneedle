/* Program   : Threadneedle
 *
 * Statistic : Statistic handler.         
 * 
 * Author    : Jacky Mallett
 * Date      : July 2013    
 *
 * Comments:   Changes and bugs here effect the entire simulation, as 
 *             statistics are also used within models for input to behaviours
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package statistics;

import base.Base;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.LinkedList;

public class Statistic
{
  public String name;                      // Name of statistic being set
  public String group;                     // Chart Group statistic belongs too
  public Type   type;                      // Type of statistic

  private long count;
  private long value;
  private int  currentStep;

  public  int  stepModulo = 1;             // Accumulate statistic over N steps

  private LinkedList<Long> values  = new LinkedList<>();
  public  LinkedList<Long> history = new LinkedList<>();

  public static int historyLength = 120; // Keep last 120 values for ref

  public static HashMap<String, Statistic>       names    = new HashMap<>();  // Common directory
  public static ObservableMap<String, Statistic> obsNames = FXCollections.observableMap(names);

  /*
   * Interface to the statistics is statistic.add(value). The operation
   * performed by the add() is controlled by the type of the statistic
   * as below:
   *
   * COUNTER   All values are summed.
   * AVERAGE   All add operations for each step are averaged together
   * SINGLE    Statistic represents a single value/round. If multiple
   *           adds occur in the same round the previous value will be
   *           overwritten.
   * NUMBER    Single value maintained between rounds, behaves like a
   *           number. (Add -ve value to get subtraction).
   */
  public enum Type
  {
    COUNTER,		
    AVERAGE,	   
    SINGLE,		  
    NUMBER,		 
  }

  /**
   * Constructor
   *
   * @param n label for statistic
   * @param g group statistic belongs to (for aggregates like GDP)
   * @param t type of statistic
   */

  public Statistic(String n, String g, Type t)
  {
    name  = n;
    group = g;
    type  = t;

    currentStep = Base.step;

    // Remove statistic if it is already in the list, and replace

    if (obsNames.get(name) != null)
      removeStatistic(name);

    // All statistics must be held in list, in order for rollover to
    // work - chart controller must select correct stats for display.

    obsNames.put(name, this);
  }

  /**
   * Constructor for blank groups
   *
   * @param name label for statistic
   * @param t    type of statistic
   */
  public Statistic(String name, Type t)
  {
    this(name, null, t);
  }

  /**
   * Return statistics with specified name.
   *
   * @param name  Name for statistic
   * @param group Group for statistic
   * @param t     Type of statistic
   * @return Statistic
   */
  public static Statistic getStatistic(String name, String group, Type t)
  {
    if (obsNames.get(name) != null)
      return obsNames.get(name);
    else if (group != null)
    {
      return new Statistic(name, group, t);
      // Debugging

      // System.out.println("Registered statistics: ");
      // for(String s : names.keySet())
      // System.out.println("\t" + s);
      // System.out.println("Err: No statistic registered with name " +
      // name);
    }
    else
      return new Statistic(name, t);
  }

  /**
   * Return statistics with specified name.
   *
   * @param name   Name for statistic
   * @param group  Group for statistic
   * @param t      Type of statistic
   * @param modulo specify no. steps statistic should span
   * @return Statistic
   */
  public static Statistic getStatistic(String name, String group, Type t,
                                       int modulo)
  {
     Statistic stat = getStatistic(name, group, t);
     stat.stepModulo = modulo;

     return stat;
  }

  /**
   * Return statistic by name only.
   *
   * @param   name  Name of statistic
   * @return  Statistic or null if no match
   */

  public static Statistic getStatistic(String name)
  {
    if (obsNames.get(name) != null)
      return obsNames.get(name);
    else
      return null;
  }

  /**
   * Return the value for the statistic for the last round. Note: current
   * round may not yet be completely accumulated.
   *
   * @return last round's value
   */
  public long get()
  {
    try
    {
      return history.getLast();
    }
    catch (Exception NoSuchElementException)
    {
      return 0;
    }
  }

  /**
   * Return the current accumulated value for this round. Note: may
   * not be completely accumulated if called during round evaluation within
   * agent code, but should be safe if used in SimulationEngine after 
   * evaluation occurs.
   *
   * @return current value of statistic
   */

   public long getCurrent()
   {
      return value;
   }
  

  /**
   * Return history value from linked list, indexes go back relative to last
   * value added. ie. 0 is last round, 1 is round before last, etc.
   *
   * @param n position in list to return. If n is greater than the history size 
   *          then last value in the history is returned.
   * @return value at n'th position in list
   */

  public long get(int n)
  {
    if (history.size() == 0) // No history
      return 0;
    if (n >= history.size())
      return history.getFirst();
    else
      return history.get(history.size() - 1 - n);
  }

  /**
   * Return no. of data points retained in statistic's history.
   *
   * @return no. of data points currently held for statistic.
   */
  public int size()
  {
    return history.size();
  }

  /**
   * Test to see if value has decreased between last and n steps back. If
   * there is insufficient history to determine this, return false.
   *
   * @param n number of steps back
   * @return t/f whether current value is lower than n'th value back
   */

  public boolean shrinking(int n)
  {
    return n < history.size() && history.getLast() < get(n);
  }

  /**
   * Test to see if value has increased between last and n steps back. If
   * there is insufficient history to determine this, return false.
   *
   * @param n number of steps back
   * @return t/f whether current value has increased from n'th value back
   */

  public boolean increasing(int n)
  {
    return n < history.size() && history.getLast() > get(n);
  }

  public void inc()
  {
    value++;
  }

  /**
   * Add a value to this statistic.
   *
   * @param stat Value to add
   */
  public void add(long stat)
  {
    switch (type)
    {
      case COUNTER:
        value += stat;
        break;

      case AVERAGE:
        values.add(stat);
        break;

      case SINGLE:
        value = stat;
        break;

      case NUMBER:
        value = value + stat;
        break;
    }
  }

  public void reset()
  {
    values.clear();
	history.clear();
    value = 0;

    currentStep = Base.step;
  }

  /**
   * Rollover statistic counter. Move this round's value to history, and
   * initialise for the next round.
   */

  public void rollover()
  {

    if((Base.step % stepModulo) == 0)
    {
	    long avg = 0;

		switch (type)
		{
		  case COUNTER:
		  case SINGLE:
			//if (value == 0 && history.size() > 0)
			//   history.add(history.getLast());
			//else
			history.add(value);
			break;

		  case AVERAGE:
			for (Long v : values)
			{
                avg += v;
            }

            if (values.size() > 0)
              history.add(avg / values.size());
            else
              history.add(0L);

            break;

          case NUMBER:
              history.add(value);
              break; 
		}

        if(type != Type.NUMBER)
        {
           values.clear();
           value = 0;
        }

        if (history.size() > historyLength)
            history.removeFirst();
    }
  }


  public static void rolloverAll()
  {
    names.values().forEach(statistics.Statistic::rollover);
  }

  /**
   * Clear all statistics - used for simulation reset
   */

  public static void resetAll()
  {
    for(Statistic s : names.values())
        s.reset();

    obsNames.clear();
  }

  /**
   * Remove specified statistic.
   *
   * @param name Statistic to remove
   */

  public static void removeStatistic(String name)
  {
    obsNames.remove(name);
  }

  public static void saveToCsv(String Filename)
  {
    // TODO: Implement this
    throw new NotImplementedException();
  }
}

