/* Program: Threadneedle
 *
 * Calculate screen settings for individual panes to adapt to screen size.
 *
 * Author : Copyright (c) Jacky Mallett
 * Date   : February 2016
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

public class ScreenSettings
{
   int numberScreens;
   double screenWidth;
   double screenHeight;
   double midX, midY;

   double[][] mainScreenSize = {{400.0, 400.0}};

   ScreenSettings()
   {
      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] devices = env.getScreenDevices();

      Dimension bounds = Toolkit.getDefaultToolkit().getScreenSize();

      screenWidth  = bounds.getHeight();
      screenHeight = bounds.getWidth()/numberScreens;

      midX = screenWidth;
      midY = screenHeight;

      System.out.println(bounds.getHeight() + " x " + bounds.getWidth());

      numberScreens = devices.length;


   }

}
