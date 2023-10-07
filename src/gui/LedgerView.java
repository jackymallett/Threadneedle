/*
 * Program: Threadneedle
 *
 * LedgerView - provide the ledger display for a single bank
 *
 * Author : Copyright (c) Jacky Mallett
 * Date   : September 2013
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package gui;

import javafx.util.*;
import javafx.stage.*;

import java.text.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.*;
import javafx.beans.property.*;

import java.util.*;

import core.*;

// http://docs.oracle.com/javafx/2/fxml_get_started/fxml_tutorial_intermediate.htm#CACFEHBI

/**
 * Display Ledger View of a Bank
 */

public class LedgerView extends TableView
{
  Bank bank;
  Stage     viewer;

  ObservableList<LedgerRow> data = FXCollections.observableArrayList();

  TableColumn<LedgerRow, String> nameCol    = new TableColumn();
  TableColumn<LedgerRow, Long>   balanceCol = new TableColumn();
  TableView<LedgerRow> table;

  /**
   * Display view of supplied ledgers for this Bank, grouped by
   * asset vs liability and equity.
   *
   * @param b    Bank to display ledgers for
   * @param type ASSET - LHS, LIABILITY/EQUITY - RHS
   * @param maxRows Maximum number of rows to display
   */
  public LedgerView(Bank b, AccountType type, int maxRows)
  {
    this.bank = b;
    table = this;
    this.setItems(data);

    this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    nameCol.setCellValueFactory(new PropertyValueFactory("Name"));
    balanceCol.setCellValueFactory(new PropertyValueFactory("Balance"));

    nameCol.setMinWidth(80);
    balanceCol.setMinWidth(80);

    if (type == AccountType.ASSET)
    {
      addLedgers(this.bank.gl.assets, bank.gl, type);
      this.getColumns().addAll(nameCol, balanceCol);

      int totalRows = bank.gl.assets.size();

      while (totalRows++ < maxRows)
        data.add(new LedgerRow());

      data.add(new LedgerRow(null, bank.gl, type));
    }
    else
    {
      addLedgers(this.bank.gl.liabilities, bank.gl, AccountType.LIABILITY);
      addLedgers(this.bank.gl.equities, bank.gl, AccountType.EQUITY);

      data.add(new LedgerRow(null, bank.gl, type));

      this.getColumns().addAll(balanceCol, nameCol);
    }


    // Set single cell selection so that transaction or account views can be
    // shown in response to mouse selection. 
    table.getSelectionModel().setCellSelectionEnabled(true);

    this.setOnMouseClicked(event -> {
      int transactionCol;     // Column representing ledger transactions.
      LedgerRow lr = table.getSelectionModel().getSelectedItem();

      // For Assets, the rhs column is the total in the ledger, lhs for L + E
      if(type == AccountType.ASSET)
         transactionCol = 1;
      else
         transactionCol = 0;

      // Check click was on a ledger
      if ((lr == null) || (lr.ledger == null)) return;

      if (table.getFocusModel().getFocusedCell().getColumn() == transactionCol)
      {
        Stage s = new TransactionView(lr.ledger);
        if(s != null)
        {
           s.showAndWait();
        }
        else
            System.out.println("Failed to create TransactionView\n");

//        s.setAlwaysOnTop(true);
//        s.showAndWait();
      }
      else
      {
        if (lr.ledger.ledgertype == LedgerType.LOAN)
          viewer = new LoanViewer(this.bank, lr.ledger.getAccount());
        else
          viewer = new AccountViewer(this.bank, lr.ledger.accounts);

        viewer.setAlwaysOnTop(true);
        viewer.showAndWait();
      }

    });

    setRowFactory(new Callback<TableView<LedgerRow>, TableRow<LedgerRow>>()
    {
      @Override
      public TableRow<LedgerRow> call(TableView<LedgerRow> tableView)
      {
        return new TableRow<LedgerRow>()
        {
          @Override
          protected void updateItem(LedgerRow row, boolean empty)
          {
            super.updateItem(row, empty);

            if (row == null)
              return;

            if (row.showRed())
            {
              getStyleClass().removeAll(Collections.singleton("blackRow"));
              getStyleClass().add("redRow");
            }
            else
            {
              getStyleClass().removeAll(Collections.singleton("redRow"));
              getStyleClass().add("blackRow");
            }
          }
        };
      }
    });
  }

  public void refresh()
  {
    setItems(null);
    layout();
    setItems(data);

    // This is a dirty way to do this.
    if (viewer != null)
    {
      if (viewer instanceof LoanViewer)
        ((LoanViewer) viewer).refresh();
      else
        ((AccountViewer) viewer).refresh();
    }

  }

  public void addLedgers(LinkedHashMap<String, Ledger> ledger,
                         GeneralLedger gl, AccountType type)
  {
    for (Ledger l : ledger.values())
    {
      data.add(new LedgerRow(l, gl, type));
    }
  }

  /**
   * Provides a row in the ledger view table. Each row represents a single
   * ledger - a total row can be created by specifying a null ledger, which
   * will then sum the entire side of the book.
   */

  public class LedgerRow
  {
    private final SimpleStringProperty name;
    private final SimpleStringProperty balance;

    DecimalFormat df = new DecimalFormat("###,###");

    private GeneralLedger gl;
    public  Ledger        ledger;
    private AccountType   type;

    public SimpleStringProperty balanceProperty()
    {
      return balance;
    }

    public SimpleStringProperty nameProperty()
    {
      return name;
    }

    public LedgerRow()
    {
      this.gl = null;
      this.ledger = null;
      this.name = new SimpleStringProperty("");
      this.balance = new SimpleStringProperty();
    }

    public LedgerRow(Ledger ledger, GeneralLedger gl, AccountType type)
    {
      this.gl = gl;
      this.ledger = ledger;
      this.type = type;

      this.name = new SimpleStringProperty(getName());
      this.balance = new SimpleStringProperty(getBalance());
    }

    public boolean showRed()
    {
      return !((gl == null) || (ledger == null))
             && (gl.myBank.reserveConstrained() && ledger.name.equals("reserve") || gl.myBank.capitalConstrained() && ledger.name.equals("capital"));
    }

    /**
     * Return name of ledger.
     *
     * @return Ledger name
     */
    public String getName()
    {
      if (gl == null)
        return "";
      else if (ledger != null)
        return ledger.getName();
      else
        return "Total";
    }

    /**
     * Return ledger's balance.
     *
     * @return Ledger balance
     */
    public String getBalance()
    {
      if (gl == null)
        return "0";
      else if (ledger != null)
      {
        return df.format(ledger.total());
      }
      else
      {
        if (type == AccountType.ASSET)
          return df.format(gl.totalAssets());
        else if (type == AccountType.LIABILITY
                 || type == AccountType.EQUITY)
          return df.format(gl.totalLiabilities());
      }
      return "-1"; // This shouldn't be reached if type is correct
    }
  }
}
