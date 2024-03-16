package krishna.core.dataobject;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Callback;
import krishna.core.NewTraderWithNewUI.ButtonPressEventHandler;
import krishna.core.indicators.VolatalityPredictor;
import krishna.core.ui.OptionOrderTileView;
import krishna.core.ui.SimulatorPopupView;

public class OptionTileObjectController extends ScripTileObjectController {

    private ScripTileObjectController underLying;
    public static  enum optionType {CALL,PUT}; // this is used for order placement?
    private long lotsize;
    
    private optionType callRput;

    private VolatalityPredictor vp;

    private double strikePrice;
    private final double volatility = 0.5;
   


    
    private DoubleBinding volatilityProperty = Bindings.createDoubleBinding(() -> 0.5);
    private double daysHVP = 0;
    private double daysLVP = 0;
    private DoubleBinding daysHighVolatilityProperty = Bindings.createDoubleBinding(() -> 0.0);
    private DoubleBinding dayslowVolatilityProperty = Bindings.createDoubleBinding(() -> 0.0);
    



    private OptionOrderTileView orderTileView;
    

    private Stage priceSimulatorStage ;

    private Stage volatilityTileStage ;

    private SimpleStringProperty marginAmount = new SimpleStringProperty("Margin Not Calculated");

    public OptionTileObjectController(String scripName, Long scripTradingCode,
                                        ScripTileObjectController underlyingScrip, double strikePrice,
                                        optionType type,Long lotsize,double numDaysForExpiry,double riskFreeInterestRate,
                                        ButtonPressEventHandler buttonPressed,BooleanProperty loggedinStringProperty) 
    {
      
        super(scripName, scripTradingCode);// as options get level 2 data for price
        this.strikePrice = strikePrice;

        vp = new VolatalityPredictor(numDaysForExpiry, riskFreeInterestRate);
        

        scripTileView.enableIndicator(false);
        scripTileView.setInvisibleInOptionTile();
        this.lotsize = lotsize;
        isUnderLying = false;
        callRput = type;
        this.underLying = underlyingScrip;

        underLying.lastTradedPrice.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {

                recalculatePredictedPrices();
            }
            
        });
        orderTileView = new OptionOrderTileView();
    
        loggedinStringProperty.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue)
                {
                    long margin = buttonPressed.getMarginAmountToShort();

                    float roi = lastTradedPrice.floatValue()*OptionTileObjectController.this.getOrderQuantity()*100/margin;
                    marginAmount.set( "₹ "+String.format("%,d",margin)+" ("+fasterFormatter.formatDouble(roi,2,false)+"%)");
                    
                }
            }
            
        });
        
        //this is the only binding that is needed 
        volatilityProperty = new DoubleBinding() {
            {
                super.bind(underlyingScrip.lastTradedPrice,lastTradedPrice);
            }
            @Override
            protected double computeValue()
            {
                if(underlyingScrip.lastTradedPrice.get()==0 || lastTradedPrice.get()==0)
                    return volatility;
                //long st = System.nanoTime();
                double myvalue = vp.Use_ImpliedVol_NewtonRaphsonSmile(underlyingScrip.lastTradedPrice.get(), strikePrice, volatility, lastTradedPrice.get(), callRput);
            //    System.out.println("Returning volatility as "+myvalue);
                //long et = System.nanoTime();
                //System.out.println("Volatility Computation "+(et-st));
                if(myvalue>daysHVP)
                {

                    daysHVP = myvalue;    
            
                }
               if(myvalue<daysLVP|| daysLVP==0)
               {
                    daysLVP = myvalue;
               }
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        if(!Platform.isImplicitExit())
                             return;

                        orderTileView.getVolatility().setText(fasterFormatter.formatDouble(myvalue*100, 2, false));
                        orderTileView.getVolatilityhigh().setText(fasterFormatter.formatDouble(daysHVP*100, 2, false));
                        orderTileView.getVolatilitylow().setText(fasterFormatter.formatDouble(daysLVP*100, 2, false));

                    }
                    
                });
                return myvalue;
            }
        };

        


        buttonPressed.setCaller(this);

        orderTileView.getBuybutton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                buttonPressed.actOnButtonPress(OptionOrder.transactionType.BUY);
            }
            
        });
        orderTileView.getSellbutton().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                buttonPressed.actOnButtonPress(OptionOrder.transactionType.SELL);            }
            
        });
        


      
   
      
        orderTileView.getQtyBox().valueProperty().addListener(new ChangeListener<Integer>() {

            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if(loggedinStringProperty.get())
                {
                    long margin = buttonPressed.getMarginAmountToShort();
                    float roi = lastTradedPrice.floatValue()*OptionTileObjectController.this.getOrderQuantity()*100/margin;
                    marginAmount.set( fasterFormatter.formatLong(margin)+" ("+fasterFormatter.formatDouble(roi,2,false)+"%)");
                }
                    
            }   
        });


      
     
        orderTileView.getMarginRequiredText().textProperty().bind(marginAmount);
        

    }

  
    public OptionOrderTileView getOptionOrderTileView()
    {
        return orderTileView;
    }
    public synchronized void setAll(double vWAP, long ltt, long vol, long ltq, double open, double high,double low, double prevclose)
    {
        super.setAll(vWAP, ltt, vol/lotsize, ltq, open, high, low, prevclose);
        recalculatePredictedPrices();
        
    }

    public void recalculatePredictedPrices()
    {
        double vwq =vp.computeOptionPrice(volatilityProperty.get(), underLying.VWAP.get(), strikePrice,callRput)[0]; 

        double abidq = vp.computeOptionPrice(volatilityProperty.get(), underLying.averageBidPrice.get(), strikePrice,callRput)[0];
        double lbidq = vp.computeOptionPrice(volatilityProperty.get(), underLying.lowestBid.get(), strikePrice,callRput)[0];

        double aaskq = vp.computeOptionPrice(volatilityProperty.get(), underLying.averageAskPrice.get(), strikePrice,callRput)[0];

        double haskq = vp.computeOptionPrice(volatilityProperty.get(), underLying.highestAsk.get(), strikePrice,callRput)[0];
        double resultarray[] = new double[2];
        if(underLying.getLastTradedPrice()==0 || volatilityProperty.get()>1 )
        {
        }
        else
            resultarray =vp.computeOptionPrice(volatilityProperty.get(), underLying.getLastTradedPrice(), strikePrice,callRput);
       
        final double delta = resultarray[1];
        final double predictedPrice = resultarray[0];
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
            if(!Platform.isImplicitExit())
                return;

            orderTileView.getDelta().setText(fasterFormatter.formatDouble(delta, 2, false));
            orderTileView.getLtpq().setText(fasterFormatter.formatDouble(predictedPrice, 2, false));
            //orderTileView.getLtpq().setText(Platform.isImplicitExit()+"");
         
            orderTileView.getOptionprice().setText(fasterFormatter.formatDouble(predictedPrice, 2, false));
            
            orderTileView.getVwapq().setText(fasterFormatter.formatDouble(vwq, 2, false));

            orderTileView.getAbidq().setText(fasterFormatter.formatDouble(abidq, 2, false));
            orderTileView.getLbidq().setText(fasterFormatter.formatDouble(lbidq, 2, false));

            orderTileView.getHaskq().setText(fasterFormatter.formatDouble(haskq, 2, false));
            orderTileView.getAaskq().setText(fasterFormatter.formatDouble(aaskq, 2, false));

            }
            
        });

    }

   
    public ScripTileObjectController getUnderlying()
    {
        return this.underLying;
    }
    public String getStrikePriceAsString()
    {
        return (long)strikePrice+"";
    }

    public optionType getOptionType()
    {
        return callRput;
    }
    public long getLotSize()
    {
        return this.lotsize;
    }
    public long getOrderQuantity()
    {
        return orderTileView.getQuantity()*lotsize;
    }
    public String getOptionOrderPrice()
    {
        return orderTileView.getOptionprice().textProperty().get();
    }
   
   private float roundToNearestPrice(double price)
   {
        return (float)(0.05*(Math.round(price/.05)));

   }
   
}
