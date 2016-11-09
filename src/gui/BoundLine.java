/* Program: Threadneedle
 *
 * BoundLine
 *
 * Provides connected line between two nodes, centered in middle of node.
 *
 * Author : Jacky Mallett
 * Date   : October 2014
 */

package gui;

import javafx.geometry.*;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.beans.property.*;

public class BoundLine extends Line
{
  private Color colour; // Line colour

  /**
   * Constructor for nodes which provide centerXProperty.
   *
   * @param startNode starting node
   * @param endNode   ending node
   * @param colour    colour to use for line
   */

  BoundLine(SimNode startNode, SimNode endNode, Color colour)
  {
    this(startNode.centerXProperty(), startNode.centerYProperty(), 
	 	 endNode.centerXProperty(), endNode.centerYProperty(), colour);
  }

  /**
   * Constructor specifying endpoints.
   *
   * @param startX x starting co-ordinate
   * @param startY y starting co-ordinate
   * @param endX   x ending co-ordinate
   * @param endY   y ending co-ordinate
   * @param colour colour to use for line
   */
  BoundLine(DoubleProperty startX, DoubleProperty startY,
            DoubleProperty endX, DoubleProperty endY, Color colour)
  {
    startXProperty().bind(startX);
    startYProperty().bind(startY);
    endXProperty().bind(endX);
    endYProperty().bind(endY);

    setStrokeWidth(1);
    setStroke(colour);
  }

  /**
   * Get midpoint of line for label.
   * @return midpoint for this line
   */
  public Point2D getMidpoint()
  {
    Bounds b = this.getBoundsInParent();
    return new Point2D(b.getMinX() + 5 + b.getWidth() / 2, b.getMinY()
                                                           + b.getHeight() / 2);
  }
}
