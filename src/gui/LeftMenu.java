/* Program: Threadneedle
 *
 * Container class for LeftMenu to add a convenience lookup function
 * for looking up valid agent classes for the simulation. This allows
 * agents in the simulation to be configured through fxml/dynamically.
 *
 * Author : Jacky Mallett
 * Date   : September 2014
 */
package gui;

import javafx.scene.*;
import javafx.scene.layout.*;
import gui.*;

public class LeftMenu extends VBox
{
  /**
   * Return a copy of the node in the menu representing this type, suitable
   * for adding to the main view panel.
   *
   * @param type Type (simple class name) of node being copied.
   * @param obj object to contain
   * @return SimNode for supplied node
   */
  public SimNode getSimNode(Class type, Object obj)
  {
    for (Node n : this.getChildren())
    {
      if (((SimNode) n).type.equals(type))
      {
        return new SimNode(((SimNode) n).image, type, obj);
      }

    }
    return null;
  }
}
