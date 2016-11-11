/**
 * Program: Threadneedle
 *
 * AccountViewer
 *
 * Provide a view of the accounts being managed by a bank.
 *
 * Author : Jacky Mallett (c)
 * Date   : January 2015
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import core.Account;
import core.Bank;

import java.util.*;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Display accounts for either a deposit only bank, or for a specified
 * ledger.
 *
 * Todo: improve debt display. (duration, etc.)
 */

class AccountViewer extends Stage implements Initializable
{
  private Bank                      bank;
  private HashMap<Integer, Account> accounts;
  private ObservableList<AccountRow> data = FXCollections.observableArrayList();

  @FXML private TableColumn<AccountRow, Integer> idCol;
  @FXML private TableColumn<AccountRow, String>  nameCol;
  @FXML private TableColumn<AccountRow, Long>    depositCol;
  @FXML private TableColumn<AccountRow, Long>    debtCol;
  @FXML private TableView<AccountRow>            accountTable;

  /**
   * Constructor for account viewer for individual bank
   *
   * @param bank     Bank to display
   * @param accounts k to display
   */
  public AccountViewer(Bank bank, HashMap<Integer, Account> accounts)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/accountviewer.fxml"));
    fxmlLoader.setController(this);

    this.bank = bank;
    this.accounts = accounts;

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      this.setTitle(bank.name);

      if (!bank.govt.hasCentralBank)
        bank.obsAccounts.addListener((MapChangeListener.Change<? extends Integer, ? extends Account> change) -> {
          //TODO: implement ??
          System.out.println("*** onChanged ** AccountViewer");
        });

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    accountTable.setItems(data);

    idCol.setCellValueFactory(new PropertyValueFactory<>("Id"));
    nameCol.setCellValueFactory(new PropertyValueFactory<>("Name"));
    depositCol.setCellValueFactory(new PropertyValueFactory<>("Deposit"));

    accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    if (bank.govt.hasCentralBank)
    {
      debtCol.setCellValueFactory(new PropertyValueFactory<>("Debt"));
    }
    else
    {
      accountTable.getColumns().remove(debtCol);
      accountTable.layout();
    }

    for (Account account : accounts.values())
    {
      data.add(new AccountRow(account));
    }
  }

  /**
   * Update all ledger displays for simple deposit only bank
   */

  public void refresh()
  {
    accountTable.setItems(null);
    accountTable.layout();
    accountTable.setItems(data);
  }

  /**
   * Provide a row in the account view table. Each row represents
   * a single account.
   */
  public class AccountRow
  {
    private final Account               account;      // account being display in row
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty  name;
    private final SimpleLongProperty    deposit;
    private final SimpleLongProperty    debt;

    public AccountRow(Account account)
    {
      this.account = account;

      this.id = new SimpleIntegerProperty(getId());
      this.name = new SimpleStringProperty(getName());
      this.deposit = new SimpleLongProperty(getDeposit());
      this.debt = new SimpleLongProperty(getDebt());
    }

    // getters for account row values

    public Integer getId() {return account.getId();}

    public String getName() {return account.getName();}

    public Long getDeposit() {return account.getDeposit();}

    public Long getDebt() {return account.getTotalDebt();}

    public void setDeposit() {this.deposit.set(getDeposit());}

    public void setDebt() {this.deposit.set(getDebt());}
  }
}
 
