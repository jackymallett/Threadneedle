/*
 * Program: Threadneedle
 *
 * TextDialog - provides text display dialog
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

import java.lang.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.event.*;
import java.util.*;
import java.net.URL;
import java.io.*;



public class TextDialog extends Stage implements Initializable
{
    @FXML public Button saveButton;
    @FXML public TextArea  description;

    public TextDialog(String title)
    {
       FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../resources/textdialog.fxml"));
       fxmlLoader.setController(this);

       try
       {
          setScene(new Scene(fxmlLoader.load()));

          setTitle(title);
       }
       catch(IOException e)
       {
          e.printStackTrace();
       }
    }

    @Override public void initialize(URL url, ResourceBundle resourceBundle) 
    {
    }

    @FXML void onSaveButton(ActionEvent event)
    {
       close();
    }

    @FXML public void onClose()
    {
      close();
    }

    @FXML public void onMinimize()
    {
      close();
    } 

}
