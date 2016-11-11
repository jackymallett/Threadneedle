/* Program  : Threadneedle
 *
 * CLI      : Provide command line interface control over simulation for
              batch runs.
 * 
 * Author   : Jacky Mallett
 * Date     : November 2012
 * Comments : All commands are passed the command line, even those that
 *            don't have one.
 * 
 * Todo:      internal self check on commands available and help message
 * TODO:      refactor config - show current configuration (modified)
 * Todo:      re-write to use parser
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import au.com.bytecode.opencsv.CSVReader;
import base.Base;
import charts.ChartController;
import charts.StepChart;
import core.*;
import javafx.application.Platform;
import statistics.Statistic;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gui.Common.simeng;

public class CLI implements Runnable
{
  ChartController charts = null;
  InputStream input = null;
  BufferedReader br = null;
  boolean debug = false;
  int DEFAULT_STEPS = 1;     // Default Step to run simulation for step command

  String[] cmds =
  {
     //"write file    : write new config to file",
     //"show          : show parameter in runtime simulation",
     // "report [file]          : print report on simulation optionally to file",

    "step [n]                 : Step simulation n steps (default 1200)",
    "steps [n]                : Step simulation n steps while updating charts",
    "repeat times command     : repeat a command multiple times",
    "forall regex pattern command : provide simple for loop capability",
    "wait [time]              : wait a while before continuing reading commands",
    "reset                    : reset simulation",
    "set                      : set parameters in simulation",
    "load file                : load new config file",
    "preferences  filename    : load Threadneedle parameters from file",
    "config                   : show current parameters for simulation",
    "statistics               : show statistics registered with simulation",
    "",
    "printmoney agent-id x    : increase agent's deposit by x",
    "addagent type bankname [options] : add an agent with [options] as properties key=value map",
    "addneed agent-id name purchase store consume consumable useloan: add need to identified agent",
    "addwant agent-id name purchase store consume consumable useloan: add want to identified agent",
    "agentinfo agent-id     : invoke toString() on agent-id",
    "debug [file]           : toggle debugging information on and off",
    "agentinfo agent-id     : invoke toString() on <agent>",
    "htmlcharts [dir]       : write charts out in html/png format",
    "report [file]          : print report on simulation optionally to file",
    "setbaserate            : change the base interest rate",
    "shareholders stockmarket : print the shareholders of the given stockmarket",
    "savechartdata [dir]        : write chart's series data out to a file for that chart as png",
    "",
    "savechartcsvdata [dir] : write chart's series data out to a file for that chart as csv",
    "printorders agent-id    : print the orders listed on stockmarket agent-id or put up by agent-id",
    "quit               : exit simulation",
  };

  /**
   * Constructor
   *
   * Create and run a command line interface for the simulation.
   * Nb. charts is written to handle charts not being displayed, and its
   * easier to pass it through, than rewrite SimulationEngine for charts 
   * being null.
   *
   * @param csv    csv for supplied config file | null
   * @param charts Chart controller for SimulationEngines
   * @param batchfile optional - batchfile containing commands to execute
   *
   * Todo: refactor to allow config files to be loaded
   */

  public CLI(CSVReader csv, ChartController charts, String batchfile)
  {
    this.charts = charts;

    // Are we running in batch file mode?

    if (batchfile != null)
    {
      Ledger.postTransactions = false;
      try
      {
        input = new FileInputStream(batchfile);
        Statistic.historyLength = Integer.MAX_VALUE;
      }
      catch (Exception e)
      {
        System.err.println("Unable to find batch file: " + batchfile);
        System.exit(0);
      }
      System.err.println("Reading commands from batchfile: " + batchfile);
    }
    else
    {
      input = System.in;
    }

    br = new BufferedReader(new InputStreamReader(input));
  }

  /**
   * Main Loop for command line and batch processing.
   */

  public void run()
  {
    boolean finished = false;
    String cmd = null;
    String line = null;

    while (!finished)
    {
      // don't show "> > > > > > > " in batch mode
      if (input == System.in)
      {
        System.out.print("> ");
      }

      try
      {
        line = br.readLine();

        // This should only happen in batch. Control is switched back
        // to the command line, if the batch file didn't end with quit.
        if (line == null)
        {
          br.close();
          input = System.in;
          br = new BufferedReader(new InputStreamReader(input));
          System.out.flush();
          System.out.print("> ");
          continue;
        }
        // Ignore comments
        else if ((line.length() > 0) && (line.charAt(0) == '#'))
        {
          if (line.toLowerCase().startsWith("# debug on"))
          { // enable debug output via comment
            debug = true;
          }
          continue;
        }
        // debug mode - print out command
        if (debug && input != System.in)
        {
          System.out.println(line); 
        }

        processCommand(line);
      }
      catch (Exception e)
      {
        // Ctrl-D triggers "Stream closed" exception.
        System.out.println(e.getMessage());
        System.exit(0);
      }
    }
  }

  private boolean processCommand(String line)
  {
    String cmd;
    try
    {
      cmd = line.split("\\s+")[0];
    }
    catch (Exception e)
    {
      return false;
    }

    try
    {
      Method m = this.getClass().getMethod(cmd, String[].class);
      
      runAndWait(() -> 
      {
         try
         {
              String[] fields = line.split("\\s+");

			  final long startTime = System.currentTimeMillis();

              Object o = m.invoke(this, (Object)fields);

			  final long endTime = System.currentTimeMillis();

			  if((Base.profile == true) && (endTime - startTime > 1000))
				  System.out.println(line + "\t: " + ((endTime - startTime)/1000) + " s");

         }
         catch (IllegalAccessException e)
         {
           System.err.println("Incorrect access exception: " + cmd + " " +
                         line);
         }
         catch (InvocationTargetException e)
         {
            System.err.println("Invocation exception " + e.getTargetException());
            System.err.println("\t " + cmd);
            e.printStackTrace();
         }
      });
    }
    catch (NoSuchMethodException e)
    {
      // Ignore blank lines
      if (cmd.length() > 1)
      {
        System.out.println("No such command: " + cmd);
        printCommands();
      }
    }
    catch (Exception e)
    {
      System.err.println(e);
    }
    return true;
  }

  /**
   * Print out a list of the commands understood by the CLI.
   */
  private void printCommands()
  {
    for (String cmd : cmds)
    {
      System.out.println(cmd);
    }
  }

  /**
   * Toggle debugging on and off, optionally save to file rather than
   * output to terminal.
   *
   * @param line	command line
   */

  public void debug(String[] line)
  {
    Base.toggleDebug();
    System.out.println("Debug " + Base.debug);

    if (line.length == 2)
    {
      Base.setDebugFile(line[1]);
      System.out.println("Debug is " + Base.debugFileName);
    }
    else if (line.length == 1)
    {
      Base.clearDebugFile();
    }
    else
    {
      System.err.println("Incorrect number of parameters for debug");
      System.err.println(getHelp("debug"));
    }
  }

  /**
   * report - output on simulation run
   * - iterate through defined charts and output results
   *
   * @param line	command line
   */

  public void report(String[] line)
  {
    //simeng.chartCtrl.printAllCharts();
    //simeng.chartCtrl.printEquilibrium();
  }


  /**
   * Save the data for each chart's series
   * to a csv file for that chart.
   *
   * @param line Directory for file to be written to (optional)
   */
  public void savechartdata(String[] line)
  {
    String dir = "../";

    if (line.length == 2)            // Append directory name if supplied
      dir = line[1];

    charts.saveAllCharts(dir, 320,240);
  }

  public void savechartcsvdata(String[] line)
  {
    String dir = "../";

    if (line.length == 2)
      dir = line[1];

    String filename = "";

    try
    {
      File fdir = new File(dir);
      fdir.mkdirs();

      for (StepChart chart : ChartController.charts.values())
      {
        ChartController.saveCsv(dir + "/" 
                  + chart.getTitle().trim().replace(" ", "_") + ".csv", chart);
      }

    }
    catch (IOException ex)
    {
      System.err.println("Failed to write file: " + filename);
      ex.printStackTrace();
    }
  }

  /**
   * Set preferences from supplied file
   *
   * @param line  filename  containin preferences
   */

   public void preferences(String[] line)
   {
     Preferences pref = new Preferences();
     pref.loadPreferences(line[1]);
     charts.refresh();
   }


  /**
   * Save results as html readable page
   * Nb. current setup may change during simulation run
   *
   * eg. htmlcharts output/market2 "Control"
   *
   * @param line  Directory to create and put results into
   */
  public void htmlcharts(String[] field)
  {
    BufferedWriter bw = null;
    String dir = null;
    File config = null;

    // Get comment if specified.
    String comment = "";

    if(field.length > 2)
		for(int i = 2; i < field.length; i++)
          comment += field[i] + " ";

    dir = "../";

    try
    {
      // See if an output file was provided, if not write to stdout

      if (field.length >= 1)
      {
        dir = field[1];

        File directory = new File(dir);
        directory.mkdirs();

        config = new File(dir + "/test_config");

        // Overwrite config file if it already exists
        if (config.exists()) config.delete();
        config.createNewFile();
      }
      else
        dir = null;


      if (dir != null)
      {
		System.out.println("Saving simulation run to: " + dir);
        FileWriter fw = new FileWriter(config.getAbsoluteFile());
        bw = new BufferedWriter(fw);
      }
      else
      {
        bw = new BufferedWriter(new OutputStreamWriter(System.out));
      }

      if(comment != null) bw.write(comment + "\n");

      bw.write("Total deposits: " + simeng.govt.getDepositSupply() + "\n");
      bw.write(simeng.getCurrentSetup());


      bw.flush();
      bw.close();
    }
    catch (IOException e)
    {
      System.err.println("Unable to save config file for test in: " + dir);
      e.printStackTrace();
    }

    charts.saveAllCharts(dir, 320, 240);
  }

  /**
   * Load simulation from configuration file. The contents of the file are
   * added to the current simulation, and multiple files can be loaded
   * sequentially. Reset must be done separately if required.
   *
   * @param cmdline Configuration filename
   */

  public void load(String[] cmdline)
  {
    if (!simeng.loadSimulation(new File(cmdline[1])))
       System.out.println("Failed to load simulation from file: " + cmdline[1]);
  }

  /**
   *  Increase agent's deposit by creating cash and depositing
   *  it at the agent's bank. No other action takes place in
   *  the system. (i.e. this isn't quantitative easing).
   *  @param fields command arguments (if present)
   */
  public void printmoney(String[] fields)
  {

    Agent agent = simeng.getAgent(fields[1]);
    Long amount = Long.parseLong(fields[2]);

    agent.getBank().printMoney(agent.getAccount(), amount, "Print Money - CLI");
  }

  /**
   * Show simulation configuration
   * 
   * @param fields fields supplied to command line (unused)
   */
  public void config(String[] fields)
  {
    simeng.printCurrentConfig();
  }

  /**
   * statistics - print out information on simulation statistics.
   * 
   * @param fields fields supplied to command line (unused)
   */
   public void statistics(String[] fields)
   {
     for(Statistic s: Statistic.names.values())
     {
        System.out.println(s.name + " " + s.history.size());
     }
   }

  /**
   * reset  - reset simulation to config values.
   * 
   * @param fields fields supplied to command line (unused)
   */
  public void reset(String[] fields)
  {
    simeng.resetAll();
  }

  /**
   * set variable in agent
   * i.e. set A1 initialDeposit 10
   * @param fields fields supplied to command line
   */

  public void set(String[] fields)
  {
    Agent agent = simeng.getAgent(fields[1]);

    if (agent == null) {
      if (fields[1].compareToIgnoreCase("seed") == 0) {
        try {
          int seed = fields.length >= 3 ? Integer.parseInt(fields[2]) : (int) System.currentTimeMillis();
          Base.setRandom(seed);
          System.out.println("SIMULATION SEED SET TO: " + seed);
        } catch (NumberFormatException e) {
          System.err.println("Invalid operand should be int: " + fields[2]);
          System.err.println(fields[2]);
        }
        return;
      }
      else if (fields[1].compareToIgnoreCase("maxdatapoints") == 0)
      {
        try
        {
          long max = fields.length >= 3 ? Long.parseLong(fields[2]) : Long.MAX_VALUE;

          for (charts.StepChart chart : ChartController.charts.values())
          {
            chart.setMaxDataPts(max);
          }
        }
        catch (NumberFormatException e)
        {
          System.err.println("Invalid operand should be long: " + fields[2]);
          System.err.println(fields[2]);
        }
        return;
      }

      System.err.println("Unknown agent in cmdline: " + fields[1]);
      System.err.println(fields[1]);
      return;
    }

    try
    {
      Field field = agent.getClass().getField(fields[2]);
      field.setAccessible(true);
      // Todo: Currently assumes value is int

	  if(field.getType() == Integer.TYPE)
	  {
        field.setInt(agent, Integer.parseInt(fields[3]));
	  }
	  else if (field.getType() == Boolean.TYPE)
	  {
        field.setBoolean(agent, Boolean.parseBoolean(fields[3]));
	  }
	  else
		  System.out.println("Unhandled type for field in set");
    } 
	catch (Exception e) 
	{
     System.err.println("Failed to find field: " + fields[2]);
     System.err.println(e);
    }

  }

  public void setbaserate(String[] fields)
  {
    core.CentralBank cb = simeng.govt.centralbank;

    int rate;
    if (fields.length >= 2)
    {
      try
      {
        rate = Integer.parseInt(fields[1]);
        cb.setBaseRate(rate);
      }
      catch (NumberFormatException ex)
      {
        System.err.println("Couldn't set the rate: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
    else
    {
      System.err.println("Missing argument of type int.");
    }

  }

  /** Pending -- jm
  public void copyagent(String[] fields)
  {
     Agent agent = simeng.objs.get(fields[1]);

	 String name = agent.getSimpleName() + "-" + getNextID();

  }
  **/
  /**
   * Add an agent to the simulation
   * 
   * Format: addagent  type bankname [options]
   * [options] is in the format key=value key2=value2 ...
   * Example: addagent StockMarket product=Shares initialDeposit=0
   * todo: check example - bankname??
   *
   * @param fields  fields supplied for command
   */
  public void addagent(String[] fields)
  {
    // Determine what kind of agent is being added

    Class agentType;

    try
    {
      agentType = Class.forName("core." + fields[1]);

      if (!Agent.class.isAssignableFrom(agentType))
      {
        throw new ClassNotFoundException(agentType + " does not extend Agent");
      }
    }
    catch (ClassNotFoundException e)
    {
      System.err.println("Failed to find agent class: " + fields[1]);
      System.err.println(e);
      return;
    }
    catch (Exception e)
    {
      System.err.println("Unknown exception occurred!");
      System.err.println(e);
      return;
    }

    // Configure the agent as necessary

    Map<String, String> properties = new HashMap<>();

    for (int i = 2; i < fields.length; ++i)
    {
      String[] property = fields[i].split("=", 2);

      if (property.length < 2) continue; // no '=' found

      properties.put(property[0], property[1]);
    }

    if (agentType == Bank.class) 
    {
      simeng.addEntity(agentType, properties, null, properties.get("name"));
      return;
    }

    if(simeng.govt.getBank(fields[2]) == null)
       System.out.println("Error: bank not found");
    else
    {
       try
       {
          simeng.addEntity(agentType, properties,
                        simeng.govt.getBank(fields[2]),properties.get("name"));
       }
       catch (Exception e)
       {
          System.out.println("Failed to add agent");
       }
    }
  }

  /**
   * Add need to designated agent
   *
   * addneed agent-id need purchase consume store consumable useLoan
   *
   * todo: add frequency
   *
   * e.g. addneed ID-1 food 1 1 2 true false
   *
   * @param fields command fields
   */
  public void addneed(String[] fields)
  {
     Agent agent = simeng.getAgent(fields[1]);

     boolean consumable = Boolean.parseBoolean(fields[6]);
     boolean useLoan    = Boolean.parseBoolean(fields[7]);

     if(agent instanceof Person)
     {
        ((Person)agent).profile.addNeed(fields[2], Integer.parseInt(fields[3]),
                                        Integer.parseInt(fields[4]), 
                                        Integer.parseInt(fields[5]), 1, 
                                        consumable, useLoan);
     }
     else System.out.println("Needs only supported for Person class agents");
  }


  /**
   * Add want to designated agent
   *
   * addwant agent-id need purchase consume store consumable useLoan
   *
   * todo: add frequency, allow wants to take out loans
   *
   * e.g. addneed ID-1 food 1 1 2 true false
   * @param fields fields supplied to command line
   */
  public void addwant(String[] fields)
  {
     Agent agent = simeng.getAgent(fields[1]);

     boolean consumable = Boolean.parseBoolean(fields[6]);
     boolean useLoan    = Boolean.parseBoolean(fields[7]);

     if(agent instanceof Person)
     {
        ((Person)agent).profile.addWant(fields[2], Integer.parseInt(fields[3]),
                                        Integer.parseInt(fields[4]), 
                                        Integer.parseInt(fields[5]), 1,
                                        consumable, useLoan);
     }
     else System.out.println("Wants only supported for Person class agents");
  }


  public void shareholders(String[] fields)
  {
    for (int i=1; i < fields.length; ++i) {
      String field = fields[i];
      try {
        // who owns shares called `field`
        StockMarket m = StockExchange.findMarket(field, simeng.govt);
        if (m != null) {
          List<Agent> owners = new LinkedList<>();
          for (Shares s : m.sharesIssued) {
            Agent owner = s.getOwner();
            owners.add(owner);
          }

          Collections.sort(owners, (o1, o2) -> o1.name.compareTo(o2.name));

          for (Agent a : owners) {
            System.out.println(String.format("%5d", a.getShareholding(field)) + "x[" + field + "] owned by " + a.getName());
          }
        }
      }
      catch (Exception e) {
        System.err.println("Couldn't gather information for " + field);
        e.printStackTrace();
        System.err.println(e.getMessage());
      }
    }
  }

  public void printorders(String[] fields)
  {
 
    try {
      StockMarket sm = StockExchange.findMarket(fields[1], simeng.govt);
      if (sm != null) {
        sm.printOrders();
      } else {
        Agent a = simeng.getAgent(fields[1]);
        for (StockExchange se : simeng.govt.getStockExchanges()) {
          for (StockMarket m : se.markets) {
            m.printOrders(a);
          }
        }
      }
    } catch (Exception ex) {
      System.err.println("Error: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public void agentinfo(String[] fields)
  {
  
    try
    {
      Agent a = simeng.getAgent(fields[1]);
      System.out.println(a.info());
    }
    catch (NullPointerException e)
    {
      System.err.println("Agent could not be found: " + fields[1]);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      System.err.println("Argument is required. Example: `agentinfo InvestmentCompany-5`");
    }
  }

  public void repeat(String[] fields)
  {
    try
    {
      int num_loops = Integer.parseInt(fields[1]);

      String command = fields[2];
      for (int i = 3; i < fields.length; i++)
      {
        command += ' ';
        command += fields[i];
      }

      for (int i = 0; i < num_loops; i++)
      {
        processCommand(command);
      }

    }
    catch (NumberFormatException e)
    {
      System.err.println("Couldn't convert to integer: " + fields[1]);
      System.err.println(e);
    }
    catch (Exception e)
    {
      System.err.println("Unexpected exception occurred!");
      System.err.println(e);
    }
  }

  /**
   * For loops.
   *
   * Format is: for &lt;regex&gt; &lt;command&gt;
   *
   * e.g.  for ^A set initialDeposit 10  :: set all agents beginning with A
   *
   * @param fields fields supplied to command line
   */

  public void forall(String[] fields)
  {
    Matcher matcher;
    String newcmd = "";

    Pattern pattern = Pattern.compile(fields[1]);

    // Iterate through all objects in the simulation

    for (Map.Entry<String, Agent> agent : simeng.objectList.entrySet())
    {
      Agent agt =  agent.getValue();
      matcher = pattern.matcher(agt.name);

      //         System.out.println("name: " + agt.name + " " + fields[1]);

      if (matcher.find())
      {
        //System.out.println("grp: " + matcher.group() + " " + matcher.start());
        // Create full command line for match

        newcmd = fields[2] + " " + agt.name;

        for (int i = 3; i < fields.length; i++)
        {
          newcmd += " " + fields[i];
        }

        System.out.println(newcmd);

        // Invoke command via introspection

        try
        {
          Method m = this.getClass().getMethod(fields[2], String.class);
          Object o = m.invoke(this, newcmd);
        }
        catch (Exception e)                // collapse the exception handling for this one
        {
          System.err.println("Incorrect command in for loop: " + newcmd);
          System.err.println(e);
        }
        matcher.reset();
      }
    }
  }

  /**
   * step [nsteps]
   *
   * Run simulation for supplied number of steps, or default (100 years)
   *
   * @param fields fields supplied to command
   */
  public void step(String[] fields)
  {
    int nSteps = DEFAULT_STEPS;

    assert (fields[0].equalsIgnoreCase("step")) : "Incorrect cmd in step";

      /* If a step count was provided use that, otherwise use default
       */
    if (fields.length > 1)
    {
      nSteps = Integer.parseInt(fields[1]);
    }

    for (int i = 0; i < nSteps; i++)
    {
      simeng.evaluate();
      charts.inc_x();
/*
    for (int i = 0; i < nSteps; i++) {
      Platform.runLater(() ->
          {
            simeng.evaluate();
            charts.inc_x();
          }
      );
*/
    }

    // If running in GUI update graphical display
    //if(mspanel != null)mspanel.redrawFrames();
  }

  /**
   * steps [nsteps]
   *
   * Run simulation for supplied number of steps while updating charts
   * periodically
   *
   * @param fields fields supplied to command
   */
  public void steps(String[] fields)
  {
    int nSteps = fields.length > 1 ? Integer.parseInt(fields[1]) : DEFAULT_STEPS;
    int stepSz = 10;

    while (nSteps > stepSz) {
      runAndWait(() ->
      {
        for (int i=0 ; i < stepSz ; ++i) {
          simeng.evaluate();
          charts.inc_x();
        }
      }
      );
      nSteps -= stepSz;
    }
    while (nSteps  > 0) {
      runAndWait(() -> {simeng.evaluate(); charts.inc_x();});
      nSteps--;
    }
  }

 /**
  * Perform a sleep. Default time is 1000, or as supplied
  *
  * @param fields fields supplied to command
  */
  public void wait(String[] fields)
  {
    long sleeptime = 1000;
    if (fields.length >= 2) {
      try
      {
        sleeptime = Long.parseLong(fields[1]);
      }
      catch (Exception ex)
      {
        System.err.println("Default value of 2 seconds set");
      }
    }

    try
    {
      Thread.sleep(sleeptime);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Exit command line
   *
   * Todo: statistics output
   *
   * @param cmdline (unused)
   */
  public void exit(String[] cmdline)
  {
    System.exit(0);
  }

  public void quit(String[] cmdline) {exit(cmdline);}

  /**
   * Return help string for command
   *
   * @param cmd Command to return string for
   * @return Help string from cmds array
   */

  public String getHelp(String cmd)
  {
    for (String command : cmds)
    {
      if (command.startsWith(cmd))
        return command;
    }
    return "Error - Command not found: " + cmd;
  }

  /**
   * Runs the specified {@link Runnable} on the JavaFX application thread 
   * and waits for completion. This is used to resolve the issues created
   * by interacting with the javafx gui thread, in particular the need
   * for io and chart updates to complete before the next command is
   * applied.
   *
   * Source: Christopher Nahr
   * http://news.kynosarges.org/2014/05/01/simulating-platform-runandwait/
   *
   * @param action the {@link Runnable} to run
   * @throws NullPointerException if {@code action} is {@code null}
   */
  public static void runAndWait(Runnable action) {
    if (action == null)
      throw new NullPointerException("action");

    // run synchronously on JavaFX thread
    if (Platform.isFxApplicationThread()) {
      action.run();
      return;
    }

    // queue on JavaFX thread and wait for completion
    final CountDownLatch doneLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        action.run();
      } finally {
        doneLatch.countDown();
      }
    });

    try {
      doneLatch.await();
    } 
    catch (InterruptedException e) 
    {
      // ignore exception
    }
  }
}

