/* Program: Threadneedle
 *
 * Stage handler for default configuration menu, defaultconfig.fxml
 *
 * Author  :  (c) Jacky Mallett
 * Date    :  November 2014
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
