/* Program: Threadneedle
 *
 * SimNode 
 *
 * Container node for simulation objects in main view. Handles configuration
 * and provides link between simulation engine and objects in view.
 *
 * Author  :  Jacky Mallett
 * Date    :  July 2014
 */

package gui;

import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;

import java.util.*;

import javafx.stage.Stage;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import core.*;

public class SimNode extends Parent
{
  public SimpleDoubleProperty centerX = new SimpleDoubleProperty();
  public SimpleDoubleProperty centerY = new SimpleDoubleProperty();

  public Class type;            // Class of agent represented by this node
  public Tooltip tooltip;       // Associated tooltip string for agent...

  public Group group = new Group();
  public Point2D dragAnchor;
  public double  startDragX;
  public double  startDragY;

  // For nodes with employees, etc.
  public LinkedList<Maru> endnodes = new LinkedList<>();

  // Connecting lines from and to the node
  public BoundLine moneyFromMarket    = null;
  public Text      moneyFromMarketTxt = null;

  // Simobject is either the simulation object for this
  // node, or an error string suitable for model debugging.
  public Object simObject = "Undefined Object";

  ImageView imageView = new ImageView();
  public Image image = null;

  // Allow SimNodes to form a graph

  Set<Edge> adjacent;

  private int width = 20;

  /**
   *  Default constructor used by fxml.
   */
  public SimNode()
  {
    getChildren().addAll(imageView);
    setCache(true);
    addActionHandlers();
  }


  /**
   * Initialisation from fxml file.
   *
   * @param image Icon to use for this node.
   * @param type  class of node being represented in the simulation
   * @param tooltip tooltip for this node
   * @param properties properties list from fxml
   */
  public SimNode(Image image, String type, String tooltip, Map properties)
  {
    try
    {
      this.type = Class.forName("core." + type);
    }
    catch (Exception ce)
    {
      try
      {
          this.type = Class.forName("agents." + type);
      }
      catch(Exception e)
      {
          throw new RuntimeException(
                        "Failed to load simulation object from fxml");
      }
    }
    this.setImage(image,width);
    getChildren().addAll(imageView);

    addActionHandlers();

  }

  /**
   * Initialisation from drop into main panel, or no tooltip in parameters
   * in fxml file.
   *
   * @param image      Icon to use for this node.
   * @param type       class of node being represented in the simulation
   * @param properties List from fxml to be passed through to agent class
   */
  public SimNode(Image image, String type, Map properties)
  {
     this(image, type, null, properties); 
  }

  /**
   * Construct new node based on type and accompanying image. Use product
   * name for non-market Companies
   *
   * @param image   Image to display for object
   * @param type    type of object
   * @param obj     Object (agent to display)
   */
  public SimNode(Image image, Class type, Object obj)
  {
    this.type = type;

    if((obj instanceof Company) && !(obj instanceof Market) && 
	  !(obj instanceof StockExchange) && !(obj instanceof Bank) )
    {
       this.setImage(((Company)obj).product);
    }
    else
       this.setImage(image,width);

    this.simObject = obj;
    getChildren().addAll(imageView);

    addActionHandlers();
  }

  public SimNode(String file, Object simObject)
  {
    setImage(new Image(file),width);
    this.simObject = simObject;
    this.type = simObject.getClass();

    getChildren().addAll(imageView);
    addActionHandlers();
  }

  /**
   * Setter for type of agent being represented by this node in fxml
   *
   * @param type  Type (class) of defined agent.
   */
  public void setType(String type)
  {
    try
    {
      this.type = Class.forName("core." + type);
    }
    catch (Exception e)
    {
      try
      {
         this.type = Class.forName("agents." + type);
      }
      catch(Exception e1 ) 
      { 
         throw new RuntimeException(
               "Failed to load simulation object from fxml");
      }
    }
  }

  /**
   * Setter for tooltip defined in fxml
   *
   * @param tipstring  Tool tip string.
   */
  public final void setTooltip(String tipstring)
  {
    if(this.tooltip != null)
       Tooltip.uninstall(this, this.tooltip);

    tooltip = new Tooltip(tipstring);
    Tooltip.install(this, tooltip);
  }

  public final String getTooltip()
  {
     return this.tooltip.getText();
  }

