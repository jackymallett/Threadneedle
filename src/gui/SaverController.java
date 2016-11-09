package gui;

import javafx.fxml.*;
import javafx.scene.control.*;

import core.*;
import agents.*;

import static base.Base.*;

public class SaverController extends AgentController implements Initializable
{
  public static String fxml = "../../resources/savercfg.fxml";

  @FXML private TextField depositAmount;
  @FXML private TextField numberAccounts;

  /**
   * Configure the borrower dialog. This has to be done outside of the
   * fxml initialization methods owing to the static instantation of the
   * controller.
   */
  @Override
  public void configure()
  {
    depositAmount.setText(depositAmount.getPromptText());
  }

  @Override
  public Agent addAccount()
  {
    int deposit = positiveInput(depositAmount.getText());

    String name = accountType.getValue() + "-" + getNextID();

    return new Saver(name, mainController.bank.govt,
                           mainController.bank, deposit);
  }
}
