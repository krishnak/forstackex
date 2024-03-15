package krishna.core.dataobject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import krishna.core.FasterFormatter;
import krishna.core.ui.ScripTileView;

public class ScripTileObjectController  {
    protected ScripTileView scripTileView;
    protected SimpleStringProperty ScripName;
    
    protected Long ScripTradingCode;
    protected long totalVolume;
    protected boolean isUnderLying = true;
    protected SimpleDoubleProperty lastTradedPrice = new SimpleDoubleProperty(0); // level 3 data

    protected SimpleDoubleProperty high = new SimpleDoubleProperty(0);
    protected SimpleDoubleProperty low = new SimpleDoubleProperty(0);
    protected SimpleDoubleProperty open = new SimpleDoubleProperty(0);
    protected SimpleDoubleProperty close = new SimpleDoubleProperty(0);

    protected SimpleDoubleProperty averageAskPrice = new SimpleDoubleProperty(0);
  
    protected SimpleDoubleProperty averageBidPrice = new SimpleDoubleProperty(0);
    protected SimpleLongProperty askQuantity = new SimpleLongProperty(0); // level 3 data
    protected SimpleStringProperty askQuantityAsString = new SimpleStringProperty();
    protected SimpleLongProperty bidQuantity = new SimpleLongProperty(-1); // level 3 data
    protected SimpleStringProperty bidQuantityAsString = new SimpleStringProperty();

    protected SimpleDoubleProperty VWAP = new SimpleDoubleProperty(0);


    protected SimpleDoubleProperty lowestBid = new SimpleDoubleProperty(0);
    protected SimpleDoubleProperty highestAsk = new SimpleDoubleProperty(0);
    protected long lastTradedTime;
    
  //  protected SimpleLongProperty volumeTradedIntheday = new SimpleLongProperty();

    // the UI has no space to put these yet
    private SimpleIntegerProperty fakeAskQty = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty fakeBidQty = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty lastTradedQty = new SimpleIntegerProperty(0);

        


    protected Date ltt =new Date();
    protected SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    protected FasterFormatter fasterFormatter = new FasterFormatter();
    public NumberFormat nformat;

    protected float targetPercentage = 7f;    
    public ScripTileObjectController(String scripName, Long scripTradingCode) {
        ScripName = new SimpleStringProperty(scripName);
        scripTileView = new ScripTileView();
        scripTileView.getScripName().setText(scripName);
        ScripTradingCode = scripTradingCode; //this is not used in the UI
        
        nformat= NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT); 

        scripTileView.enableIndicator(true);

        open.addListener(new FlashBackgroundListener(0));
        low.addListener(new FlashBackgroundListener(2));
        
