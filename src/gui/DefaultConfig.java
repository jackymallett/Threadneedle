/* Program: Threadneedle
 *
 * Stage handler for default configuration menu, defaultconfig.fxml
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

public class DefaultConfig
{
  String lastConfigFile;
  String author;

  public String getLastConfigFile()
  {
    return lastConfigFile;
  }

  public String getAuthor()
  {
    return author;
  }

  public void setLastConfigFile(String file)
  {
    this.lastConfigFile = file;
  }

  public void setAuthor(String author)
  {
    this.author = author;
  }
}
