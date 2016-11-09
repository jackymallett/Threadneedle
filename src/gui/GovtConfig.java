package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.value.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.*;
import static gui.Common.*;

public class GovtConfig extends Stage implements Initializable
{
  @FXML private TextField personalTaxRate;
  @FXML private TextField personalCutoff;
  @FXML private TextField corporateCutoff;
  @FXML private TextField corporateTaxRate;
  @FXML private TextField treasuryRate;
  @FXML private TextField maxCivilServants;
  @FXML private TextField minWage;
  @FXML private TextField name;
  @FXML private ChoiceBox banks;
  @FXML private CheckBox  unemployment;
  @FXML private TextField dolePayment;
  @FXML private GridPane  grid;

  public  Govt govt;
  private FXMLLoader fxmlLoader;
  private boolean addingRegion = false;

  /**
   * Constructor for adding regions to the simulation.
   */
  public GovtConfig()
  {
    fxmlLoader = new FXMLLoader(getClass().getResource(
                                "../../resources/govtconfig.fxml"));
    fxmlLoader.setController(this);

    addingRegion = true;

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      // Populate bank choice box
  
      for(Bank b : simeng.govt.getBankList().values())
          banks.getItems().add(b.name);

      banks.getSelectionModel().selectFirst();

    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  public GovtConfig(Govt govt)
  {
    fxmlLoader = new FXMLLoader(getClass().getResource(
                               "../../resources/govtconfig.fxml"));
    fxmlLoader.setController(this);

    this.govt = govt;

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      name.setText(govt.name);

 
      // Populate bank choice box
 
      for(Bank b : simeng.govt.getBankList().values())
          banks.getItems().add(b.name);

      if(govt.govtBank != null)
          banks.getSelectionModel().select(govt.govtBank);
      else
          banks.getSelectionModel().select("Central Bank");

      if(govt instanceof core.Region)
      {
         this.setTitle("Regional Govt ");
      }
      else
      {
         this.setTitle("National Govt");
         treasuryRate.setText(Double.toString(govt.treasuryRate));
         banks.getItems().add(govt.getBank().name);
      }

      banks.getSelectionModel().selectFirst();
      personalTaxRate.setText(Integer.toString(govt.personalTaxRate));
      personalCutoff.setText(Integer.toString(govt.personalCutoff));
      corporateTaxRate.setText(Integer.toString(govt.corporateTaxRate));
      corporateCutoff.setText(Integer.toString(govt.corporateCutoff));
      maxCivilServants.setText(Integer.toString(govt.maxCivilServants));
      minWage.setText(Integer.toString(govt.minWage));
	  dolePayment.setText(Integer.toString(govt.unemploymentPayment));
	  unemployment.setSelected(govt.payUnemployment);

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
     name.textProperty().addListener(new ChangeListener<String>()
     {
       @Override
       public void changed(ObservableValue<? extends String> observable,
                           String oldname, String newname)
       {
          // Allow name to be changed, but check this is not a new
          // region being created, since govt won't be set yet.
          if(govt != null)
             govt.name = newname;
       }
     });
  }

  // todo:update values here
  // todo: allow money to be changed here, if not in basel banking system
  @FXML void onOkButton(ActionEvent event)
  {
    // Add new region to the simulation.
    if(addingRegion)
    {
      this.govt   = new core.Region(name.getText(), simeng.govt,
                                    simeng.govt.getBank((String)banks.getValue()));
    }
    govt.personalTaxRate  = Integer.parseInt(personalTaxRate.getText());

    govt.corporateTaxRate = Integer.parseInt(corporateTaxRate.getText());
    govt.personalCutoff   = Integer.parseInt(personalCutoff.getText());
    govt.corporateCutoff  = Integer.parseInt(corporateCutoff.getText());
    govt.maxCivilServants = Integer.parseInt(maxCivilServants.getText());
   
    govt.name = name.getText();

    govt.payUnemployment     = unemployment.isSelected();
	govt.unemploymentPayment = Integer.parseInt(dolePayment.getText());

    if(!(govt instanceof core.Region))
    {
       govt.treasuryRate  = Double.parseDouble(treasuryRate.getText());
    }

	int minwage = Integer.parseInt(minWage.getText()); 

	// Check simulation and increase current salary if necessary

    if(govt.minWage < minwage)
    {
		for(Person p : simeng.employees)
		{
           if(p.getSalary() < minwage)
			  p.setSalary(minwage);
		}
	}

	govt.minWage = minwage;

    close();
  }

}
