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
import static gui.Common.simeng;

public class MarketConfig extends Stage implements Initializable
{
  @FXML private TextField marketName;
  @FXML private TextField productName;
  @FXML private TextField money;
  @FXML private TextField employees;
  @FXML private TextField stockSize;
  @FXML private TextField maxSpread;
  @FXML private TextField minSpread;
  @FXML private TextField maxStock;
  @FXML private TextField minCapital;
  @FXML private CheckBox  payDividend;
  @FXML private Button    okButton;
  @FXML private ChoiceBox<String> region;
  @FXML private ChoiceBox<String> bankName;
  @FXML private TextField TTL;

  Market market;

  public MarketConfig(Market m)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/marketconfig.fxml"));
    fxmlLoader.setController(this);

    this.market = m;

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      this.setTitle("Market");

      marketName.setEditable(false);

      marketName.setText(market.name);
      productName.setText(market.product);
      money.setText(Long.toString(market.getDeposit()));
      employees.setText(Long.toString(market.maxEmployees));
      stockSize.setText(Long.toString(market.getTotalItems()));
      maxStock.setText(Long.toString(market.maxInventory));
      stockSize.setText(Long.toString(market.getTotalItems()));
      maxSpread.setText(Long.toString(market.maxSpread));
      minSpread.setText(Long.toString(market.minSpread));
	  minCapital.setText(Long.toString(market.minCapital));

	  TTL.setText(Integer.toString(market.ttl));

	  payDividend.setSelected(market.payDividend);


      for(String key : market.govt.regions.keySet())
            region.getItems().addAll(key);

      if(market.region != null)
      {
         region.setValue(market.region.name);
      }

      for(String key : simeng.govt.banks.getBankList().keySet())
            bankName.getItems().addAll(key);

      bankName.setValue(market.getBankName());

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public MarketConfig()
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/marketconfig.fxml"));
    fxmlLoader.setController(this);
 
    
    try
    {
      setScene(new Scene(fxmlLoader.load()));

      this.setTitle("Market");


      for(String key : simeng.govt.regions.keySet())
          region.getItems().addAll(key);

      if(simeng.govt.region != null)
      {
         region.setValue(simeng.govt.region.name);
      }

      for(String key : simeng.govt.banks.getBankList().keySet())
            bankName.getItems().addAll(key);

      bankName.getSelectionModel().selectFirst();


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


  // todo:update values here
  @FXML void onOkButton(ActionEvent event)
  {
    // Rudimentary validation on valid input. 

    if(marketName.getText().equals("") || productName.getText().equals(""))
    {
       close();
       return;
    }
    // Check market exists for product. Pass null in for region, 
    // and then check for it from the config menu.
    if(market == null)
    {
      simeng.govt.markets.createMarket(marketName.getText(),
                           productName.getText(),
                           simeng.govt.banks.getBank(bankName.getValue()),
                           Long.parseLong(money.getText()),
                           null);
                                        
      market = simeng.govt.markets.getMarket(productName.getText());
    }

    market.maxEmployees = Integer.parseInt(employees.getText());
    market.maxInventory = Integer.parseInt(maxStock.getText());

	market.maxSpread    = Integer.parseInt(maxSpread.getText());
	market.minSpread    = Integer.parseInt(minSpread.getText());
	
	market.minCapital   = Integer.parseInt(minCapital.getText());

	market.setTTL(Integer.parseInt(TTL.getText()));

	market.payDividend  = payDividend.isSelected();

    if(((market.region != null) && (!region.getValue().equals(market.region.name)))
        || region.getValue() != null)
    {
       market.region = market.govt.regions.get(region.getValue());
    }

    // Did amount of money change?

    int input_money = Integer.parseInt(money.getText());

    if(market.getDeposit() < input_money)
    {
       market.getBank().printMoney(market.getAccount(),
                                   input_money - market.getDeposit(),
                                   "(Exogenously) Modified by user");

	   // Update deposit for config file iff first round.
	   if(Base.step == 1)
		   market.initialDeposit = market.getDeposit();
    }
    else if(market.getDeposit() > input_money)
       System.out.println("Decreasing the money supply not available here");

    // todo: error dialog
    // market.labourInput = Integer.parseInt(labourInput.getText());
    close();
  }
}
