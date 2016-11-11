/**
 * Program: Threadneedle
 *
 * SummaryView 
 *
 * Provide a summary of the state of the agents in the running simulation.
 *
 * Author : Jacky Mallett (c)
 * Date   : August 2015
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import java.util.*;
import javafx.util.*;
import javafx.stage.*;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import core.*;

import static gui.Common.simeng;


public class SummaryView extends Stage implements Initializable
{
  @FXML TableView<SummaryRow> summaryTable;

  @FXML TableColumn<SummaryRow, String> typeCol;
  @FXML TableColumn<SummaryRow, String> nameCol;
  @FXML TableColumn<SummaryRow, String> productCol;
  @FXML TableColumn<SummaryRow, Long>   employeesCol;
  @FXML TableColumn<SummaryRow, Long>   depositCol;
  @FXML TableColumn<SummaryRow, Long>   debtCol;
  @FXML TableColumn<SummaryRow, String> employerCol;
  @FXML TableColumn<SummaryRow, String>   salaryCol;
  @FXML TableColumn<SummaryRow, Long>   balanceCol;

  ObservableList<SummaryRow> data = FXCollections.observableArrayList();
 
  public SummaryView()
  {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../resources/summaryview.fxml"));
    fxmlLoader.setController(this);

    try
    {
      setScene(new Scene(fxmlLoader.load()));
      this.getScene().getStylesheets().add("../resources/summaryview.css");
      this.setTitle("Simulation Summary");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
     summaryTable.setItems(data);

     typeCol.setCellValueFactory(new PropertyValueFactory("Type"));
     nameCol.setCellValueFactory(new PropertyValueFactory("Name"));
     productCol.setCellValueFactory(new PropertyValueFactory("Product"));
     employeesCol.setCellValueFactory(new PropertyValueFactory("Employees"));
     depositCol.setCellValueFactory(new PropertyValueFactory("Deposit"));
     debtCol.setCellValueFactory(new PropertyValueFactory("Debt"));
     employerCol.setCellValueFactory(new PropertyValueFactory("Employer"));
     salaryCol.setCellValueFactory(new PropertyValueFactory("Salary"));
     balanceCol.setCellValueFactory(new PropertyValueFactory("Balance"));

     // Provide visual status for tax and debt payments not being made.

     summaryTable.setRowFactory(new Callback<TableView<SummaryRow>,TableRow<SummaryRow>>(){
          @Override
          public TableRow<SummaryRow> call(TableView<SummaryRow> table)
          {
             final TableRow<SummaryRow> row = new TableRow<SummaryRow>()
             {
               @Override
               protected void updateItem(SummaryRow row, boolean empty)
               {
                  super.updateItem(row, empty);
                  if(!empty)
                  {
                     if(!row.agent.paidDebts)
                     {
                        getStyleClass().removeAll(Collections.singleton("blackRow"));
                        getStyleClass().add("navyRow");
                     }
                     else
                     {
                        getStyleClass().removeAll(Collections.singleton("navyRow"));
                        getStyleClass().add("blackRow");
                     }
                  }
               };
             };
             return row;
          }
         });

	 // Provide operations on individual agents in summary table
/*
	 summaryTable.setOnMouseClicked(event -> {
		 SummaryRow row = summaryTable.getSelectionModel().getSelectedItem();
		 Stage stage = new AgentView(row.agent);
		 stage.setAlwaysOnTop(true);
		 stage.showAndWait();
		 });
*/
     refresh();
  }

  /**
   * Update view with new round information.
   */
  public void refresh()
  {
     data.clear();
     data.add(new SummaryRow(simeng.govt));

     for(Bank bank : simeng.govt.getBankList().values())
     {
         data.add(new SummaryRow(bank));
     }

     for(Market market : simeng.govt.markets.markets)
     {
         data.add(new SummaryRow(market));
     }

     /* 
      * Sort to provide a consistent viewing order, without
      * effecting order in simeng in order to avoid any interaction
      * with simulation evaluation (which is randomised before each step
      * to avoid order of evaluation issues).
      */

     ArrayList<Company> sorted_companies 
                               = new ArrayList<>(simeng.companies);

     Collections.sort(sorted_companies, Agent.Comparators.NAME);

     for(Company company : sorted_companies)
         data.add(new SummaryRow(company));


     ArrayList<Person> sorted_employees 
                            = new ArrayList<>(simeng.employees);
     Collections.sort(sorted_employees, Agent.Comparators.NAME);

     for(Person employee : sorted_employees)
     {
         data.add(new SummaryRow(employee));
     }
  }

  public class SummaryRow
  {
     public Agent agent;

     private final SimpleStringProperty  type = new SimpleStringProperty();
     private final SimpleStringProperty  name = new SimpleStringProperty();
     private final SimpleLongProperty    employees = new SimpleLongProperty();
     private final SimpleLongProperty    deposit  = new SimpleLongProperty();
     private final SimpleLongProperty    debt     = new SimpleLongProperty();
     private final SimpleStringProperty  employer = new SimpleStringProperty();
     private final SimpleStringProperty  salary   = new SimpleStringProperty();
     private final SimpleLongProperty    balance  = new SimpleLongProperty();

     public SummaryRow(Agent agent)
     {
        this.agent = agent;
     }

     public String  getType(){ return agent.getClass().getSimpleName();}
     public String  getName(){ return agent.name;}
     public Long    getDeposit(){return agent.getDeposit();}
     public Long    getDebt(){return agent.getDebt();}

     /**
      * Provide the step wise input vs output on the main account for
      * the agent. Bank has to be handled separately at the moment, since
      * it's main account is subject to change.
      *
      * @return incoming - outgoing on account this step
      */
     public Long getBalance()
     {
        if(agent instanceof Bank)
           return ((Bank)agent).getAccount().incoming -
                         ((Bank)agent).getAccount().outgoing;
        else
           return agent.getAccount().incoming - agent.getAccount().outgoing;
     }

     public String getSalary()
     {
        if(agent instanceof Person)
           return Long.toString(((Person)agent).getSalary());
        else
           return "-";
     }

	 public String getProduct()
	 {
		 if(agent instanceof Company)
			 return ((Company)agent).product;
		 else
			 return "";
	 }

     public String  getEmployer()
     {
        if(agent instanceof Person)
           return ((Person)agent).getEmployer();
        else
           return "";
     }

     public Long getEmployees()
     {
        if(agent instanceof LabourMarket)
           return ((LabourMarket)agent).totalAvailableWorkers();
        else
           return (long)(agent.employees.size());
     }
  }
}
