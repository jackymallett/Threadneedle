/**
 * Program: Threadneedle
 *
 * BankPane
 *
 * View controller for each individual bank's ledger.
 *
 * Author : Jacky Mallett (c)
 * Date   : January 2015
 */

package gui;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import core.*;

/**
 * Places content in a bordered pane with a title.
 *
 * Source: https://gist.github.com/jewelsea/2838292 via StackOverflow.
 */

public class BankPane extends SplitPane
{
  Bank     bank;
  BankView bankview;
  Button   title;

  BankPane(String titleString, Bank b)
  {
    title = new Button(" " + titleString + " ");
    bankview = new BankView(b);

    this.bank = b;

    this.setDividerPosition(0, 0.7);
    this.setOrientation(Orientation.VERTICAL);
    this.getItems().addAll(title, bankview);

    title.setOnAction(new EventHandler<ActionEvent>()
    {
      @Override public void handle(ActionEvent e)
      {
        Stage cfg = null;

        if (bank instanceof CentralBank)
        {
          cfg = new CentralBankConfig((CentralBank) bank);
        }
        else if (bank != null)
        {
          cfg = new BaselBankConfig(bank, bankview);
        }

        if (cfg == null)
        {
          System.out.println("implement bank config for " + bank);
        }
        else
        {
          cfg.showAndWait();
          refresh();
        }
      }
    });
  }

  /**
   * Refresh ledgers, and check bank status. Highlight bank name if
   * in zombie (runoff) mode.
   */
  public void refresh() 
  {
     bankview.refresh();

     if(bank.zombie)
     {
        title.setStyle("-fx-background-color: #FF0000;");
     }
  }
}
