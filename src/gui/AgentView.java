package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;
import java.io.*;

import core.*;
import static base.Base.*;

public class AgentView extends Stage implements Initializable
{
  @FXML private Button copy;
  @FXML private Button delete;
  @FXML private TextField noCopy;

  private Agent agent;

  public AgentView(Agent agent)
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
      "../../resources/agentview.fxml"));
    fxmlLoader.setController(this);

	this.agent = agent;

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.setTitle(agent.name + " (" + agent.getClass().getSimpleName() + ")");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

  }

  // TODO: convert to observable list to allow real time update
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
  }

  // refactor mainControl??
  @FXML void copy(ActionEvent event)
  {
	  for(int i = 0; i < positiveInput(noCopy.getText()); i++);
	  {
		  Bank bank = agent.getBank();

		  //Object obj = simeng.addEntity(agent.getClass(), 
	//			                        agent.getProperties(), bank, null);


	//	  SimNode newNode = new SimNode(null,agent.getClass(), obj);
	  }
  }


  @FXML void delete(ActionEvent event)
  {
  }

  @FXML void onOkButton(ActionEvent event)
  {
    close();
  }
}