        high.addListener(new FlashBackgroundListener(1));
        
       
        VWAP.addListener(new FlashBackgroundListener(3));
        
       

       
    }
    public ScripTileView getScripTileView() {
        return scripTileView;
    }
    public void setScripTileView(ScripTileView tc) {
        this.scripTileView = tc;
    }
    public void setScripName(String scripName) {
        ScripName.set(scripName);
    }
    public Long getScripTradingCode() {
        return ScripTradingCode;
    }
    public void setScripTradingCode(Long scripTradingCode) {
        ScripTradingCode = scripTradingCode;
    }
    public boolean isUnderLying() {
        return isUnderLying;
    }
    public void setUnderLying(boolean isUnderLying) {
        this.isUnderLying = isUnderLying;
    }
    public SimpleDoubleProperty getLastTradedPriceProperty()
    {
        return lastTradedPrice;
    }
    public double getLastTradedPrice()
    {
        return this.lastTradedPrice.doubleValue();
    }
    public void priceAction(double change)
    {
        if(change <0)
        {
            //buy
        }
        else
        {
            //sell if already bought
        }

    }
    public  void setLastTradedPrice(double ltp) {
        {

            lastTradedPrice.set(ltp);
            final double change = (ltp-VWAP.get())*100/VWAP.get();
            if(change < targetPercentage*-1 || change > targetPercentage)
            {
                priceAction(change);
            }

            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    if(!Platform.isImplicitExit())
                    {
                        System.out.println("Has one more thread");
                        return;
                    }
                        

                    scripTileView.getStockprice().setText(fasterFormatter.formatDouble(ltp, 2, true));
                    if(change >0)
                    {
                        scripTileView.getChange().setFill(Color.valueOf("#8fc75e"));
                        scripTileView.getPercentsign().setFill(Color.valueOf("#8fc75e"));
                    }
                    else
                    {
                        scripTileView.getChange().setFill(Color.RED);
                        scripTileView.getPercentsign().setFill(Color.RED);
                    }
                        
        
                }
                
            });

        }
        
    }
    
    public double getHigh() {
        return high.doubleValue();
    }
    public double getLow() {
        return low.doubleValue();
    }
    public double getOpen() {
        return open.doubleValue();
    }
    public double getClose() {
        return close.doubleValue();
    }
    public double getAverageAskPrice() {
        return averageAskPrice.doubleValue();
    }
    public double getAverageBidPrice() {
        return averageBidPrice.doubleValue();
    }
    public int getAskQuantity() {
        return askQuantity.intValue();
    }
    public int getBidQuantity() {
        return bidQuantity.intValue();
    }
    public double getVWAP() {
        return VWAP.doubleValue();
    }

    public synchronized void setAll(final double vWAP, final long ltt, final long vol, final long ltq, final double open,final double high,final double low, final double prevclose)
    {
        VWAP.set(vWAP);
        this.open.set(open);
        this.high.set(high);
        this.low.set(low);
        this.close.set(prevclose);
        final double change = (lastTradedPrice.get()-VWAP.get())*100/VWAP.get();

        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if(!Platform.isImplicitExit())
                    return;

                //this.lastTradedTimeAsString.set(sdf.format(ltt));
                scripTileView.getLTT().setText(sdf.format(ltt*1000));
                scripTileView.getVWAP().setText(fasterFormatter.formatDouble(vWAP, 2, false));
                scripTileView.getVolume().setText(fasterFormatter.formatLong(vol));

                scripTileView.getOpen().setText(fasterFormatter.formatDouble(open, 2, false));
                scripTileView.getHigh().setText(fasterFormatter.formatDouble(high, 2, false));

                scripTileView.getLow().setText(fasterFormatter.formatDouble(low, 2, false));

                scripTileView.getClose().setText(fasterFormatter.formatDouble(prevclose, 2, false));

                scripTileView.getChange().setText(fasterFormatter.formatDouble(change, 2, false));     
                if(change >0)
                {
                    scripTileView.getChange().setFill(Color.valueOf("#8fc75e"));
                    scripTileView.getPercentsign().setFill(Color.valueOf("#8fc75e"));
                }
                else
                {
                    scripTileView.getChange().setFill(Color.RED);
                    scripTileView.getPercentsign().setFill(Color.RED);
                }
                    
    

            }
            
        });
        
    }

    public synchronized void setLevel3_Supplement_Data(double ltp,double averageAskPrice,long askQuantity,double highask,double averageBidPrice,long bidQuantity,double lowbid)
    {
        this.averageAskPrice.set(averageAskPrice);
        this.askQuantity.set(askQuantity);
        this.highestAsk.set(highask);
        this.averageBidPrice.set(averageBidPrice);
        this.bidQuantity.set(bidQuantity);
        this.lowestBid.set(lowbid);

        this.lastTradedPrice.set(ltp);
        final double change = (ltp-VWAP.get())*100/VWAP.get();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            if(!Platform.isImplicitExit())
                return;

            scripTileView.getStockprice().setText(fasterFormatter.formatDouble(ltp, 2, true));

            scripTileView.getAverageAsk().setText(fasterFormatter.formatDouble(averageAskPrice, 2, false));
            scripTileView.getAskQuantity().setText(nformat.format(askQuantity));
            scripTileView.getHighestAsk().setText(fasterFormatter.formatDouble(highask, 2, false));
            scripTileView.getAverageBid().setText(fasterFormatter.formatDouble(averageBidPrice, 2, false));
            scripTileView.getBidQuantity().setText(nformat.format(bidQuantity));
            scripTileView.getLowestBid().setText(fasterFormatter.formatDouble(lowbid, 2, false));
            scripTileView.getChange().setText(fasterFormatter.formatDouble(change, 2, false));     
            if(change >0)
            {
                scripTileView.getChange().setFill(Color.valueOf("#8fc75e"));
                scripTileView.getPercentsign().setFill(Color.valueOf("#8fc75e"));
            }
            else
            {
                scripTileView.getChange().setFill(Color.RED);
                scripTileView.getPercentsign().setFill(Color.RED);
            }
                

            }
        });
    
    }
    public long getLastTradedTime() {
        return lastTradedTime;
    }
    
    public int getFakeAskQty() {
        return fakeAskQty.intValue();
    }
    public void setFakeAskQty(int fakeAskQty) {
        this.fakeAskQty.set(fakeAskQty);;
    }
    public int getFakeBidQty() {
        return fakeBidQty.intValue();
    }
    public void setFakeBidQty(int fakeBidQty) {
        this.fakeBidQty.set(fakeBidQty);;
    }
    public int getLastTradedQty() {
        return lastTradedQty.intValue();
    }
    
    public double getLowestBid()
    {
        return lowestBid.get();
    }
    public double getHighestAsk()
    {
        return highestAsk.get();
    }
    
    public SimpleStringProperty getScripName() {
        return ScripName;
    }
    
    
    
  
    private class FlashBackgroundListener implements ChangeListener<Number>
    {
        private int whichRectangle;
        FlashBackgroundListener(int rectangle)
        {
            this.whichRectangle = rectangle;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                Number newValue) {
                    if(oldValue.doubleValue()!=newValue.doubleValue())
                    {
                        Color startColor = Color.YELLOWGREEN;
                        if(oldValue.doubleValue()>newValue.doubleValue())
                            startColor = Color.RED;
                        switch (whichRectangle) {
                            case 0:
                                if(!scripTileView.isBeingAnimated(scripTileView.openbackground))
                                    scripTileView.runAnimationonRectangle(startColor,scripTileView.openbackground);

                                break;
                            
                            case 1:
                                if(!scripTileView.isBeingAnimated(scripTileView.highbackground))                                
                                    scripTileView.runAnimationonRectangle(startColor,scripTileView.highbackground);

                                break;

                            case 2:
                                if(!scripTileView.isBeingAnimated(scripTileView.lowbackground))                                
                                    scripTileView.runAnimationonRectangle(startColor,scripTileView.lowbackground);

                            break;
                            
                            case 3:
                                if(!scripTileView.isBeingAnimated(scripTileView.vwapbackground))
                                    scripTileView.runAnimationonRectangle(startColor,scripTileView.vwapbackground);
                              
                                break;
                        
                            default:
                                break;
                            
                        }
                    }           
                }

        
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
            return false;

        if(obj.getClass()!=this.getClass())
            return false;
        final ScripTileObjectController compareobj = (ScripTileObjectController)obj;
        if(this.ScripTradingCode==compareobj.ScripTradingCode)
            return true;
        
        return false;
    }
    


}
