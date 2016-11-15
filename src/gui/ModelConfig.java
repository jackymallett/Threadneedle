/*
 * Program: Threadneedle
 *
 * ModelConfig  - First panel displayed to user (select government)
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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.value.*;

import java.net.URL;
import java.util.*;
import java.io.*;

import com.google.gson.Gson;

import static base.Base.*;

public class ModelConfig extends Stage implements Initializable
{
  @FXML public TextField countryTextBox;
  @FXML private Button    okButton;
  @FXML private ChoiceBox bankingChoice;
  @FXML public ChoiceBox govtType;
  @FXML private TextField configFile;

  public DefaultConfig    config    = null;
  public SimulationConfig simconfig = null;

  public ModelConfig()
  {
    // Read in configurations
    try
    {
      Gson gson = new Gson();

      BufferedReader buff = new BufferedReader(new FileReader(MAIN_CONFIG));
      config = gson.fromJson(buff, DefaultConfig.class);
      buff.close();

      if (config.getLastConfigFile() != null)
      {
        buff = new BufferedReader(new FileReader(
          config.getLastConfigFile()));
        simconfig = gson.fromJson(buff, SimulationConfig.class);
      }
    }
    catch (Exception e)
    {
      if (config != null)
        System.out.println("Unable to open system config file: " + "~/"
                           + config.getLastConfigFile());
      else
        System.out.println("Failed to read configuration: " + MAIN_CONFIG);

    }

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/modelconfig.fxml"));

    fxmlLoader.setController(this);

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.setTitle("Set Simulation Government");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    // Add change listeners, to set dependent values. Note, this depends on
    // values set for
    // choice box in resources/modelconfig.fxml

    govtType.getSelectionModel().selectedIndexProperty()
            .addListener(new ChangeListener<Number>()
            {
              @Override
              public void changed(
                ObservableValue<? extends Number> obsValue,
                Number old, Number newno)
              {
                String choice = (String) govtType.getItems().get(newno.intValue());

                if (choice.equals("BaselGovt"))
                {
                  bankingChoice.setValue("Basel Capital");
                }
              }
            });

    bankingChoice.getSelectionModel().selectedIndexProperty()
                 .addListener(new ChangeListener<Number>()
                 {
                   @Override
                   public void changed(
                     ObservableValue<? extends Number> obsValue,
                     Number old, Number newno)
                   {
                     String choice = (String) bankingChoice.getItems().get(
                       newno.intValue());
                     if (choice.equals("Deposit"))
                     {
                       govtType.setValue("Govt");
                     }
                     else if (choice.equals("Basel Capital"))
                     {
                       govtType.setValue("BaselGovt");
                     }
                   }
                 });

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
    if (config != null)
    {
      configFile.setText(config.getLastConfigFile());
      if (simconfig != null)
      {
        setCountry(simconfig.getCountry());
        bankingChoice.setValue(simconfig.getBankingSystem());
        govtType.setValue(simconfig.getGovernment());
      }
    }
  }

  @FXML void onOkButton(ActionEvent event)
  {
    close();
  }

  public void setCountry(String name)
  {
    countryTextBox.setText(name);
  }

  public String getCountry()
  {
    return countryTextBox.getText();
  }

  public String getBankingSystem()
  {
    return (String) bankingChoice.getValue();
  }

  public String getGovtType()
  {
    return (String) govtType.getValue();
  }
}
