/* Program: Threadneedle
 *
 * Stage for modelconfig.fxml
 *
 * Author  :  (c) Jacky Mallett
 * Date    :  November 2014
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
