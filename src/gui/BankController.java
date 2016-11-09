/**
 * Program: Threadneedle
 *
 * BankController
 *
 * Show a panel displaying aggregate books for the banks in the simulation.
 *
 * Author : Jacky Mallett (c)
 * Date   : June 2014
 */

package gui;

import java.util.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;

import core.*;

/**
 * Provide display of Bank ledgers for the banks in the simulation.
 */

public class BankController extends Stage implements Initializable
{
  @FXML private GridPane bankgrid;

  private HashMap<String, BankPane> bankpanes = new HashMap<>(10);

  public BankController(Banks banks, Govt govt)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/bankcontroller.fxml"));
    fxmlLoader.setController(this);

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.getScene().getStylesheets().add("../resources/ledger.css");
      this.getScene().getStylesheets().add("../resources/borderedpane.css");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    ColumnConstraints colc = new ColumnConstraints(400, 480,
                                                   Double.MAX_VALUE);
    colc.setHgrow(Priority.ALWAYS);
    bankgrid.getColumnConstraints().addAll(colc, colc);
    int row = 0;
    int col = 1;

    // Central Bank goes on the left

    BankPane bpane = new BankPane("Central Bank", banks.getBank("Central Bank"));

    GridPane.setConstraints(bpane, 0, 0);
    bankgrid.getChildren().addAll(bpane);

    bankpanes.put("Central Bank", bpane);

    for (Bank b : banks.getBankList().values())
    {
      if (!(b instanceof CentralBank))
      {
        bpane = new BankPane(b.name, b);
        bankpanes.put(b.name, bpane);
        GridPane.setConstraints(bpane, col, row++);
        bankgrid.getChildren().addAll(bpane);
      }
    }

    this.setAlwaysOnTop(true);
    this.setTitle("Bank Ledgers");

    sizeToScene();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    // numberCol.setCellFactory(TextFieldTableCell.forTableColumn());
  }

  /**
   * Update all ledger displays.
   */

  public void refresh()
  {
    bankpanes.values().forEach(gui.BankPane::refresh);
  }

}
