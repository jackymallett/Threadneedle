/*
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;
import java.io.*;

import core.*;

public class TransactionView extends Stage implements Initializable
{
  @FXML ListView<String> transactions;
  ObservableList<String> items = FXCollections.observableArrayList();
  Ledger ledger; // Ledger being displayed

  public TransactionView(Ledger ledger)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/transactionview.fxml"));
    fxmlLoader.setController(this);

    this.ledger = ledger;

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.setTitle(ledger.name + "(" + ledger.getAccountNo() + ") Transactions");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

  }

  // TODO: convert to observable list to allow real time update
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
    transactions.setItems(items);
    for (Transaction t : ledger.transactions)
    {
      items.add(t.toString());
    }
  }

  @FXML void onOkButton(ActionEvent event)
  {
    close();
  }
}
