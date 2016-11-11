/* Program  : Threadneedle
 *
 * WorkBank : WorkBank is a Bank agent that hires workers.
 * 
 * Author   : (c) Jacky Mallett
 * Date     : October  2015
 * Comments :
 *
 * Licencesing: Commercial Commons 4.0 (see LICENSE)
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

import core.*;

/**
 * Simple bank that hires workers if it has sufficient interest
 * income, and fires them if it doesn't.
 */

public class WorkBank extends Bank
{
  double incomePctage = 0.20;

  public WorkBank(String name, Govt g, Bank b)
  {
     super(name, g, b);
  }

  /**
   * Provide properties map (unused) for interface with fxml, main screen.
   *
   * @param name  Name of agent
   * @param g     Government
   * @param b     Bank (unused)
   * @param properties Properties string passed in by fxml (unused)
   */
  public WorkBank(String name, Govt g, Bank b, HashMap<String, String> properties)
  {
     this(name, g, b);
  }

  public WorkBank(){super();}

  /**
   * Evaluation function for Bank.
   *
   * @param report t/f Print detailed report.
   * @param step       evaluation step
   */

   public void evaluate(boolean report, int step)
   {
      super.evaluate(report, step);

      // Hire people if there are sufficient funds.

      long labourcost = markets.getMarket("Labour").getAskPrice();

      if(employees.size() * gl.ledger("interest_income").total()
                                   > labourcost * 12 * employees.size())
      {
         hireEmployee();
      }
      else if(employees.size() * gl.ledger("interest_income").total() 
                                  > labourcost * 2 * employees.size())
      {
         fireEmployee();
      }

      paySalaries();
   }
}
