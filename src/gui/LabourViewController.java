/*
 * Program: Threadneedle
 *
 * LabourViewController  - controller for labour market 
 *
 * Author : Copyright (c) Jacky Mallett
 * Date   : September 2014
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package gui;

import java.util.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.control.TableColumn.*;
import javafx.scene.control.cell.*;
import javafx.util.Callback;

import java.net.URL;

import core.*;
import static gui.Common.*;

// Todo: refactor labour handling to allow more than 1 hard coded need.

public class LabourViewController extends VBox implements Initializable
{
  @FXML private TableView<LabourRow> tableView;
  @FXML private TableColumn<LabourRow, String> noWorkersCol;
  @FXML private TableColumn<LabourRow, String> productCol;
  @FXML private TableColumn<LabourRow, String> profileCol;

  final ObservableList<LabourRow> data = FXCollections.observableArrayList();

  Market market = null;

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    tableView.setEditable(true);
    tableView.setItems(data);

    productCol.setCellValueFactory(new PropertyValueFactory("Product"));
    profileCol.setCellValueFactory(new PropertyValueFactory("Profile"));
    noWorkersCol.setCellValueFactory(new PropertyValueFactory("NoWorkers"));

    // On mouse clicked - if no profile present show the add worker menu
    // otherwise:         left button        display selected profile
    //                    right button       add worker menu

    tableView.setOnMouseReleased(event -> {
       WorkerConfig config = null;

       if(tableView.getSelectionModel().getSelectedItem() == null)
       {
         LabourRow newrow = new LabourRow(0,1,1,1,1, null, false, true, false);
         config = new WorkerConfig(newrow);
       }
       else
       {
         if(event.getButton() == MouseButton.PRIMARY)
            config = new WorkerConfig(tableView.getSelectionModel().getSelectedItem());
       }

       if(config != null)
       {
          config.showAndWait();

          updateTable(config.labourRow, config.workerProfile);
       }

       event.consume();
    });

    // Setup the context menu for the tableview

    tableView.setRowFactory(new Callback<TableView<LabourRow>, TableRow<LabourRow>>()
    {
       @Override
       public TableRow<LabourRow> call(TableView<LabourRow> tableview)
       {
          final TableRow<LabourRow> row = new TableRow<>();
          final MenuItem addWorkerItem  = new MenuItem("Add...");
          final ContextMenu menu        = new ContextMenu();

          // Handler for Add.. menu item
          addWorkerItem.setOnAction(event -> {
             LabourRow newrow = new LabourRow(0,1,1,1,1, null,false,true,false);
             WorkerConfig stage = new WorkerConfig(newrow);
             stage.showAndWait();

             if(newrow.getNoWorkers() > 0)
                updateTable(newrow, stage.workerProfile);

             event.consume();
          });
          menu.getItems().add(addWorkerItem);
          row.setContextMenu(menu);
 
          return row;
       }
    });

    tableView.addEventHandler(ScrollEvent.SCROLL, event ->
    {

       LabourRow row = tableView.getSelectionModel().getSelectedItem();
       if(row != null)
          row.displayNextNeed();

       event.consume();

    });

  }

  /**
   * Update table with contents of supplied row. May result in new row, or
   * merge with existing rows contents. 
   *
   * @param row   Row returned from WorkerConfig dialog
   * @param workerProfile  New profile for workers on this row
   */

   public void updateTable(LabourRow row, Profile workerProfile)
   {
     // Check existing rows and see if we can merge this with one of them
     
     boolean merged = false;

     for(LabourRow r : data)
     {
        if(row == r) continue;

		// All variables set in workerconfig must be checked here for
		// identity to be true.
        if(r.workers.getFirst().profile.equals(workerProfile) &&
           r.workers.getFirst().desiredSalary == row.desiredSalary &&
		   r.workers.getFirst().randomPurchase == row.randomPurchase)
        {
           merged = true;

           for(Person p : row.workers)
           {
               r.workers.add(p);
           }
           if(data.contains(row))
              data.remove(row);
        }
     }

     // Add row to table if it was new, didn't match existing, and 
     // isn't the result of the user not specifying no. of workers 
     // in the config dialog

     if(!data.contains(row) && !merged && (row.getNoWorkers() > 0))
         data.add(row);

     // Sorts workers on market, and sets bid/sell
     if(market != null)
        ((LabourMarket)market).adjustPrices();
     refreshTable();
   }

  /**
   * Add Labour market for this view to display, and perform initialisation.
   *
   * @param market Market to view
   */

  @FXML
  protected void addMarket(Market market)
  {
    if (market != null)
      this.market = market;
  }

  @FXML
  protected void workersCellEnter(CellEditEvent event)
  {
    LabourRow row = (LabourRow) event.getRowValue();
    row.setNoWorkers((String) event.getNewValue());
  }

  /**
   * Add a labourer to the row which matches its profile, or
   * create a new row for it.
   * 
   * Used on simulation load.
   *
   * @param p   Person to be added
   */
  public void addLabour(Person p)
  {
     for(LabourRow row : data)
     {
        if(row.workers.getFirst().profile.equals(p.profile))
        {
           row.addWorker(p);
           return;
        }
     }
     data.add(new LabourRow(p)); 
     refreshTable(); 
  }

  /**
   * Clear labour view controller for reload/reset
   */

  public void clear()
  {
    this.market = null;
    data.clear();
  }

  /**
   * Refresh table to reflect changes. Work around for
   * various javafx deficiencies in this area.
   */
   private void refreshTable()
   {
     tableView.getColumns().get(0).setVisible(false);
     tableView.getColumns().get(0).setVisible(true);
   }

  /**
   * Data handler for rows in the LabourView table. 
   *
   * The LabourRow implicitly assumes in several cases that all workers
   * under its control have the same profile. This is fragile if profiles
   * start changing elsewhere in the simulation - which is not the current
   * design intent. 
   */
  public class LabourRow
  {
    private final SimpleStringProperty noWorkers = new SimpleStringProperty();
    private final SimpleStringProperty profile   = new SimpleStringProperty();
    private final SimpleStringProperty product   = new SimpleStringProperty();

    public  LinkedList<Person> workers = new LinkedList<>();

    private int  purchaseQ   = 0;
    private long storeQ      = 1;
    private int  consumption = 0;
    private int  workersQ    = 0;
    private long desiredSalary = 1;
    private boolean useLoan    = false;
    private boolean consumable = true;
    private boolean randomPurchase = false;

    // Todo: custom property for profile handling
    public LabourRow(int no, int quantity, long storeQ, int consumption,
                     long desiredSalary, String product, 
					 boolean useLoan, boolean consumable, 
					 boolean randomPurchase)
    {
      this.setNoWorkers(Integer.toString(no));
      this.purchaseQ = quantity;
      this.storeQ = storeQ;
      this.consumption = consumption;
      this.workersQ   = no;
      this.consumable = consumable;
      this.useLoan    = useLoan;
	  this.randomPurchase = randomPurchase;
      this.desiredSalary = desiredSalary;

      if(product != null)
         this.setProduct(product);

      this.setProfile(this.toString());
    
      this.setNoWorkers(Integer.toString(no));
    }

    /**
     * Constructor from person. Need profile for this worker is used
     * to set parameters for the row
     *
     * @param p  Person to add
     */

    public LabourRow(Person p)
    {
      // Use the first need for display purposes if it exists.

      randomPurchase = p.randomPurchase;
      desiredSalary  = p.desiredSalary;

      if(p.profile.needs.firstEntry() != null)
         updateDisplay(p.profile.needs.firstEntry().getValue());

      workers.add(p);
    }


    /**
     * Update this labourrow with a new profile, and changed
     * number of workers. 
     *
     * Only increases in the initial deposit are currently supported.
     *
     * todo: support changing bank for accounts. (account move)
     * 
     * @param profile        New profile from user config dialog
     * @param no             No. of workers now in row
     * @param newDeposit     New initial deposit for workers in row.
     * @param salary         Desired salary for worker
	 * @param randomPurchase Randomise purchases
     * @param bankname       Bank used by workers
     */
    public void update(Profile profile, long no, int newDeposit, long salary,
                       boolean randomPurchase, String bankname)
    {
      // Update existing workers with new profile

      this.desiredSalary = salary;

      for(Person p : workers)
      {
          p.setProfile(profile);
		  p.randomPurchase = randomPurchase;
      }

      // Adjust number of workers

      if(workers.size() > no)
      {
        for(int i = 0; i < workers.size() - no; i++)
        {
           Person p = workers.removeLast();

           // Remove from other containers

           simeng.removeFromContainers(p);
        }
      }
      else if(workers.size() < no)
      {
        for (int i = workers.size(); i < no; i++)
        {
           Person p = new Person(null, simeng.govt, 
                              simeng.govt.banks.getBank(bankname), newDeposit);

           p.setProfile(profile);
		   p.desiredSalary = salary;
           p.setSalary(desiredSalary);

           simeng.addToContainers(p);
           addWorker(p);
        }
      }

      // Adjust deposit if required (matching cash will be created alongside the
      // addition to the liability deposit, and update desiredSalary field.

      for(Person p : workers)
      {
          if(newDeposit > p.initialDeposit)
          {
             p.getBank().printMoney(p.getAccount(), 
                                    newDeposit - p.initialDeposit, 
                                    "(Exogenously) Modified by user");
             p.initialDeposit = newDeposit;
          }
          else if(newDeposit < p.initialDeposit)
          {
             System.out.println("Reduction in initial deposit not supported.");
          }
          p.setSalary(salary);
          p.desiredSalary = p.getSalary();
      }

      Need displayNeed=workers.getFirst().profile.needs.firstEntry().getValue();

      updateDisplay(displayNeed);
    }


    /**
     * Get the next need in the profile sets list, and switch the display to
     * that need.
     *
     * @return Need now being displayed
     */
    public Need displayNextNeed()
    {
       // Use the first workers need profile since they should all be the same

       Need currentNeed = workers.getFirst().profile.getNeed(getProduct());

       Need next = workers.getFirst().profile.getNext(currentNeed.product);

       if(next != currentNeed) 
       {
          updateDisplay(next);
       }
       return next;
    }

    public boolean displayNeed(String product)
    {
       Need need = workers.getFirst().profile.getNeed(product);

       if(need == null) return false;
       else
       {
           updateDisplay(need);    
           return true;
       }
    }

    /**
     * Add a worker to this profile set.
     *
     * @param p person to add.
     */
    private void addWorker(Person p)
    {
      this.workersQ++;
      setNoWorkers(Integer.toString(this.workersQ));

      workers.add(p);
    }


    /**
     * Update need being displayed.
     *
     * @param need to update
     */

    public void updateDisplay(Need need)
    {
       this.storeQ        = need.storeQ;
       this.purchaseQ     = need.quantity;
       this.consumption   = need.consumption;
       this.consumable    = need.consumable;
       this.useLoan       = need.useLoan;

       setProduct(need.product);
       setProfile(this.toString());

       refreshTable();
    }

    public String toString()
    {
      return purchaseQ + ":" + consumption + ":" + storeQ;
    }

    public String getProfile()
    {
      return profile.get();
    }

    public void setProfile(String value)
    {
      profile.set(value);
    }

    public String getBankName()
    {
       if(workers.size() > 0)
          return workers.getFirst().getBankName();      
       else
          return null;
    }

    public String getInitialDeposit()
    {
       return String.valueOf(workers.getFirst().initialDeposit);
    }

    public String getInitialSalary()
    {
System.out.println("desired: " + workers.getFirst().desiredSalary);
       return String.valueOf(workers.getFirst().desiredSalary);
    }

    public long getStore(){ return storeQ;}
    public int  getConsumption(){ return consumption;}
    public int  getQuantity(){ return purchaseQ;}
    public boolean getUseLoan() { return useLoan;}
	public boolean getConsumable() { return consumable;}
    public boolean getRandomPurchase() { return randomPurchase;}

    /**
     * @return No. of workers controlled by row.
     */
    public int getNoWorkers()
    {
      return workers.size();
    }

	public void setRandomPurchase(boolean value)
	{
		randomPurchase = value;
	}

    public void setNoWorkers(String value)
    {
      noWorkers.set(value);
    }

    public String getProduct()
    {
      return product.get();
    }

    public void setProduct(String value)
    {
      product.set(value);
    }

  }
}
