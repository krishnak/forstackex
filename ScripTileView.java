package krishna.core.ui;

import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ScripTileView extends VBox {
    

    @FXML
    private Text askQuantity;

    @FXML
    private Text averageAsk;

    @FXML
    private Text averageBid;

    @FXML
    private Text bidQuantity;

    @FXML
    private Text change;

    @FXML
    private Text high;

    @FXML
    private Text highestAsk;

    @FXML
    private Text low;

    @FXML
    private Text lowestBid;

    @FXML
    private Text ltt;

    @FXML
    private Text open;

    @FXML
    private Text close;

    @FXML
    private Text scripname;

    @FXML
    private Text stockprice;

    @FXML
    private Text volume;

    @FXML
    private Text vwap;
    @FXML
    private Circle indicator;

    @FXML
    public Rectangle vwapbackground;
 
    @FXML
    public Rectangle openbackground;
    @FXML
    public Rectangle highbackground;
    @FXML
    public Rectangle lowbackground;

    @FXML
    public Text percentsign;
   
    @FXML

    private HBox leftcolorbox;
    @FXML

    private HBox rightcolorbox;

    
    private Color normal ;

    private ArrayList<Rectangle> isBeingAnimatedList = new ArrayList<>();
   
    public  ScripTileView()
    {
            try {
                    FXMLLoader fxmlLoader= new FXMLLoader();

                    fxmlLoader.setLocation(getClass().getResource("/fxml/colorfulltptile.fxml"));
                    fxmlLoader.setRoot(this);
                    fxmlLoader.setController(this);
                    fxmlLoader.load();
                    indicator.setVisible(false);
                    normal = Color.valueOf("#2a2a2a");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        
    }
   
    public void enableIndicator(boolean enable)
    {
        if(true)
            indicator.setVisible(enable);
    }
    public Text getAskQuantity() {
        return askQuantity;
    }

    
 
    public Text getAverageAsk() {
        return averageAsk;
    }

    public Text getChange()
    {
        return change;
    }


    public Text getAverageBid() {
        return averageBid;
    }

    public Text getBidQuantity() {
        return bidQuantity;
    }

    
    public Text getClose() {
        return this.close;
    }
    public Text getHigh()
    {
        return this.high;
    
    }
    

    public Text getHighestAsk() {
        return this.highestAsk ;
    }

    public void setInvisibleInOptionTile()
    {
        lowestBid.setVisible(false);
        highestAsk.setVisible(false);
        leftcolorbox.setVisible(false);
        rightcolorbox.setVisible(false);
    }

    

    public Text getLow() {
        return this.low;
    }

    public Text getLowestBid() {
        return lowestBid;
    }


    public Text getLTT()
    {
        return ltt;
    }
    public Text getOpen()
    {
        return open;
    }

    
    public Text getStockprice()
    {
        return this.stockprice;
    }
    public Text getScripName()
    {
        return this.scripname;
    }
    public Text getVolume() {
        return this.volume;
    }


    public Text getVWAP() {
        return this.vwap;
    }

    public Rectangle getVWAPBackground()
    {
        return vwapbackground;
    }

    public synchronized boolean  isBeingAnimated(Rectangle rect)
    {
        if(isBeingAnimatedList==null || isBeingAnimatedList.size()==0)
            return false;

        return isBeingAnimatedList.contains(rect);
        
    }

    public  void runAnimationonRectangle(Color transitionColor,Rectangle rect)
    {
        
        Task<Void> rectangleFlashTask = new Task<Void>() {
            @Override protected Void call() throws Exception {
                rect.setFill(transitionColor);
                    
                for(int i =10;i>0;i--)
                {
                    double op = i*.1;

                    Platform.runLater(new Runnable() {
    
                        @Override
                        public void run() {
                            if(!Platform.isImplicitExit())
                            {
                                System.out.println("Has one more thread- Flashing 1");
                                return;
                            }
                            rect.setOpacity(op);
                        }
                        
                    });
                    Thread.sleep(100);
                    
                }
                Platform.runLater(new Runnable() {
    
                    @Override
                    public void run() {
                        if(!Platform.isImplicitExit())
                        {
                            System.out.println("Has one more thread- Flashing 2");
                            return;
                        }
                        rect.setFill(normal);  if(!Platform.isImplicitExit())
                        return;
    
                    }
                    
                });
                synchronized(isBeingAnimatedList)
                {
                    isBeingAnimatedList.remove(rect);

                }


                return null;
            }
        };
        synchronized(isBeingAnimatedList)
        {
            if(isBeingAnimatedList.contains(rect))
            {
                System.out.println("Returning as rectangle is still being animated - This should not occur as previously checked by caller");
                return;
            }
            isBeingAnimatedList.add(rect);
        }
        
        Thread th = new Thread(rectangleFlashTask);
        th.start();

        /* This hogs cpu
        FillTransition fillIndicatorTransition = new FillTransition(Duration.millis(1000), rect);
        
        fillIndicatorTransition.setFromValue(transitionColor);
        fillIndicatorTransition.setToValue(normal);
        fillIndicatorTransition.setCycleCount(1);
        fillIndicatorTransition.setAutoReverse(false);
            
        fillIndicatorTransition.play();
        
        */
    }

    public Text getPercentsign() {
        return percentsign;
    }

    
}
