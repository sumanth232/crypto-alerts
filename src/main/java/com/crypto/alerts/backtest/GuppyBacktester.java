package com.crypto.alerts.backtest;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.crypto.alerts.CommonUtil;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.crypto.alerts.CommonUtil.candlestickIntervalVsMinutes;
import static com.crypto.alerts.CommonUtil.getClient;
import static com.crypto.alerts.GuppyUtil.*;

/**
 * @author sumanth on 29/07/18
 */
public class GuppyBacktester {

    final static CandlestickInterval interval = CandlestickInterval.FOUR_HOURLY;
    final static String symbol = "BTCUSDT";

    private static ClosePriceIndicator closePriceIndicator;

    static int currentPosition = -1;  // -1 not yet started trading, 0 short, 1 long
    static Double entryPrice;

    static int longLeverage = 1;
    static int shortLeverage = 3;
    final static double intialPortfolioInBTC = 1;
    static double currentPortfolioInBTC = intialPortfolioInBTC;

    //private Integer previousCrossOverTrend; // -1 if faster EMAs are below slower EMAs, 1 if faster EMAs are above slower EMAs, 0 otherwise

    public static void main(String[] args) {
        backtest();
    }

    public static void backtest() {
        BinanceApiRestClient client = getClient();

        //List<Candlestick> candlestickBars = client.getCandlestickBars(symbol, interval);
        List<Candlestick> candlestickBars = CommonUtil.getCandlestickBarsUnlimited(symbol, interval, 2000, null, System.currentTimeMillis());
        List<Bar> bars = new ArrayList<>();
        for (Candlestick candle : candlestickBars) {
            ZonedDateTime endZonedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(candle.getCloseTime()), ZoneId.systemDefault());
            bars.add(new BaseBar(Duration.ofMinutes(candlestickIntervalVsMinutes.get(interval)), endZonedTime, Decimal.valueOf(candle.getOpen()), Decimal.valueOf(candle.getHigh()), Decimal.valueOf(candle.getLow()), Decimal.valueOf(candle.getClose()), Decimal.valueOf(candle.getVolume()), Decimal.valueOf(candle.getQuoteAssetVolume())));
        }

        BaseTimeSeries timeSeries = new BaseTimeSeries("binance_" + interval.toString() + "_" + symbol, bars);
        closePriceIndicator = new ClosePriceIndicator(timeSeries);

