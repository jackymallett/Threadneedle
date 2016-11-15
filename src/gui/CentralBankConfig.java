/* Program: Threadneedle
 *
 * Central Bank configuration.
 *
 * Author : Copyright (c) Jacky Mallett
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
import javafx.scene.layout.GridPane;
import javafx.beans.value.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.text.*;

import core.*;

public class CentralBankConfig extends Stage implements Initializable
{
  @FXML private GridPane  grid;
  @FXML private Slider    reservePct;
  @FXML private Slider    capitalPct;
  @FXML private Label     reserve;
  @FXML private Label     capital;
  @FXML private TextField baseRate;
  @FXML private TextField iblDuration;
  @FXML private CheckBox  reserveControls;
  @FXML private CheckBox  capitalControls;
  @FXML private Button    okButton;

  CentralBank cb;

  public CentralBankConfig(CentralBank cbank)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/centralbankcfg.fxml"));
    fxmlLoader.setController(this);
    DecimalFormat df = new DecimalFormat("#.00");

    try
    {
      setScene(new Scene(fxmlLoader.load()));

      this.cb = cbank;

      this.setTitle(cb.name);

      baseRate.setText(Integer.toString(cb.getBaseRate()));
      iblDuration.setText(Integer.toString(cb.interBankDuration));

      reservePct.setValue(cb.cb_reserve);
      reservePct.setSnapToTicks(true);
      reserve.setText(df.format(cb.cb_reserve));

      capitalPct.setValue(cb.capitalPct);
      capitalPct.setSnapToTicks(true);
      capital.setText(df.format(cb.capitalPct));

      reserveControls.setSelected(cb.reserveControls);
      capitalControls.setSelected(cb.capitalControls);

      this.setAlwaysOnTop(true);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    reservePct.valueProperty().addListener((
                                             ObservableValue<? extends Number> obv, Number old_val, Number new_val) ->
                                           {
                                             reserve.setText(df.format(new_val));
                                           });
    capitalPct.valueProperty().addListener((
                                             ObservableValue<? extends Number> obv, Number old_val, Number new_val) ->
                                           {
                                             capital.setText(df.format(new_val));
                                           });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
  }

  @FXML void onOkButton(ActionEvent event)
  {
    cb.setBaseRate(Integer.parseInt(baseRate.getText()));
    cb.interBankDuration = Integer.parseInt(iblDuration.getText());
    cb.cb_reserve = (int) reservePct.getValue();
    cb.capitalPct = capitalPct.getValue();

    //Todo: extend to make govt set capital pct -> banking system

    cb.govt.capitalPct = cb.capitalPct;

    cb.reserveControls = reserveControls.isSelected();
    cb.capitalControls = capitalControls.isSelected();

    close();
  }

}
