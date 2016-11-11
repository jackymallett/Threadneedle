/* Program : Threadneedle
 *
 * Company : Implements a farm. A farm is a specialisation of company that
 *           produces food.
 * 
 * Author  : Jacky Mallett
 * Date    : April 2012
 *
 * Comments:
 *   Configuration - inventory name is assumed to stay constant
 *
 *
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package agents;

import java.util.*;

import statistics.*;
import core.*;

import static base.Base.*;
import static statistics.Statistic.Type.*;

public class Farm extends Company
{
  public Statistic s_mkt_price;

  public int overUnder = 0;

  // Local factors for decision making
  public double savingsRate = 0.1; // 10% savings rate

  /**
   * Constructor:
   *
   * @param name       Name for individual farm
   * @param govt       Government this farm belongs to
   * @param bank       Bank farm has deposit at
   * @param properties Property map (to interface with fxml) product Product
   *                   farm is producing initialDeposit initial deposit with
   *                   bank labourInput amount of labour(employees) to produce
   *                   a unit of product
   */
  public Farm(String name, Govt govt, Bank bank,
              HashMap<String, String> properties)
  {
    super(name, Integer.parseInt(properties.get("initialDeposit")), govt,
          bank);

    this.labourInput = Integer.parseInt(properties.get("labourInput"));
    this.product = properties.get("product");

    initStatistics();
  }

  public Farm()
  {

  }

  public void initStatistics()
  {
    super.initStatistics();

    s_mkt_price = new Statistic(Id + ": mkt_price", AVERAGE);

    s_quantityProduced = Statistic.getStatistic(product + "-produced",
                                                "production", COUNTER);
    s_quantitySold = new Statistic(Id + ":q sold", COUNTER);

  }

  /**
   * Evaluation method for Farm.
   *
   * @param report T/F print report
   * @param step   step in model
   */

  public void evaluate(boolean report, int step)
  {
    Widget food;
    long initialDeposit;                    // Amount on deposit at beginning
    long depositChange;                     // Change in monetary holdings
    long saleQuantity = 0;                  // Amount sold to market
    long salariesPaid = 0;                  // Amount actually paid

    // Determine initial conditions

    initialDeposit = getDeposit();

    // Output is based on employees at start of round.

    output = employees.size() * labourInput;

    // Turn output into widgets -- attempt to sell to market

    if ((output != 0) && (market.getMaxLot() > 0))
    {
      food = new Widget(product, market.ttl, output);

      s_quantityProduced.add(food.quantity());

      DEBUG(name + " produced " + food.quantity() + " # employees "
                 + employees.size());

      if (market.getMaxLot() < food.quantity()) // market won't buy total
      {
        saleQuantity  = market.getMaxLot();
        lastSoldPrice = market.sell(food.split(saleQuantity), -1,
                                    this.getAccount());
      }
      else
      {
        saleQuantity  = food.quantity();
        lastSoldPrice = market.sell(food, -1, this.getAccount());
      }

      s_quantitySold.add(saleQuantity); // update statistic

      if (lastSoldPrice == -1)
      {
        DEBUG(name + " sale to market failed ");
      }
      else
      {
        DEBUG(name + " sold " + saleQuantity + " units " + " @ "
              + lastSoldPrice);
        s_income.add(lastSoldPrice * s_quantitySold.get());
        s_mkt_price.add(lastSoldPrice);
      }
    }
    else
    {
      DEBUG(name + " no sale to market - maxlot is : "
            + market.getMaxLot() + " output " + output);
    }

    // Pay salaries. This must be done before any firing decisions are
    // made otherwise they won't get paid for work completed.

    salariesPaid = paySalaries();

    s_labourCost.add(salariesPaid);

    // Determine actions for next round.
    //
    //   Prices are rising - attempt to hire
    //   Prices are falling - reduce employees

    long profit  = lastSoldPrice * saleQuantity - salariesPaid;

    //if(offeredSalary > markets.getMarket("Labour").getAskPrice())


    //if((s_mkt_price.increasing(2) || employees.size() == 0) && 
    //   (surplus >= 2 * offeredSalary))
   offeredSalary = profit/2;

   if(offeredSalary <= govt.minWage) offeredSalary = govt.minWage;

   if((employees.size() == 0) || getDeposit() > 3 * salariesPaid) 
   {
      if (hireEmployee(offeredSalary, null, null) == null)
      {
	    offeredSalary += 1;
      }
   }
   else if (s_income.shrinking(2))
   {
      if (offeredSalary > 1)
        decreaseSalaries(1);
      else
        fireEmployee();
   }

    // Pay Taxes
    payTax(govt.corporateTaxRate, govt.corporateCutoff);

    //System.out.println(name + " funds: " + getDeposit());
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
