package gui;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import java.text.*;

import core.*;

import agents.*;
import static base.Base.*;

public class InvestorController extends AgentController implements Initializable
{
  public static String fxml = "../../resources/investorcfg.fxml";

  @FXML private TextField shares;
  @FXML private TextField cashCreated;
  @FXML private TextField numberAccounts;

  @FXML
  protected void displayCashCreated(KeyEvent event)
  {
    DecimalFormat df = new DecimalFormat("###,###");

    long noShares = positiveInput(shares.getText());
    long capital = noShares * Long.parseLong(mainController.prefSharePrice.getText().trim());
    cashCreated.setText(df.format(capital));
  }

  /**
   * Configure the borrower dialog. This has to be done outside of the
   * fxml initialization methods owing to the static instantiation of the
   * controller.
   */
  @Override
  public void configure()
  {
    shares.setText(shares.getPromptText());
  }

  @Override
  /**
   * Purchase capital and create account for agent.
   */
  public Agent addAccount()
  {
    int noShares = positiveInput(shares.getText());
    long capital = noShares * Long.parseLong(mainController.prefSharePrice.getText().trim());

    String name = accountType.getValue() + "-" + getNextID();

    BankInvestor bankInvestor = new BankInvestor(name, mainController.bank.govt,
                                                 mainController.bank, 0);
    bankInvestor.setInvestment(mainController.bank, capital);

    return bankInvestor;
  }
}
