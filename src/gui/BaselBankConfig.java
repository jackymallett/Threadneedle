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
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.value.*;

import java.util.*;
import java.io.*;
import java.net.URL;

import core.*;

import static gui.Common.*;
import static base.Base.*;

public class BaselBankConfig extends Stage implements Initializable
{
  @FXML private TextField  depositAmount;
  @FXML private TextField  loanAmount;
  @FXML private TextField  interestRateDelta;
  @FXML private TextField  roundsCapitalInc;
  @FXML private TextField  capitalIncrease;
  @FXML private TextField  dividendIncrease;
  @FXML private Button     addAccounts;
  @FXML private Label      loanAmountTxt;
  @FXML private AnchorPane agentHolder;
  @FXML public  TextField  prefSharePrice;
  @FXML private TextField  lossProvisionPct;

  public Bank bank;           // Bank being configured
  public BankView  bankview;  // Ledger container - used to refresh view

  private AgentController     agent           = null;
  private AccountTypeListener accountListener = new AccountTypeListener();

  public BaselBankConfig(Bank b, BankView bv)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/bankcfg.fxml"));
    fxmlLoader.setController(this);

    this.bank = b;
    this.bankview = bv;
    this.setAlwaysOnTop(true);

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      AgentController.mainController = this;

      agent = AgentController.load(BorrowerController.class, "../../resources/borrowercfg.fxml");
      agent.configure();
      agent.accountType.getSelectionModel().selectedIndexProperty().addListener(accountListener);

      this.setTitle(bank.name);

      interestRateDelta.setText(Integer.toString(bank.interestRateDelta));
      roundsCapitalInc.setText(Integer.toString(bank.capitalSteps));
      prefSharePrice.setText(Long.toString(bank.sharePrice));
      dividendIncrease.setText(Double.toString(bank.capitalDividend));
      lossProvisionPct.setText(Double.toString(bank.lossProvisionPct));

      //      depositAmount.setText(depositAmount.getPromptText());
      //      capitalIncrease.setText(capitalIncrease.getPromptText());

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
  }

  /**
   * Loads the correct configuration panel for the agent selected
   * by the accountType choice box.
   *
   * @param node Node provided by agent's controller
   */
  public void setAgentConfig(Node node)
  {
    agentHolder.getChildren().setAll(node);
  }

  /**
   * Handle ok button from dialog.
   *
   * @param event event
   */
  @FXML void onSave(ActionEvent event)
  {
    bank.interestRateDelta = Integer.parseInt(this.interestRateDelta.getText().trim());
    bank.capitalPct        = positiveInput(capitalIncrease.getText());
    bank.lossProvisionPct  = Double.parseDouble(this.lossProvisionPct.getText().trim());

    try
    {
      bank.capitalDividend
        = Double.parseDouble(dividendIncrease.getText().trim());
    }
    catch (NumberFormatException e) { }

    close();
  }

  @FXML void addAccounts(ActionEvent event)
  {
    for (int i = 0; i < positiveInput(agent.numberAccounts.getText()); i++)
    {
      Agent a = agent.addAccount();
      simeng.addToContainers(a);
      bankview.refresh();
    }

    // Reset gui to show 0 

    agent.numberAccounts.setText("0"); 
  }

  class AccountTypeListener implements ChangeListener<Number>
  {
    @Override
    public void changed(ObservableValue<? extends Number> obsValue,
                        Number old, Number newNo)
    {
      String choice = (String) agent.accountType.getItems().get(newNo.intValue());
      TextField noAccounts;

      // Maintain common value for number of agents shared by all three dialogs

      noAccounts = agent.numberAccounts;

      switch (choice)
      {
         case "Borrower":
               agent = AgentController.load(BorrowerController.class, "../../resources/borrowercfg.fxml");
               break;

         case "Saver":
               agent = AgentController.load(SaverController.class, "../../resources/savercfg.fxml");
               break;

         case "Investor":
               agent = AgentController.load(InvestorController.class, "../../resources/investorcfg.fxml");
               break;
      }

      agent.numberAccounts.setText(noAccounts.getText());
      agent.accountType.getSelectionModel().selectedIndexProperty().addListener(accountListener);

      agent.configure();
    }
  }
}
