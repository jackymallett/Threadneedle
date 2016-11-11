/*
 * Project : Threadneedle
 *
 * Provide simple wrapper for a circle on 2D display.
 *
 * Author : (c) Jacky Mallett
 * Date   : October 2014
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

public class Maru extends Circle
{
  public BoundLine salaryFromEmployer = null;
  public Text      salaryFromTxt      = new Text("");
  public Text      label              = new Text("");   // Placed in middle 


  Maru(Color c, double radius, String text, double x, double y)
  {
    super(x, y, radius);

    setFill(c);
    setStroke(Color.GREEN);
    setStrokeWidth(2);
    setStrokeType(StrokeType.OUTSIDE);

	label.setText(text);
    label.relocate(x-5.0,y-5.0);
  }

}
