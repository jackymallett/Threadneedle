/* 
 * Program     : Threadneedle
 *
 * BankInvestor: An Agent that buys preferential shares in a bank.
 *
 * Author   : Jacky Mallett
 * Date     : October 2012
 *
 * Behaviour: Bank Investor's have 0 salary and receive all income from
 *            dividends/interest. Iff the current deposit is greater than
 *            the minimum specified investment, they will attempt to buy
 *            more bank shares from the specified bank they are an investor
 *            in. This does not imply that the bank has to sell them any.
 *
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package agents;

import core.*;
import com.google.gson.annotations.Expose;

public class BankInvestor extends Person
{
  @Expose public String investmentCompany = null; // Bank to buy capital of
  @Expose public long   initialCapital    = 0;

  Bank investment      = null;            // Target for investor's purchases
  int  minInvestAmount = 50;

  public void evaluate(boolean report, int step)
  {
    Shares shares = null;                 // investment purchased from company
    int purchased = 0;
    boolean bought = false;

    super.evaluate(report, step);

    // Attempt to buy bank shares

    if ((getDeposit() > minInvestAmount))
    {
      investment.sellInvestment(this, getDeposit() / investment.sharePrice, 
                                InvestmentType.ORDINARY);
    }
  }

  /**
   * Constructor within model.
   *
   * @param name    Unique and identifying name
   * @param g       Government
   * @param b       Bank for BankInvestor account/capital purchase
   * @param deposit Initial deposit
   */

  public BankInvestor(String name, Govt g, Bank b, long deposit)
  {
    super(name, g, b, deposit);

    this.employer = this;

  }

  public BankInvestor()
  {
    super();
    this.employer = this;
  }

  public void init(Govt g)
  {
     super.init(g);

     if(investmentCompany != null)
     {
        setInvestment(govt.getBank(investmentCompany),initialCapital);     
     }
  }

  // todo: fix investors getting accidentally hired
  @Override
  public boolean unemployed()
  {
     return false;
  }

  /**
   * Allow BankInvestor's to have a salary of 0
   *
   * @param newSalary New value for salary
   */
  @Override
  public void setSalary(long newSalary)
  {
    salary = newSalary;
  }

  /**
   * @param bank    Bank  investor will invest in.
   * @param capital Initial capital to invest
   */

  public void setInvestment(Bank bank, long capital)
  {
    investment = bank;
    investmentCompany = bank.name;
    initialCapital = capital;

    long noShares = initialCapital / bank.sharePrice;
    bank.sellCapital(this, noShares, bank.sharePrice, "BankInvestor Capital");
  }

  /**
   * Todo: Sell investment (allow investors to trade)
   *
   * @param to     Agent to sell investment to
   * @param amount Amount to sell for
   * @param period Period of investment to sell (loans)
   * @param type   Type of loan
   *
   * @return Investment being sold
   */

  public Object sellInvestment(Agent to, int amount, int period, String type)
  {
    throw new RuntimeException("Not implemented for this institution"
                               + this.name);
  }

}
