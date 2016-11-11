/**
 * AgentController
 *
 * Provides a dialog within the dialog framework for the BaselBankController
 * configuration dialog. This allows multiple different agent types to be
 * specified from the controller.
 *
 * Author : (c) Jacky Mallett
 * Date   : January 2015
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package gui;

import java.io.*;
import java.net.URL;
import java.util.*;

import javafx.fxml.*;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import core.*;

public abstract class AgentController
{
  public static BaselBankConfig mainController;   // Main dialog controller

  @FXML private Button    addAccounts;
  @FXML public  ChoiceBox accountType;            // Type of agent 
  @FXML public  TextField numberAccounts;         // Number of accounts to add

  /**
   * Load the controller for the specified agent class and return to main
   * controller.
   *
   * @param agentclass     Class of agent controller to load
   * @param fxml           Resource file for specified agent controller
   * @return agent controller for type agentclass
   */
  @FXML public static AgentController load(Class agentclass, String fxml)
  {
    if (mainController == null)
      throw new RuntimeException("Failed to set dialog controller");

    try
    {
      FXMLLoader loader = new FXMLLoader(agentclass.getResource(fxml));
      mainController.setAgentConfig(loader.load());
      return loader.getController();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public void initialize(URL url, ResourceBundle resourceBundler)
  {
  }

  /**
   * Handler for the add account button
   *
   * @param event event
   */
  @FXML public void addAccounts(ActionEvent event)
  {
    mainController.addAccounts(event);
  }

  public abstract void configure();

  public abstract Agent addAccount();
}
