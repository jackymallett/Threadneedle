/* Program: Threadneedle
 *
 * Stage for modelconfig.fxml
 *
 * Author  :  (c) Jacky Mallett
 * Date    :  November 2014
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

public class SimulationConfig
{
  String government;
  String bankingSystem;
  String country;

  public String getGovernment()
  {
    return government;
  }

  public String getBankingSystem()
  {
    return bankingSystem;
  }

  public String getCountry()
  {
    return country;
  }

  public void setGovernment(String g)
  {
    this.government = g;
  }

  public void setBankingSystem(String bs)
  {
    this.bankingSystem = bs;
  }

  public void setCountry(String c)
  {
    this.country = c;
  }
}
