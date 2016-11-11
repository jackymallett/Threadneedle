/*
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package gui;

import java.util.*;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
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

import core.*;

/**
 * Display accounts for either a deposit only Bank, or for a specified
 * ledger.
 *
 * Todo: improve debt display. (duration, etc.)
 */

public class LoanViewer extends Stage implements Initializable
{
  Bank                      bank;
  Account                   account;
  HashMap<Integer, Account> accounts;
  ObservableList<LoanRow> data = FXCollections.observableArrayList();

  @FXML TableColumn<LoanRow, Integer> idCol;
  @FXML TableColumn<LoanRow, String>  debtorCol;
  @FXML TableColumn<LoanRow, String>  typeCol;
  @FXML TableColumn<LoanRow, String>  interestCol;
  @FXML TableColumn<LoanRow, Integer> durationCol;
  @FXML TableColumn<LoanRow, Long>    debtCol;
  @FXML TableColumn<LoanRow, Long>    capitalpaidCol;
  @FXML TableColumn<LoanRow, Long>    interestpaidCol;
  @FXML TableColumn<LoanRow, Integer> remainingCol;
  @FXML TableView<LoanRow>            loanTable;

  /**
   * Constructor for account viewer for individual Bank
   *
   * @param bank    Bank to display
   * @param account Account containing loans to display
   */
  public LoanViewer(Bank bank, Account account)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/loanviewer.fxml"));
    fxmlLoader.setController(this);

    this.bank = bank;
    this.account = account;

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      this.setTitle(account.ledger + " ledger: " + account.capital_loans.size() + " borrowers");
/*
      obsLoans.addListener((MapChangeListener.Change<? extends Integer, ? extends Loan> change) -> 
      {
          //TODO: implement ??
          System.out.println("*** onChanged ** LoanViewer");
      });
*/
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources)
  {
    loanTable.setItems(data);

    idCol.setCellValueFactory(new PropertyValueFactory("Id"));
    debtorCol.setCellValueFactory(new PropertyValueFactory("Debtor"));
    typeCol.setCellValueFactory(new PropertyValueFactory("Type"));
    durationCol.setCellValueFactory(new PropertyValueFactory("Duration"));
    interestCol.setCellValueFactory(new PropertyValueFactory("Interest"));
    debtCol.setCellValueFactory(new PropertyValueFactory("Debt"));
    interestpaidCol.setCellValueFactory(new PropertyValueFactory("Interestpaid"));
    capitalpaidCol.setCellValueFactory(new PropertyValueFactory("Capitalpaid"));
    remainingCol.setCellValueFactory(new PropertyValueFactory("Remaining"));

    loanTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    for (Loan loan : account.capital_loans.values())
         data.add(new LoanRow(loan));
  }

  /**
   * Update all ledger displays for simple deposit only Bank.
   */

  public void refresh()
  {
    data.clear();
    for (Loan loan : account.capital_loans.values())
         data.add(new LoanRow(loan));
  }

  /**
   * Provide a row in the account view table. Each row represents
   * a single account.
   */
  public class LoanRow
  {
    private final Loan                  loan;                 // account being display in row
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty  debtor;
    private final SimpleStringProperty  type;
    private final SimpleStringProperty  interest;
    private final SimpleIntegerProperty duration;
    private final SimpleLongProperty    debt;
    private final SimpleLongProperty    capitalpaid;
    private final SimpleLongProperty    interestpaid;
    private final SimpleIntegerProperty remaining;

    public LoanRow(Loan loan)
    {
      this.loan = loan;

      this.id = new SimpleIntegerProperty(getId());
      this.debtor = new SimpleStringProperty(getDebtor());
      this.type = new SimpleStringProperty(getType());
      this.interest = new SimpleStringProperty(getInterest());
      this.duration = new SimpleIntegerProperty(getDuration());
      this.debt = new SimpleLongProperty(getDebt());
      this.capitalpaid = new SimpleLongProperty(getCapitalpaid());
      this.interestpaid = new SimpleLongProperty(getInterestpaid());
      this.remaining = new SimpleIntegerProperty(getRemaining());
    }

    // getters for account row values

    public Integer getId() {return loan.Id;}

    public String getDebtor() {return loan.borrower.getName();}

    public String getType() {return loan.getType();}

    public Long getDebt() {return loan.getCapitalOutstanding();}
    public Integer getDuration(){return loan.duration;}
    public String getInterest(){return String.valueOf(loan.interestRate) + "%";}
    public Long getInterestpaid(){return loan.interestPaid;}
    public Long getCapitalpaid(){return loan.capitalPaid;}
    public Integer getRemaining(){return loan.duration - loan.payIndex;}

  }
}
 
