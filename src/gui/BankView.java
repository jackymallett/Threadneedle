/*
 * Program: Threadneedle
 *
 * BankView  Provide a display of a single bank's general ledger
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

import javafx.geometry.*;
import javafx.scene.layout.*;

import core.*;

// http://docs.oracle.com/javafx/2/fxml_get_started/fxml_tutorial_intermediate.htm#CACFEHBI

public class BankView extends HBox
{
  Bank bank;
  LedgerView assets      = null;
  LedgerView liabilities = null;

  public BankView(Bank b)
  {
    this.bank = b;

    setPadding(new Insets(5, 5, 5, 5));

    int maxRows = Math.max(bank.gl.assets.size(),
                           bank.gl.liabilities.size() + bank.gl.equities.size());

    assets = new LedgerView(b, AccountType.ASSET, maxRows);
    setHgrow(assets, Priority.ALWAYS);
    this.getChildren().addAll(assets);

    liabilities = new LedgerView(b, AccountType.LIABILITY, maxRows);
    setHgrow(liabilities, Priority.ALWAYS);
    this.getChildren().addAll(liabilities);
  }

  public void refresh()
  {
    assets.refresh();
    liabilities.refresh();
  }
}
