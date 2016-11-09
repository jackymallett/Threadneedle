/* Program : Threadneedle
 *
 * Company : 
 * 
 * Author  : Jacky Mallett
 * Date    : April 2015
 *
 * Comments:
 *   Configuration - inventory name is assumed to stay constant
 *           
 *
 */
package agents;

import com.google.gson.annotations.*;
import java.util.*;

import statistics.*;
import core.*;

import static base.Base.*;
import static statistics.Statistic.Type.*;

/*
 * Builder builds a single house at a time, according to:
 *
 *    labourInput - total labour required to build house
 *    buildTime   - total time required to build house
 *
 */
public class Builder extends Company
{
  @Expose private int buildTime       = 100;
  private int  HOUSE_TTL      = 120;
  private Statistic s_mkt_price;
  private boolean building    = false;		 // t/f house being built
  private long totalBuilt     = 0;   		 // total built to this point
  private long totalCost      = 0;
  private long salariesPaid   = 0;
  long profitMargin     = 25;		 // % profit margin to put on price
  private Widget house = null;		 // House, when completed
  private int    inflation = 0;


  /**
   * Constructor:
   *
   * @param name       Name for company
   * @param govt       Government for company
   * @param bank       Bank for company
   * @param properties Property map (to interface with fxml) 
   */
  public Builder(String name, Govt govt, Bank bank,
                 HashMap<String, String> properties)
  {
    super(name, Integer.parseInt(properties.get("initialDeposit")), govt,
          bank);

    this.labourInput = Integer.parseInt(properties.get("labourInput"));
    this.buildTime   = Integer.parseInt(properties.get("buildTime"));
    this.product     = properties.get("product");

    if((this.market = govt.markets.getMarket(this.product)) == null)
    {
       this.market = new HouseMarket("M-" + this.product, this.product, govt, bank);
       this.market.region = this.region;
       govt.markets.addMarket(this.market);
    }

    initStatistics();
  }

  /**
   * Constructor from gson file
   */
  public Builder()
  {
    initStatistics();
  }

  /**
   * Initialise statistics for this agent.
   */
  @Override
  public void initStatistics()
  {
    super.initStatistics();

    s_mkt_price = new Statistic(Id + ": mkt_price", AVERAGE);

    s_quantityProduced = Statistic.getStatistic(product + "-produced",
                                                "production", COUNTER);
    s_quantitySold = new Statistic(Id + ":q sold", COUNTER);

    System.out.println("Builder: buildTime = " + buildTime + 
                       " labourInput = " + labourInput);

  }

  /**
   * Evaluation method 
   *
   * @param report T/F print report
   * @param step   step in model
   */

  protected void evaluate(boolean report, int step)
  {
    long   startDeposit;            // funds available at start of round
    long   salaryBill;              // salaries payable at start of round
    double interestcost;
    long   labourcost;
    long   interestRate;
    int    totalWorkers;

    salaryBill     = getSalaryBill();
    startDeposit   = getDeposit();

    // Production function
    //
    // Verify workers are available to hire, work out salary costs,
    // and request loan based on estimated cost to build. Note - 
    // if the company has an outstanding loan, it will not be able
    // to get another one until the existing loan is repaid.
    //
    // If loan is granted, attempt to hire workers.
    //   -- if the company can't hire, it will incur loan costs, while
    //      not building.
    //   -- the alternative is to hire the workers first, and then fire
    //      them if a loan isn't forthcoming.
    //   -- so we cheat, and check the number of workers available
    //      to hire.


    if(!building && getDebt() <= 0 
       && markets.getMarket(product).getTotalItems() < 2)
    {
       // total workers required is total labour divided by the length of
       // of time the builder should take to build the house.

       totalWorkers = labourInput/buildTime;

       if(((LabourMarket)markets.getMarket("Labour")).totalAvailableWorkers() 
                                                              >= totalWorkers)
       {
         // Work out total cost of building, and request a loan
         //    labourInput is total labour required - each labourInput
         //    costs the salary price for that round. 
         labourcost  = markets.getMarket("Labour").getAskPrice() * labourInput; 

//         labourcost = 50 * labourInput;
         interestRate= getBank().requestInterestRate(BaselWeighting.CONSTRUCTION);

         // Use double the labourcost as the amount for the loan:
         //     - profit
         //     - cost of covering loan while waiting for sale

         totalCost = (long)(labourcost * 1.1);
         long totalLoan = (long)(1.5 * labourcost);

System.out.println(name + " req loan for " + totalLoan  + "/" + buildTime*2);

         if(getBank().requestLoan(this.getAccount(),
                      totalLoan,      buildTime * 2, Time.MONTH,
                      BaselWeighting.CONSTRUCTION, Loan.Type.COMPOUND) != null)
         {
            while(employees.size() < totalWorkers)
            {
                // Hire at market rate. If we are unable to hire all employees
                // then building will continue with reduced workers - which 
                // will slightly increase cost of the building if it happens
                // due to additional interest payments
                Person worker = 
                  hireEmployee(markets.getMarket("Labour").getAskPrice(), 
                               getBank(), null);
                if(worker == null)
                {
                   System.out.println("DBG: " + getName() + " failed to hire @" +  markets.getMarket("Labour").getAskPrice());
                   break;
                }
            }
            building = true;
            getAccount().outgoing = 0;
            getAccount().incoming = 0;
         }
         else System.out.println(name + " failed to get loan " + labourcost);
       }
    }

    if(building && Time.endOfMonth())
    {
       totalBuilt   += employees.size();
       salariesPaid += paySalaries();
    }


    if((totalBuilt >= labourInput) && building)
    {
       house = new Widget(product, HOUSE_TTL, 1);
       s_quantityProduced.add(house.quantity());

       long marketPrice = markets.getMarket(product).getBidPrice();

       // totalCost += getAccount().getTotalInterestPaid();

       if(marketPrice > totalCost)
          totalCost = marketPrice;

       for(Person worker : employees)
       {
          worker.setSalary(worker.getSalary() + inflation / 5);
          worker.desiredSalary = worker.getSalary();
       }

       long price = markets.getMarket(product).sell(house, 
                                         totalCost + inflation, getAccount());

       //System.out.println("On market: " + markets.getMarket(product).getTotalItems());
       System.out.println(name + " listed - salaries paid:" + salariesPaid + " sell price " + price );
       System.out.println(name + " total cost " + getAccount().outgoing);
       building = false;
       house    = null;
       totalBuilt = 0;
       totalCost  = 0;
       salariesPaid = 0;
       fireAllEmployees();

       //inflation += 1;
    }

    // Pay off loan if money available.
    for(Loan loan : getAccount().debts.values())
    {
        if((loan.getCapitalOutstanding() < getDeposit()) && !building)
        {
           getBank().payBankLoan(getAccount(), loan, loan.getCapitalOutstanding());
        }
    }

    payDebt();                           // debt before taxes...
    payTax(govt.corporateTaxRate, govt.corporateCutoff);
  }

  public void print(String label)
  {
    if (label != null)
      System.err.println(label);

    System.err.println(name + ": " + employees.size() + " Input     : "
                       + labourInput + " Output    : " + output + " Deposit: $"
                       + getDeposit());
  }

}
