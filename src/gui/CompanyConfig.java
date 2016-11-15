/*
 * Program: Threadneedle
 *
 * CompanyConfig  Configuration pane for a company.
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.*;
import base.*;

class CompanyConfig extends Stage implements Initializable
{
  @FXML private TextField companyName;
  @FXML private TextField labourInput;
  @FXML private TextField marketName;
  @FXML private TextField money;
  @FXML private TextField employees;
  @FXML private TextField bankname;
  @FXML private ChoiceBox region;
  @FXML private ChoiceBox<String> productName;

  private Company company;

  public CompanyConfig(Company c)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/companyconfig.fxml"));
    fxmlLoader.setController(this);

    this.company = c;

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      this.setTitle("Company");

      companyName.setEditable(false);
      employees.setEditable(false);

      companyName.setText(company.name);
      labourInput.setText(Integer.toString(company.labourInput));
      marketName.setText(company.getOutputMarket().getName());
      money.setText(Long.toString(company.getDeposit()));
      employees.setText(Long.toString(company.getNoEmployees()));
      bankname.setText(company.getBankName());

      for(String key : company.govt.regions.keySet())
            region.getItems().addAll(key);

      if(company.region != null)
      {
         region.setValue(company.region.name);
      }

      for(Market market : company.govt.markets.markets)
      {
         if(!(market instanceof LabourMarket))
            productName.getItems().addAll(market.product);
      }

      productName.setValue(company.product);

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

  public void setCompanyName(String name)
  {

  }

  @FXML
  public void setProductName(String name)
  {
    System.out.println("new product = " + name);
  }

  @FXML void onOkButton(ActionEvent event)
  {
    company.changeProduct(productName.getValue());
    company.labourInput = Integer.parseInt(labourInput.getText());

    // Did user change amount of money on deposit?

    int input_money = Integer.parseInt(money.getText());

    if(company.getDeposit() < input_money)
    {
       company.getBank().printMoney(company.getAccount(),
                                   input_money - company.getDeposit(),
                                   "(Exogeneously) Modified by user");

	   // Save as initial deposit, if simulation not running.
	   if(Base.step == 1)
		   company.initialDeposit = company.getDeposit();
    }
    else if(company.getDeposit() > input_money)
       System.out.println("Reducing money supply not available");

    if(company.region != null && region.getValue() != null) 
    {
      if((region.getValue() != company.region.name) || !region.getValue().equals(""))
      {
          company.region = company.govt.regions.get(region.getValue());
       }
    }
    close();
  }

}