        int startCandle = 80;
        int previousCrossOverTrend = getCrossOverTrend(startCandle - 1, closePriceIndicator);
        for (int i = startCandle; i < timeSeries.getBarCount(); i++) {
            int currentCrossOverTrend = getCrossOverTrend(i, closePriceIndicator);

            if (currentPosition == -1) {
                if (currentCrossOverTrend != -1) {
                    entryPrice = timeSeries.getBar(i).getClosePrice().doubleValue();
                    System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                    System.out.println(String.format("i = %d, Taken Long at %.2f with current BTC = %.2f, USD = %.2f", i, entryPrice, currentPortfolioInBTC, currentPortfolioInBTC * entryPrice));
                    currentPosition = 1;

                } else {
                    entryPrice = timeSeries.getBar(i).getClosePrice().doubleValue();
                    System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                    System.out.println(String.format("i = %d, Taken Short at %.2f with current BTC = %.2f, USD = %.2f", i, entryPrice, currentPortfolioInBTC, currentPortfolioInBTC * entryPrice));
                    currentPosition = 0;
                }
            } else {
                if (currentCrossOverTrend != previousCrossOverTrend && currentCrossOverTrend != 0) {
                    // flip trade position
                    if (currentCrossOverTrend == 1) {
                        double closePrice = timeSeries.getBar(i).getClosePrice().doubleValue();
                        double PnL_BTC = -1 * (shortLeverage * currentPortfolioInBTC * entryPrice) * (1/entryPrice - 1/closePrice);
                        double closePortfolioInBTC = currentPortfolioInBTC + PnL_BTC;

                        double PnL_BTC_percent = PnL_BTC * 100 / currentPortfolioInBTC;
                        double PnL_USD_percent = ((closePortfolioInBTC * closePrice) - (currentPortfolioInBTC * entryPrice)) * 100 / (currentPortfolioInBTC * entryPrice);
                        System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                        System.out.println(String.format("Closed Short at %.2f with current BTC = %.2f, USD = %.2f, PnL_BTC = %.2f, PnL_BTC_Percent = %.2f %%, PnL_USD = %.2f %%", closePrice, closePortfolioInBTC, closePortfolioInBTC * closePrice, PnL_BTC, PnL_BTC_percent, PnL_USD_percent));

                        currentPortfolioInBTC = closePortfolioInBTC;
                        System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                        System.out.println(String.format("Taken Long at %.2f with current BTC = %.2f, USD = %.2f", closePrice, currentPortfolioInBTC, currentPortfolioInBTC * closePrice));
                        entryPrice = closePrice;
                        currentPosition = 1;
                    } else {
                        double closePrice = timeSeries.getBar(i).getClosePrice().doubleValue();
                        double PnL_BTC = +1 * (longLeverage * currentPortfolioInBTC * entryPrice) * (1/entryPrice - 1/closePrice);
                        double closePortfolioInBTC = currentPortfolioInBTC + PnL_BTC;

                        double PnL_BTC_percent = PnL_BTC * 100 / currentPortfolioInBTC;
                        double PnL_USD_percent = ((closePortfolioInBTC * closePrice) - (currentPortfolioInBTC * entryPrice)) * 100 / (currentPortfolioInBTC * entryPrice);
                        System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                        System.out.println(String.format("Closed Long at %.2f with current BTC = %.2f, USD = %.2f, PnL_BTC = %.2f, PnL_BTC_Percent = %.2f %%, PnL_USD = %.2f %%", closePrice, closePortfolioInBTC, closePortfolioInBTC * closePrice, PnL_BTC, PnL_BTC_percent, PnL_USD_percent));

                        currentPortfolioInBTC = closePortfolioInBTC;
                        System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                        System.out.println(String.format("Taken Short at %.2f with current BTC = %.2f, USD = %.2f", closePrice, currentPortfolioInBTC, currentPortfolioInBTC * closePrice));
                        entryPrice = closePrice;
                        currentPosition = 0;
                    }
                }

                if (i == timeSeries.getBarCount() - 1) {
                    double closePrice = timeSeries.getBar(i).getClosePrice().doubleValue();
                    double PnL_BTC = 1 * (longLeverage * currentPortfolioInBTC * entryPrice) * (1/entryPrice - 1/closePrice);
                    double closePortfolioInBTC = currentPortfolioInBTC + PnL_BTC;

                    double PnL_BTC_percent = PnL_BTC * 100 / currentPortfolioInBTC;
                    double PnL_USD_percent = ((closePortfolioInBTC * closePrice) - (currentPortfolioInBTC * entryPrice)) * 100 / (currentPortfolioInBTC * entryPrice);
                    System.out.print(timeSeries.getBar(i).getEndTime() + " : ");
                    System.out.println(String.format("Finally Closed Long at %.2f with current BTC = %.2f, USD = %.2f, PnL_BTC = %.2f, PnL_BTC_Percent = %.2f %%, PnL_USD = %.2f %%", closePrice, closePortfolioInBTC, closePortfolioInBTC * closePrice, PnL_BTC, PnL_BTC_percent, PnL_USD_percent));
                }

            }

            if (currentCrossOverTrend != 0) previousCrossOverTrend = currentCrossOverTrend;
        }

    }

    private static int getCrossOverTrend(int index, ClosePriceIndicator closePriceIndicator) {
        Double maxSlowerEMAs = Collections.max(getSlowerEMAs(index, closePriceIndicator));
        Double minSlowerEMAs = Collections.min(getSlowerEMAs(index, closePriceIndicator));

        Double minFasterEMAs = Collections.min(getFasterEMAs(index, closePriceIndicator));
        Double maxFasterEMAs = Collections.max(getFasterEMAs(index, closePriceIndicator));

        if (minFasterEMAs > maxSlowerEMAs) return 1;
        else if(maxFasterEMAs < minSlowerEMAs) return -1;
        else return 0;
    }

}
