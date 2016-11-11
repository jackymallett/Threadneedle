/*
 * Program: Threadneedle
 *
 * Visual display of Simulation.
 *
 * Author : Jacky Mallett
 * Date   : September 2013
 *
 * Threadneedle is provided free for non-commercial research purposes under 
 * the creative commons Attribution-NonCommercial-NoDerivatives 4.0 
 * International License:
 *
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package gui;

import java.lang.*;

import javafx.geometry.*;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.beans.property.*;

import core.*;
import static gui.Common.*;

// billmill.org/pymag-trees
// gephi.github.io
// hiveplot
// http://stackoverflow.com/questions/19748744/javafx-how-to-connect-two-nodes-by-a-line
// http://johnthecodingarchitect.blogspot.com/2013/11/implementing-custom-javafx-bindings.html

public class MainPane extends Pane
{
  int              noMarkets = 0; // No. of markets currently on pane
  int              noBanks   = 0; // No. of banks currently on pane

  public MainPane()
  {
    new Button();                 // fix for lazy css loading issue.

    // Handlers for actions detected on the main pane.

    addEventHandler(MouseEvent.DRAG_DETECTED,
                    event -> {
                      System.out.println("Drag Detected");
                    });

    addEventHandler(MouseEvent.MOUSE_DRAGGED,
                    new EventHandler<MouseEvent>()
                    {
                      public void handle(MouseEvent event)
                      {
                        // System.out.println("Mouse dragged");
                      }
                    });
    addEventHandler(MouseEvent.MOUSE_RELEASED,
                    event -> {
                      // System.out.println("Mouse dropped: " );
                    });

    addEventHandler(MouseEvent.MOUSE_CLICKED,
                    event -> {
						if(event.getButton() == MouseButton.PRIMARY)
						{
                           Stage stage = new GovtConfig(simeng.govt);
                           stage.showAndWait();
                           event.consume();
						}
						else if (event.getButton() == MouseButton.SECONDARY)
						{
						   System.out.println("r button clicked");
						}
                    });
  }

  /**
   * Dynamic information update (money flow status)
   */

  public void update()
  {
    if (simeng == null)
      throw new RuntimeException("No simulation engine in mainPanel");


    for (Node node : getChildren())
    {
      if (node instanceof SimNode)
      {
         showMarketPaths(node);
      }
    }
  }


  public void positionAll()
  {
    for (Node n : getChildren())
    {
      if (n instanceof SimNode)
      {
        n.relocate(((SimNode) n).getX(), ((SimNode) n).getY());
      }
    }
  }

  /**
   * Add an agent to the panel using local layout rules. (Interface for load
   * simulation from file.)
   *
   * @param leftMenu used to interface to fxml
   * @param agents agents to add
   */

  public void autoAddAll(LeftMenu leftMenu, Object... agents)
  {
    for (Object a : agents)
    {
      SimNode simnode = leftMenu.getSimNode(a.getClass(), a);

      if ((simnode != null) && !(simnode.simObject instanceof String))
      {
        this.autoAddAll(simnode);
      }
      /* Fail silently here as not all nodes can be added in the gui.
         System.out.println("Unable to add " + a.getClass()
                           + " to panel - not defined in left menu");
      */
    }
  }

  /**
   * Automatically add nodes to the panel using local layout rules.
   *
   * @param nodes to add.
   */

  public void autoAddAll(SimNode... nodes)
  {
    for (SimNode node : nodes)
    {
      Agent a = (Agent) (node.simObject);

      if (a == null)
        throw new RuntimeException("SimNode has no agent");

      this.getChildren().add(node);

      if (a instanceof Market)
        autoRelocateMarket(node);
      else if (a instanceof Bank)
        autoRelocateBank(node);
      else if (a instanceof Company)
        System.out.println("implement company autorelocate");
      else
        System.out.println("*T* Unknown node in autoAddAll "
                           + a.getClass().getSimpleName());
    }
  }

  /**
   * Calculate auto-market positions, based roughly on a spiral algorithm,
   * placed roughly in middle.
   *
   * @param node Node to be placed.
   */

  public void autoRelocateMarket(SimNode node)
  {
    double dy = 0.0;
    double dx = 20.0;

    Agent agent = (Agent) node.simObject;

    for (int i = 0; i < noMarkets; i++)
    {
      dx = -1 * dy;
      dy = dx;

      if (i % 4 == 0)
      {
        dx /= 2;
        dy /= 2;
      }
    }

    agent.x = getWidth() / 2 + dx;
    agent.y = getHeight() / 2 + dy;

    noMarkets++;

    node.relocate(agent.x, agent.y);
  }

  /**
   * Calculate Bank positions - line them up in the middle
   * Todo: better graphics, remove hard coded positioning
   *
   * @param node Node to be placed.
   */

  public void autoRelocateBank(SimNode node)
  {
    Agent agent = (Agent) node.simObject;

    if (agent.x % 2 == 0)
      agent.x = getWidth() / 2 - 10.0 * noBanks;
    else
      agent.x = getWidth() / 2 + 10.0 * noBanks;

    agent.y = getHeight() / 2 + 40;

    noBanks++;

    node.relocate(agent.x, agent.y);
  }


  /**
   * Show the connection between companies and their markets.
   *
   * @param n node to connect to its market
   */


  public void showMarketPaths(Node n)
  {
        Agent agent = (Agent) ((SimNode) n).simObject;
        SimNode node = (SimNode) n;

        // Connect companies to their markets.

        Platform.runLater(() -> { 
			
		  Point2D pt = null;

          if ((agent instanceof Company) && !(agent instanceof Market)
              && !(agent instanceof Bank))
          {
            SimNode market = lookup(((Company) agent).market);

			// If there isn't a market visible, then the path can't be shown.
			if(market == null) return;

		    Maru m;

            if (node.moneyFromMarket == null)
            {
              node.moneyFromMarket = new BoundLine(node, market, Color.BLACK);
              pt = node.moneyFromMarket.getMidpoint();

              node.moneyFromMarketTxt = new Text(pt.getX(), pt.getY(), "");

              getChildren().add(node.moneyFromMarket);
              getChildren().add(node.moneyFromMarketTxt);
            }
            else
              node.moneyFromMarketTxt.setText(Long.toString(((Company) agent).s_income.get()));

			// Display number of employees for company 
			  // This relies on the company representation only having one
		      // information node.
			  if(node.endnodes.size() == 0)
			  {
				  m = drawEmployees((Company)agent, 
						          node.centerX.get() - 40.0,
								  node.centerY.get() + 40.0);
				  node.endnodes.add(m);
			  }
			  else
			  {
				  m = node.endnodes.getFirst();
				  m.label.setText("" + ((Company)agent).getNoEmployees());
              }

              m.salaryFromEmployer = new BoundLine(node.centerXProperty(), 
									             node.centerYProperty(), 
												 m.centerXProperty(), 
												 m.centerYProperty(), 
												 Color.BLUE);
              m.salaryFromTxt.setText(""+((Company)agent).c_salariesPaid);

			  pt = m.salaryFromEmployer.getMidpoint();
			  m.salaryFromTxt.setX(pt.getX() - 20);
			  m.salaryFromTxt.setY(pt.getY());

              getChildren().add(m.salaryFromEmployer);

			  m.toFront();
			  m.label.toFront();
              n.toFront();
		  }

        });
   }

   private Maru drawEmployees(Company agent, double x, double y)
   {
     Maru m    = new Maru(Color.GREEN, 10.0, "" + (agent.getNoEmployees()),x,y);
     getChildren().addAll(m, m.label, m.salaryFromTxt);
	 return m;
   }

  /**
   * Return simnode representing agent in pane.
   *
   * TODO: replace with lookup table for large simulations.
   *
   * @param agent agent to find
   * @return Return simnode for agent
   */
  public SimNode lookup(Agent agent)
  {
    for (Node n : getChildren())
    {
      if ((n instanceof SimNode) && ((SimNode) n).simObject == agent)
        return (SimNode) n;
    }
    System.out.println("Lookup in mainpane for agent " + agent.name
                       + " failed");
    return null;
  }


  /**
   * Find nearest Bank to supplied point.
   *
   * @param x reference co-ordinate
   * @param y reference co-ordinate
   * @return nearest Bank, or null if no banks currently in simulation
   */

  public Bank findNearestBank(Double x, Double y)
  {
    double distance = Double.MAX_VALUE, d;
    Bank found = null;

    for (Node n : getChildren())
    {
      if((n instanceof SimNode) && (((SimNode) n).simObject instanceof Bank))
      {
        Agent a = (Agent) ((SimNode) n).simObject;

        d = Math.sqrt(Math.pow(x - a.x, 2) + Math.pow(y - a.y, 2));

        if (d < distance)
        {
          distance = d;
          found = (Bank) a;
        //System.out.println("Distance : " + d + " " + ((Bank)found).name);
        }
      }

    }

    return found;
  }
}
