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
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.beans.value.*;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.*;
import gui.LabourViewController.LabourRow;
import static gui.Common.*;
import static base.Base.*;

public class WorkerConfig extends Stage implements Initializable
{
  @FXML private ChoiceBox<String> bank;
  @FXML private Button    product;
  @FXML private TextField noWorkers;
  @FXML private TextField purchase;
  @FXML private TextField consume;
  @FXML private TextField store;  
  @FXML private TextField deposit;
  @FXML private TextField initialSalary;
  @FXML private CheckBox  useloan;
  @FXML private CheckBox  consumable;
  @FXML private CheckBox  randomPurchase;

  @FXML private GridPane gridPane;

  private TextField addMoney;

  public  Profile workerProfile;

  boolean rotate  = false;
  boolean changed = false;
  LabourRow labourRow;

  /**
   * Constructor for adding regions to the simulation.
   *
   * @param labourrow row containing workers to configure/add
   */
  public WorkerConfig(LabourRow labourrow)
  {

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
              "../../resources/workerconfig.fxml"));
    fxmlLoader.setController(this);

    this.labourRow = labourrow;
	this.setTitle("Worker Configuration");

    try
    {
       setScene(new Scene(fxmlLoader.load()));

       // Modifying an existing profile, or creating a new one?
       workerProfile = new Profile();

       for(Market market : simeng.govt.markets.markets)
       {
           if(!(market instanceof LabourMarket))
              workerProfile.addNeed(market.product, 0, 0, 0, 0, true, false);
       }

       // Update with settings for worker if present

       if(labourrow.getNoWorkers() > 0)
       {
          for(Need need : labourrow.workers.getFirst().profile.needs.values())
          {
              workerProfile.needs.put(need.product, need);
          }
       }

       // Set defaults

       for(Bank b : simeng.govt.getBankList().values())
       {
           bank.getItems().addAll(b.name);
       }
       
       // Are there any needs defined for the worker?

       if(labourrow.getProduct() != null)
       {
          product.setText(labourrow.getProduct());
       }
       else if(workerProfile.needs.size() > 0)
       {
          product.setText(workerProfile.needs.firstEntry().getKey());
       }

       // Is there a bank ?

       if(labourrow.getBankName() != null)
       {
          bank.setValue(labourrow.getBankName());
          deposit.setText(labourrow.getInitialDeposit());
          initialSalary.setText(labourrow.getInitialSalary());
       }
       else
       {
          bank.getSelectionModel().selectFirst();
          deposit.setText("0");
          initialSalary.setText("1");
       }
       
       Need need = workerProfile.getNeed(product.getText());

       if(need != null)
       {
          consume.setText(Integer.toString(need.consumption));
          store.setText(Long.toString(need.storeQ));
          purchase.setText(Integer.toString(need.quantity));
          useloan.setSelected(need.useLoan);
          consumable.setSelected(need.consumable);
       }

	   randomPurchase.setSelected(labourrow.getRandomPurchase());
       noWorkers.setText(Integer.toString(labourRow.getNoWorkers()));
  
       // Detect any changes, and reprogram the underlying labour row

       ChangeListener<String> textListener = new ChangeListener<String>()
         {
            @Override
            public void changed(ObservableValue<? extends String> obs,
                                String oldvalue, String newValue)
            {
               try
               {
                  if(!rotate)
                    setNeedVariables(workerProfile.getNeed(product.getText())); 
               }
               catch(NumberFormatException exception){}

               changed = true;
            }
         };

       ChangeListener<String> productListener = new ChangeListener<String>()
         {
            @Override
            public void changed(ObservableValue<? extends String> obs,
                                String oldvalue, String newValue)
            {
               setProductVariables(workerProfile.getNeed(newValue));
            }
         };

       ChangeListener<Boolean> checkListener = new ChangeListener<Boolean>()
         {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs,
                                Boolean oldvalue, Boolean newValue)
            {
               setNeedVariables(workerProfile.getNeed(product.getText())); 
	           labourRow.setRandomPurchase(randomPurchase.isSelected());
               changed = true;
            }
         };

       ChangeListener<String> noWorkersListener = new ChangeListener<String>()
       {
            @Override
            public void changed(ObservableValue<? extends String> obs,
                                String oldvalue, String newValue)
            {
               changed = true;
            }
       };

       ChangeListener<String> initialSalaryListener = new ChangeListener<String>()
       {
            @Override
            public void changed(ObservableValue<? extends String> obs,
                                String oldvalue, String newValue)
            {
               changed = true;
            }
       };

       consume.textProperty().addListener(textListener);
       store.textProperty().addListener(textListener);
       purchase.textProperty().addListener(textListener);
       useloan.selectedProperty().addListener(checkListener);
       consumable.selectedProperty().addListener(checkListener);
       randomPurchase.selectedProperty().addListener(checkListener);
       noWorkers.textProperty().addListener(noWorkersListener);
       deposit.textProperty().addListener(textListener);
       initialSalary.textProperty().addListener(initialSalaryListener);

    }
    catch(IOException e)
    {
       e.printStackTrace();
    }
  }

  /**
   * Rotate product button. Use a guard to prevent the automatic
   * change detection incorrectly updating the wrong need.
   *
   * @param event scroll event
   */
  @FXML protected void productRotate(ScrollEvent event)
  {
     String selected = product.getText();

     rotate = true;

     Need need = workerProfile.getNext(product.getText());

     if(need == null)
        need = workerProfile.addNeed(product.getText(), 0, 0, 0, 0, true,false);

     setProductVariables(need);

     rotate = false;
  }

 public void setNeedVariables(Need need)
 {

    try
    {
      need.consumption =  Integer.parseInt(consume.getText().replaceAll("\\s+",""));
      need.quantity    =  Integer.parseInt(purchase.getText().replaceAll("\\s+",""));
      need.storeQ      =  Integer.parseInt(store.getText().replaceAll("\\s+",""));
      need.useLoan     =  useloan.isSelected();
      need.consumable  =  consumable.isSelected();

    }
    catch (Exception e)
    {
      // todo: more detailed input alerting
    }
  }

  /**
   * Set the visible attributes for the product/need being displayed from the
   * stored need.
   *
   * @param need   Need to display
   */
  public void setProductVariables(Need need)
  {
     product.setText(need.product);
     consume.setText(Integer.toString(need.consumption));              
     store.setText(Long.toString(need.storeQ));
     purchase.setText(Integer.toString(need.quantity));
     useloan.setSelected(need.useLoan);
     consumable.setSelected(need.consumable);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
     // Allow money to be added to each worker if simulation already
     // setup.

     if(labourRow.getNoWorkers() > 0)
     { 
     Label label = new Label("Add Money");
	 addMoney    = new TextField();

	 gridPane.add(label, 0, 5);
	 gridPane.add(addMoney, 1, 5);
     gridPane.setMargin(label, new Insets(0, 0, 0, 10.0));
		 }
  }

  // todo:update values here
  // todo: allow money to be changed here, if not in basel banking system
  @FXML void onOkButton(ActionEvent event)
  {
    if(changed)
    {
       // The number of workers is blank on Add... and so maybe incorrect
       // here. Catch the exception - LabourViewController will get a new row 
       // with 0 workers specified (default), and should ignore it.
       try
       {
          int no = Integer.parseInt(noWorkers.getText());

		  if(no < 1)
	      {
			  System.out.println("GUI: no. of workers to add was < 1");
		      return;
		  }

          // Update the supplied labour row with new workers/config
          // LabourViewController will then add it to its list when
          // the dialog is closed.
          labourRow.update(workerProfile, 
                           Integer.parseInt(noWorkers.getText()),
                           Integer.parseInt(deposit.getText()),
                           Integer.parseInt(initialSalary.getText()),
						   randomPurchase.isSelected(),
                           bank.getValue());
       }
       catch (NumberFormatException e) { }

       changed = false;
    }
    close();
  }

}
