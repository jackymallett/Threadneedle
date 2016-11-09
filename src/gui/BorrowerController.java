package gui;

import javafx.fxml.*;
import javafx.scene.control.*;

import core.*;
import static gui.Common.simeng;

import static base.Base.*;

public class BorrowerController extends AgentController implements Initializable
{
  public static String fxml = "../../resources/borrowercfg.fxml";

  @FXML private TextField numberAccounts;
  @FXML private TextField depositAmount;
  @FXML private ChoiceBox accountType;
  @FXML private ChoiceBox lender;
  @FXML private ChoiceBox loanType;
  @FXML private TextField loanLength;
  @FXML private TextField defaultProb;
  @FXML private ChoiceBox employer;
  @FXML private TextField loanAmount;
  @FXML private TextField loanWindow;

  /**
   * Configure the borrower dialog. This has to be done outside of the
   * fxml initialization methods owing to the static instantiation of the
   * controller.
   */
  @Override
  public void configure()
  {
    // Add banks as options for lending and employment selection

    for (Bank bank : simeng.govt.banks.getBankList().values())
    {
      lender.getItems().add(bank.name);
      employer.getItems().add(bank.name);
    }

    // Allow unemployed agents if required

    employer.getItems().add("Unemployed");

    // Set default to be this bank

    lender.setValue(mainController.bank.name);
    employer.setValue(mainController.bank.name);

    loanLength.setText(loanLength.getPromptText());
    loanAmount.setText(loanAmount.getPromptText());
    depositAmount.setText(depositAmount.getPromptText());
    defaultProb.setText(defaultProb.getPromptText());
    loanWindow.setText(loanWindow.getPromptText());
  }

  @Override
  public Agent addAccount()
  {
    int deposit      = positiveInput(depositAmount.getText());
    int loan         = positiveInput(loanAmount.getText());
    int loanDuration = positiveInput(loanLength.getText());
    double probDefault = Double.parseDouble(defaultProb.getText().trim());

    String name = accountType.getValue() + "-" + getNextID();

    Borrower borrower = new Borrower(name, mainController.bank.govt,
                                     mainController.bank, deposit);

    borrower.loanAmount   = loan;
    borrower.loanDuration = loanDuration;
    borrower.borrowWindow = random.nextInt(positiveInput(loanWindow.getText())) + 1;

    //todo: alert user about error

    if (probDefault > 1.0)
    {
      probDefault = 1.0;
      System.out.println("Error: p(default) must be < 1.0");
    }

    borrower.defaultProb = probDefault;
    borrower.loanType = Loan.Type.valueOf((String) loanType.getValue());
    borrower.setLender(simeng.govt.banks.getBank((String) lender.getValue()));

    // Set employer if selected.

    if (!employer.getValue().equals("Unemployed"))
    {
      borrower.bankEmployee = true;
      simeng.govt.banks.getBank((String) employer.getValue()).hireEmployee(borrower, 1);
    }
    return borrower;
  }
}
