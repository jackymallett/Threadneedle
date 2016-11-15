/*
 * Program: Threadneedle
 *
 * MarketViewController - Display List of Markets 
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
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.scene.input.*;
import javafx.event.*;
import javafx.util.Callback;
import core.*;

/**
 * Display the list of markets in the simulation with current bid/ask pricing.
 *
 * Nb. Currently markets can only be added to the display, or the entire list
 * cleared.
 */

public class MarketViewController extends VBox implements Initializable
{
  @FXML private TableView<MarketRow> tableView;
  @FXML private TableColumn<MarketRow, String> productCol;
  @FXML private TableColumn<MarketRow, Integer> bidCol;
  @FXML private TableColumn<MarketRow, Integer> askCol;

  ObservableList<MarketRow> data = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    tableView.setItems(data);
    tableView.setEditable(true);

    productCol.setCellValueFactory(new PropertyValueFactory("Product")); 
    bidCol.setCellValueFactory(new PropertyValueFactory("Bid")); 
    askCol.setCellValueFactory(new PropertyValueFactory("Ask")); 

    // On mouse clicked - if no markets present show the add market menu
    // otherwise: left button display selected market
    //            right       display menu to add

    tableView.setOnMouseClicked(new EventHandler<MouseEvent>()
    {
       @Override
       public void handle(MouseEvent event)
       {
          Stage stage = null;
          if(tableView.getSelectionModel().getSelectedItem() == null)
          {
             stage = new MarketConfig();
          }
          else
          {
             if(event.getButton() == MouseButton.PRIMARY)
                stage = new MarketConfig(tableView.getSelectionModel().getSelectedItem().market);
          }

          if(stage != null) stage.showAndWait();

          event.consume();
       }
    });
    

    // Setup context menu for the tableview. 
    tableView.setRowFactory(tableview -> {
       final TableRow<MarketRow> row = new TableRow<>();
       final MenuItem addMarketItem  = new MenuItem("Add...");
       final ContextMenu menu        = new ContextMenu();

       addMarketItem.setOnAction(event -> {
          Stage stage = new MarketConfig();
          stage.showAndWait();
       });

       menu.getItems().add(addMarketItem);
       row.setContextMenu(menu);

       return row;
    });
  }
    
  /**
   * Add a product market to the display. Labour markets are silently ignored.
   *
   * @param market Market to add
   */
  
  protected void addMarket(Market market)
  {
    if (!(market instanceof LabourMarket))
    {
        if(market.getProduct() == null)
           market.setProduct();

        data.add(new MarketRow(market));
  
        refreshTable();
    }
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
   * Set the list of markets to display by adding a change listener to the
   * simulation markets list. Clear any markets already in display and add any
   * that are already in the simulation container provided.
   *
   * @param markets Simulation market container
   */
  public void setMarkets(Markets markets)
  {
    data.clear();
    // Don't display LabourMarkets here
    markets.markets.forEach(this::addMarket);

    markets.obsMarkets.addListener((ListChangeListener<Market>) change -> {
      while (change.next())
      {
        if (change.wasAdded())
        {
          for (Market market : change.getAddedSubList())
          {
              addMarket(market);
          }
        }
      }
    });
  }

  // Update all rows in view with latest price information.

  public void update()
  {
    for (MarketRow row : data)
    {
      row.setAsk(row.getAsk());
      row.setBid(row.getBid());
    }

    refreshTable();
  }

  /**
   * Clear all values from view for reset.
   */

  public void clear()
  {
    data.clear();
  }

  /**
   * Data handler for rows in the MarketView table.
   */
  public class MarketRow
  {
    private final SimpleStringProperty product;
    private final SimpleLongProperty   ask;
    private final SimpleLongProperty   bid;

    public Market market;

    public MarketRow(Market m)
    {
      market = m;

      product = new SimpleStringProperty(getProduct());
      ask     = new SimpleLongProperty(getAsk());
      bid     = new SimpleLongProperty(getBid());

    }

    // Getters and setters for tableview to display row information

    public SimpleLongProperty askProperty() {return ask;}

    public SimpleLongProperty bidProperty()
    {
      return bid;
    }

    public String getProduct()
    {
      return market.getProduct() + " : " + market.getTotalItems();
    }

    public void setProduct(String name)
    {
      product.set(name);
    }

    /**
     * If no items are available display -1 as price
     *
     * @return current ask price
     */
    public Long getAsk()
    {
       return market.getAskPrice();
    }

    public void setAsk(Long value)
    {
      this.ask.set(value);
    }

    /**
     * If no items are available display -1 as price
     *
     * @return current bid price
     */
    public Long getBid()
    {
       return market.getBidPrice();
    }

    public void setBid(Long value)
    {
      bid.set(value);
    }

  }
}
