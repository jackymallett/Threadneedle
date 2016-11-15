/* Program: Threadneedle
 *
 * MainController
 *
 * Controller for economic model. Keeps track of all objects in the model,
 * and provides step/run controls for running simulations from GUI or
 * batch/command line.
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
package gui;

import base.Base;
import charts.*;
import core.*;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.lang.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import static gui.Common.simeng;


public class MainController extends Stage implements Initializable
{
  @FXML private MainPane             mainPane;
  @FXML private MenuBar              menuBar;
  @FXML private MarketViewController marketViewController;
  @FXML private LabourViewController labourViewController;
  @FXML private LeftMenu             leftMenu;
  @FXML private VBox                 rightMenu;

  @FXML private Button               stepNButton;
  @FXML private Button               runButton;
  @FXML private Button               showBanks;
  @FXML private Button               showMacro;
  @FXML private Button               regionButton;

  public  ChartController charts         = null;
  public  MacroController macro          = null;
  private BankController  bankcontroller = null;
  private AccountViewer   accountViewer  = null;
  private SummaryView     summaryViewer  = null;

  private final String configFile = "defaultSimulation.json";

  private final ModelConfig mConfig;

  private int stepN = 1; // No. of steps to make each time on step button

  public MainController(ModelConfig mConfig, ChartController charts)
  {
    // Create simulation engine

    simeng = new SimulationEngine();

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/threadneedle.fxml"));
    fxmlLoader.setController(this);
    this.mConfig = mConfig;
    this.charts  = charts;

    macro = new MacroController();

    try
    {
      setScene(new Scene(fxmlLoader.load()));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
 
  }

  @Override
  public void initialize(URL fxmlFileLocation, ResourceBundle resources)
  {
    mainPane.setStyle("-fx-border-color: black;");
    setTitle(simeng.getTitle());

    this.initialize();
  }

  /**
   * Initialize the simulation from the fxml presets provided at launch.
   */
  private void initialize()
  {
    // Create framework simulation using model configuration parameters.
    simeng.createGovt(mConfig.getCountry(), mConfig.getGovtType(),
                      mConfig.getBankingSystem());

    // Add a listener to pick up any changes to registered markets.

    addMarketsListener();

    // Add an observer to SimulationEngine to pick up any engine changes
    // that impact the gui (such as new markets containers from a new
    // Government.

    simeng.addObserver((obs, o) -> {
       addMarketsListener();
       charts.reloadAll();
       macro.reloadAll();
    });

    // Populate the labour view widget

    labourViewController.addMarket(simeng.govt.markets.getMarket("Labour"));

    // Initialise market view widget

    marketViewController.setMarkets(simeng.govt.markets);

  }

  /**
   * Add a listener to the Govt.markets container in order to pick
   * up new markets added by the simulation.
   */

  private void addMarketsListener()
  {
     simeng.govt.markets.obsMarkets.addListener(new ListChangeListener<Market>()
       {
         @Override
         public void onChanged(Change<? extends Market> change)
         {
            while (change.next())
            {
              if (change.wasAdded())
              {
                 for (Market market : change.getAddedSubList())
                 {
                    SimNode marketNode = new SimNode(
                                     "../resources/images/market.png", market);

                    mainPane.autoAddAll(marketNode);
                 }
              }
           }
         }
       });
   }


  // Handlers for Buttons on main panel interface.

  @FXML protected void regionSelect(ActionEvent event)
  {
     if(regionButton.getText().equals("Add Region"))
     {
       GovtConfig stage = new GovtConfig();
       stage.showAndWait();

       regionButton.setText(stage.govt.name);

     }
     else
     {
       core.Region region = simeng.govt.regions.get(regionButton.getText());
       Stage stage = new GovtConfig(region);
       stage.showAndWait();

       // Check in case configuration changed name.

       if(!regionButton.getText().equals(region.name))
           regionButton.setText(region.name);
     }
  }

  /** 
   * Handle rotation on the region button.
   *
   * @param event scroll event for rotation
   */
  @FXML protected void regionRotate(ScrollEvent event)
  {
    String selected = regionButton.getText();
    boolean found = false;

    Iterator it = simeng.govt.regions.entrySet().iterator();

    if(selected.equals("Add Region") && !it.hasNext()) return;

    // Iterate through regions - set next region if there is one
    // or Add Region if we fall off the end of the list.

    while(it.hasNext())
    {
       Map.Entry<String, Region> entry = (Map.Entry)it.next();

       if((found) || (selected.equals("Add Region")))
       {
          // Edge case, there is only 1 element in the list
          if(entry.getKey().equals(selected))
             regionButton.setText("Add Region");
          else
             regionButton.setText(entry.getKey());

          return;
       }

       if(entry.getKey().equals(selected)) found = true;

    }
    regionButton.setText("Add Region");
  }

  @FXML
  protected void exitButtonAction(ActionEvent event)
  {
    System.out.println("Exit");
    System.exit(0);
  }

  @FXML
  protected void startButtonAction(ActionEvent event)
  {
    System.out.println("Implement: start button handling");
  }

  @FXML
  protected void stopButtonAction(ActionEvent event)
  {
    System.out.println("Implement: stop button handling");
  }

  @FXML
  protected void stepNButton(ActionEvent event)
  {
	 final long startTime = System.currentTimeMillis();

     for (int i = 0; i < stepN; i++) step();

	 final long endTime = System.currentTimeMillis();

	 if(Base.profile == true)
		 System.out.println(stepN + " / " + (endTime - startTime) + " ms");



    if (bankcontroller != null)
      bankcontroller.refresh();

    if (accountViewer != null)
      accountViewer.refresh();

    if(summaryViewer != null)
      summaryViewer.refresh();
  }

  /**
   * Step simulation forward one step.
   */
  private void step()
  {
    simeng.evaluate();
    setTitle(simeng.getTitle());
    charts.inc_x();
    macro.inc_x();
    marketViewController.update();
    mainPane.update();
  }

  @FXML
  protected void resetButtonAction(ActionEvent event)
  {
    // Todo: auto reset to loaded config if present?

    labourViewController.clear();
    mainPane.getChildren().clear();
    marketViewController.clear();
    simeng.resetAll();
    charts.reloadAll();
    macro.reloadAll();
    initialize();

    setTitle(simeng.getTitle());

    // If bank information is being displayed, then close windows
    // since they will no longer be relevant.

    if (accountViewer  != null) accountViewer.hide();
    if (summaryViewer  != null) summaryViewer.hide();
    if (bankcontroller != null) bankcontroller.hide();
  }

  /**
   * Handle the ShowBanks button on the main controller.
   *
   * @param event from button press (not used)
   */
  @FXML
  protected void showBanksAction(ActionEvent event)
  {
    if (simeng.govt.banks.noBanks() > 0)
    {
      bankcontroller = new BankController(simeng.govt.banks, simeng.govt);
      bankcontroller.toFront();
      bankcontroller.showAndWait();
    }
    else
      System.out.println("No Banks to Display");
  }

  /**
   * Handle the simulation summary button.
   *
   * @param event from the button press (not used)
   */

   @FXML
   protected void showSummary(ActionEvent event)
   {
      summaryViewer = new SummaryView();
      summaryViewer.showAndWait();
   }

  /**
   * Handle the Show Macro button on the main controller.
   *
   * @param event from button press (not used)
   */
  @FXML
  protected void showMacroAction(ActionEvent event)
  {
     if(!macro.isShowing())
     {
        macro.toFront();
        macro.showAndWait();
     }
  }

  @FXML
  protected void addButton(MouseEvent event)
  {
    System.out.println("Implement: add button handling");
  }

  @FXML
  protected void onRotate(ScrollEvent event)
  {
    switch (stepN)
    {
      case 1:
        stepNButton.setText("1 Month");
        stepN = 30;
        break;
      case 30:
        stepNButton.setText("1 Year");
        stepN = 360;
        break;

      case 360:
        stepNButton.setText("5 Years");
        stepN = 1800;
        break;

      case 1800:
        stepNButton.setText("10 Years");
        stepN = 3600;
        break;

      case 3600:
        stepNButton.setText("Step");
        stepN = 1;
        break;
    }
  }

  @FXML
  protected void mouseClicked(MouseEvent event)
  {
    System.out.println("\nMain - Click: " + event.getSource());
  }

  /**
   * Load a simulation model from the default file
   *
   * @param event event
   */
  @FXML
  protected void handleLoad(ActionEvent event)
  {
    try
    {
      File file = new File(configFile);
      if(file.exists())
	  {
		 resetButtonAction(event);
         loadFile(file);
	  }
      else
         System.out.println("Unable to open configuration file: " + configFile);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      System.out.println("Unable to open simulation file: " + configFile);
    }
  }

  @FXML protected void handleCharts(ActionEvent event)
  {
     if(charts.isShowing())
        charts.hide();
     else
        charts.show();
  }

  @FXML protected void handleConfig(ActionEvent event)
  {
	  SimulationCfg cfg = new SimulationCfg();
      cfg.showAndWait();
  }

  @FXML protected void handleDescription(ActionEvent event)
  {
    //TextInputDialog dialog = new TextInputDialog("test\ntest");

    TextDialog dialog = new TextDialog("Simulation Description");

    dialog.description.setText(simeng.description);

    dialog.showAndWait();

    simeng.description = dialog.description.getText();
  }

  /**
   * Load a simulation model from configuration file.
   *
   * @param event event from GUI
   */
  @FXML
  protected void handleLoadFile(ActionEvent event)
  {
    Path config = FileSystems.getDefault()
                             .getPath(Threadneedle.preferences.configDirectory);

    Stage stage = new Stage();
    stage.setTitle("Load File");

    FileChooser filechooser = new FileChooser();
    filechooser.setTitle("Open simulation config");
    filechooser.getExtensionFilters()
               .addAll(new ExtensionFilter("Configs", "*.json"));

    if(Files.exists(config))
    {
       filechooser.setInitialDirectory(new File(Threadneedle.preferences.configDirectory));
    }

    filechooser.setTitle("Load Simulation");
    File file = filechooser.showOpenDialog(stage);

    
    if ((file != null) && file.exists())
	{
	  resetButtonAction(event);
      loadFile(file);
	}
  }

  /**
   * Load simulation config file.
   *
   * @param file file to load configuration from.
   */
  private void loadFile(File file)
  {
    labourViewController.clear();
    marketViewController.clear();
    mainPane.getChildren().clear();

    if (simeng.loadSimulation(file))
    {
      // All visible agents should be added to mainPane here

      for (Company c : simeng.companies)
      {
        mainPane.autoAddAll(leftMenu, c);
      }

      for (Bank b : simeng.govt.banks.getBankList().values())
      {
        mainPane.autoAddAll(leftMenu, b);
      }

      // Markets will have been loaded without a change listener

      for (Market m : simeng.govt.markets.markets)
      {
        if (m.getClass() == Market.class)
        {
           mainPane.autoAddAll(leftMenu, m);
        }

      }

      simeng.employees.forEach(labourViewController::addLabour);
      charts.reloadAll();
      macro.reloadAll();
      mainPane.positionAll();
      marketViewController.setMarkets(simeng.govt.markets);
    }
    else
    {
      System.out.println("Unable to read configuration file: "
                         + file);
    }

    // todo: invoke positioning of some kind
    // todo: version on config files to catch formatting changes
  }

  /**
   * Save the current simulation to file chosen by user.
   *
   * @param event event from gui
   */

  @FXML
  protected void handleSaveAs(ActionEvent event)
  {
    Stage stage = new Stage();
    stage.setTitle("Save Simulation Config");

    Path config = FileSystems.getDefault().getPath(Threadneedle.preferences.configDirectory);

    FileChooser filechooser = new FileChooser();

    filechooser.setTitle("Open Simulation");

    if(Files.exists(config))
    {
       filechooser.setInitialDirectory(new File(Threadneedle.preferences.configDirectory));
    }

    File file = filechooser.showSaveDialog(stage);

    if(file != null)
       saveFile(file);
  }

  /**
   * Save the current simulation to the default config file.
   *
   * @param event even from gui
   */

  @FXML
  protected void handleSave(ActionEvent event)
  {
    try
    {
      File file = new File(configFile);
      saveFile(file);
    }
    catch (Exception e)
    {
      System.out.println("Unable to open default config file: "
                         + configFile);
    }
  }

  /**
   * Save the current simulation to file.
   * <p>
   * There are some dependencies when agents are loaded, in particular
   * governments and banks must be added to the simulation before other
   * agents, since they are then used in their initialisation.
   *
   * @param file File to save configuration to.
   */

  private void saveFile(File file)
  {
    try
    {
      FileWriter fw = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(fw);

      System.out.println("Save file");

      bw.write("{\n\"description\" : \"" + simeng.description + "\",\n");

      // Government must always be defied before any of its agents

      bw.write("\"GsonAgent\" : \n [ \n");
      bw.write("  " + simeng.govt.save());

      // Then the banks, with the exception of the central bank
      // which is currently being created automatically

      for (Bank b : simeng.govt.getBankList().values())
      {
        if (!(b instanceof CentralBank))
          bw.write(",\n " + b.save());

      }

      // Regions

      for(core.Region r : simeng.govt.regions.values())
      {
         bw.write(",\n " + r.save());
      }

      // and then the markets.

      for (Market m : simeng.govt.markets.markets)
      {
        bw.write(",\n  " + m.save());
      }

      // companies

      for (Company c : simeng.companies)
      {
        if (!(c instanceof Market) && !(c instanceof Bank))
          bw.write(",\n  " + c.save());
      }

      // and finally the people.

      for (Person p : simeng.employees)
      {
        bw.write(",\n  " + p.save());
      }

      bw.write("\n ]\n}");
      bw.close();
      fw.close();
    }
    catch (Exception e)
    {
      System.out.println("Unable to write to file: " + file);
    }
  }

  @FXML
  protected void handleClose(ActionEvent event)
  {
    System.out.println("Close file");
  }

  @FXML
  protected void handleHelp(ActionEvent event)
  {
    System.out.println("Help file");
  }

  @FXML
  protected void mouseDragDetected(MouseEvent event)
  {
    if (event.getSource() instanceof SimNode)
    {
      SimNode source = (SimNode) event.getSource();

      source.setDragStart(source.getTranslateX(), source.getTranslateY());
      mainPane.getScene().setCursor(new ImageCursor(source.getImage()));
      event.consume();
    }
  }

  /**
   * Create object matching nodes dropped into simulation map.
   *
   * @param event Mouse event from gui.
   */

  @FXML protected void mouseDragDropped(MouseEvent event)
  {
    Point2D p = mainPane.sceneToLocal(event.getSceneX(), event.getSceneY());

    /*
     * Fail silently if icon isn't over the main pane, alert if model
     * consistency check fails, and otherwise add the new node and draw on
     * the screen.
     */

    if (mainPane.contains(p.getX(), p.getY()))
    {
      SimNode source = (SimNode) event.getSource();

      if(!source.getType().contains("Bank") && simeng.govt.banks.noBanks() == 0)
      {
         showAlert("Simulation must include at least one Bank");
         return;
      }
      Bank bank = mainPane.findNearestBank(p.getX(), p.getY());

	  // Add object to simulation, and to the main pane.

	  Object obj = simeng.addEntity(source.getTypeClass(),
                                    source.getProperties(), bank, null);

      SimNode newNode = new SimNode(source.getImage(), source.getTypeClass(),
			                        obj);

      // Catch & Display errors from the Simulation Engine

      if (newNode.simObject instanceof String)
      {
        showAlert((String) newNode.simObject);
        return;
      }

      // The following is order dependent, owing to the hierarchy of
      // agents (Agent -> Company -> Farm/Bank/Market)

      Agent agent = (Agent) newNode.simObject;
      core.Region region = simeng.govt.regions.get(regionButton.getText());

      // If there is a region selected, set this for the agent.

      agent.setRegion(region);

      if (agent instanceof Bank)
      {
        agent.x = p.getX();
        agent.y = p.getY();
      }
      else if (agent instanceof Company)
      // See if additional agents need to be created.
      {
        Company c = ((Company) newNode.simObject);
        c.x = p.getX();
        c.y = p.getY();

        // Create market for product if needed.
        if (!(agent instanceof InvestmentCompany || 
              agent instanceof StockExchange     || 
              agent instanceof StockMarket)) 
        // don't create a market for InvestmentCompanies etc
        {
          // todo: wrap in try/accept for error message
          if (simeng.govt.markets.getMarket(c.product) == null)
          {
            String message = simeng.createMarket("M-" + c.product, 
                          c.product, c.govt, 
                          (Bank) simeng.objectList.get(simeng.defaultBankName),
                          Base.DEFAULT_MARKET_DEPOSIT, region);
            c.setMarkets(simeng.govt.markets);
          }
        }
      }
      else
      {
        agent.x = p.getX();
        agent.y = p.getY();
      }


      mainPane.getChildren().addAll(newNode);
      mainPane.getScene().setCursor(Cursor.DEFAULT);
      newNode.relocate();
    }

    event.consume();
  }

  // Populate the labour view widget


  /**
   * Display supplied alert, and reset cursor to default.
   *
   * @param text Message to display
   */
  private void showAlert(String text)
  {

    Alert a = new Alert("Model Consistency Check Failed", text, 320, 150, null);
    a.show();

    mainPane.getScene().setCursor(Cursor.DEFAULT);
  }

  @FXML
  protected void onDragOver(MouseEvent event)
  {
    event.consume();
  }

  private static void configureBorder(final Region region)
  {
    region.setStyle("-fx-background-color: white;"
                    + "-fx-border-color: black;" + "-fx-border-width: 1;"
                    + "-fx-border-radius: 1;" + "-fx-padding: 1;");
  }
}