  public final String getType()
  {
    return this.type.getSimpleName();
  }

  public Class getTypeClass()
  {
    return this.type;
  }

  /**
   * FXML interface version.
   */
  public final void setImage(Image image)
  {
    setImage(image,width);
  }

  /**
   * Create image from supplied string/
   *
   * @param text String to use for image
   */
  public final void setImage(String text)
  {
     Label label = new Label(text);
     label.setStyle("-fx-font-size: 8pt; -fx-background-color:black; -fx-text-fill:white;-fx-border-width:1;-fx-border-color:red;-fx-padding:2,2,2,2");
	 label.applyCss();
	 width = (int)label.prefWidth(-1);
	 Scene s  = new Scene(new Group(label));

	this.setImage(s.snapshot(null), width);
  }

  /**
   * Set image representing this node on mainPane.
   *
   * width  width for image
   *
   * @param image Image object containing png
   */

  
  public final void setImage(Image image, int width)
  {
    imageView.setImage(image);
    imageView.setFitWidth(width);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    imageView.setCache(true);

    this.image = image;
    if (imageView.getImage().errorProperty().getValue())
    {
      System.out.println("Error: failed to load image in SimNode");
    }
  }

  public void setDragStart(double x, double y)
  {
    this.startDragX = x;
    this.startDragY = y;
  }

  public final Image getImage()
  {
    return this.image;
  }

  /**
   * Return x position
   *
   * @return Position on mainPane.
   */

  public double getX()
  {
    return ((Agent) simObject).x;
  }

  public double getY()
  {
    return ((Agent) simObject).y;
  }

  /**
   * Add event/action/binding handlers for this node.
   */

  public void addActionHandlers()
  {
    addEventHandler(MouseEvent.MOUSE_CLICKED,
                    new EventHandler<MouseEvent>()
                    {
                      public void handle(MouseEvent event)
                      {
                        Stage stage = null;

                        // The following is order dependent due to the agent
                        // hierarchy underlying entities in the simulation.
                        // If no valid agent is found then the National
                        // Government config is displayed.

                        if (simObject instanceof Bank)
                        {
                          // stage = new
                          // BankController(((Bank)simObject).govt.banks);
                        }
                        else if (simObject instanceof Market)
                          stage = new MarketConfig((Market) simObject);
                        else if (simObject instanceof Company)
                          stage = new CompanyConfig((Company) simObject);
                        else if (simObject instanceof Region)
                          stage = new GovtConfig((Region)simObject);   

                        if (stage != null)
                          stage.showAndWait();

						if(simObject instanceof Company)
                            setImage(((Company)simObject).product);

                        event.consume(); 
                      }
                    });

    addEventHandler(MouseEvent.DRAG_DETECTED,
                    new EventHandler<MouseEvent>()
                    {
                      public void handle(MouseEvent e)
                      {
                        // System.out.println("Sim Drag Detected");
                      }
                    });

    /**
     * Handle move within pane.
     */

    addEventHandler(MouseEvent.MOUSE_RELEASED,
                    new EventHandler<MouseEvent>()
                    {
                      public void handle(MouseEvent event)
                      {
                        Point2D p = getParent().sceneToLocal(event.getSceneX(),
                                                             event.getSceneY());

                        Bounds b = getLayoutBounds();
                        relocate(p.getX() + b.getWidth() / 2,
                                 p.getY() + b.getHeight() / 2);
                      }
                    });

    // Setup binding for gui centering for lines

    Bounds bounds = getBoundsInParent();
    boundsInParentProperty().addListener(new ChangeListener<Bounds>()
    {
      @Override
      public void changed(
        ObservableValue<? extends Bounds> observableValue,
        Bounds oldBounds, Bounds bounds)
      {
        centerX.set(bounds.getMinX() + bounds.getWidth() / 2);
        centerY.set(bounds.getMinY() + bounds.getHeight() / 2);

      }
    });
  }

  SimpleDoubleProperty centerXProperty()
  {
    return centerX;
  }

  SimpleDoubleProperty centerYProperty()
  {
    return centerY;
  }

  public void relocate()
  {
    Bounds b = getLayoutBounds();
    relocate(((Agent) simObject).x + b.getWidth() / 2,
             ((Agent) simObject).y + b.getHeight() / 2);

  }

}
