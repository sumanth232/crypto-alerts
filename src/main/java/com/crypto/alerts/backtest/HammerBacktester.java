package com.crypto.alerts.backtest;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.crypto.alerts.CommonUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Decimal;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.crypto.alerts.CommonUtil.candlestickIntervalVsMinutes;
import static com.crypto.alerts.CommonUtil.getClient;

/**
 * @author sumanth on 22/08/18
 */
public class HammerBacktester {

    final static CandlestickInterval interval = CandlestickInterval.HALF_HOURLY;
    final static String symbol = "BTCUSDT";

    private static ClosePriceIndicator closePriceIndicator;
    private static EMAIndicator ema25;

    private static boolean inLongPosition = false;
    static Double entryPrice;
    static Double stopPrice;
    static Double takeProfitPrice;

    final static double intialPortfolioInBTC = 1;
    static double currentPortfolioInBTC = intialPortfolioInBTC;

    public static void main(String[] args) {
        backtestHammer();
    }

    // hammer at end of a downtrend
    public static void backtestHammer() {
        BinanceApiRestClient client = getClient();

        //List<Candlestick> candlestickBars = client.getCandlestickBars(symbol, interval);
        List<Candlestick> candlestickBars = CommonUtil.getCandlestickBarsUnlimited(symbol, interval, 16000, null, System.currentTimeMillis());
        List<Bar> bars = new ArrayList<>();
        for (Candlestick candle : candlestickBars) {
            ZonedDateTime endZonedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(candle.getCloseTime()), ZoneId.systemDefault());
            bars.add(new BaseBar(Duration.ofMinutes(candlestickIntervalVsMinutes.get(interval)), endZonedTime, Decimal.valueOf(candle.getOpen()), Decimal.valueOf(candle.getHigh()), Decimal.valueOf(candle.getLow()), Decimal.valueOf(candle.getClose()), Decimal.valueOf(candle.getVolume()), Decimal.valueOf(candle.getQuoteAssetVolume())));
        }

        BaseTimeSeries timeSeries = new BaseTimeSeries("binance_" + interval.toString() + "_" + symbol, bars);
        closePriceIndicator = new ClosePriceIndicator(timeSeries);
        ema25 = new EMAIndicator(closePriceIndicator, 25);

        int startCandle = 2;
        for (int i = startCandle; i < timeSeries.getBarCount(); i++) {
            Bar currentBar = timeSeries.getBar(i);
            if (!inLongPosition) {
                if(isDesiredHammerCandle(currentBar, timeSeries.getBar(i-1), timeSeries.getBar(i-2))) {
                    entryPrice = currentBar.getClosePrice().doubleValue();
                    stopPrice = currentBar.getMinPrice().minus(2).doubleValue();
                    takeProfitPrice = entryPrice + (entryPrice - stopPrice)*1.5;

                    if (true || takeProfitPrice < ema25.getValue(i).doubleValue()) {
                        System.out.print(currentBar.getEndTime() + " : ");
                        System.out.println(String.format("i = %d, Taken Long at %.2f with current BTC = %.2f, USD = %.2f", i, entryPrice, currentPortfolioInBTC, currentPortfolioInBTC * entryPrice));
                        inLongPosition = true;
                    }
                }
            } else {
                if (currentBar.getMinPrice().doubleValue() <= stopPrice) {
                    // stopped out
                    inLongPosition = false;
                    System.out.print(currentBar.getEndTime() + " : ");
                    currentPortfolioInBTC = 0.98 * currentPortfolioInBTC;
                    System.out.println(String.format("i = %d, Stopped at %.2f with current BTC = %.2f, USD = %.2f", i, stopPrice, currentPortfolioInBTC, currentPortfolioInBTC * stopPrice));

                } else if (currentBar.getMaxPrice().doubleValue() >= takeProfitPrice) {
                    // take profit, long closed
                    inLongPosition = false;
                    currentPortfolioInBTC = 1.03 * currentPortfolioInBTC;
                    System.out.print(currentBar.getEndTime() + " : ");
                    System.out.println(String.format("i = %d, Profit at %.2f with current BTC = %.2f, USD = %.2f", i, takeProfitPrice, currentPortfolioInBTC, currentPortfolioInBTC * takeProfitPrice));
                }
            }
        }
    }

    private static boolean isDesiredHammerCandle(Bar currentBar, Bar previousBar, Bar previousBar2) {
        if (!previousBar.isBearish() || !previousBar2.isBearish()) return false;
        if (!(currentBar.getVolume().isGreaterThan(previousBar.getVolume()))) return false;
        return isHammer(currentBar);
    }

    private static boolean isHammer(Bar candle) {
        Decimal open = candle.getOpenPrice();
        Decimal close = candle.getClosePrice();
        Decimal high = candle.getMaxPrice();
        Decimal low = candle.getMinPrice();

        if (!(close.isGreaterThan(low))) return false;

        Decimal tailLength = low.minus(open.min(close)).abs();
        Decimal bodyLength = open.minus(close).abs();

        if (!(tailLength.dividedBy(bodyLength).isGreaterThanOrEqual(2))) return false;

        Decimal upperTailLength = high.minus(open.max(close));
        assert upperTailLength.isGreaterThanOrEqual(0);

        if (upperTailLength.isGreaterThan(bodyLength.dividedBy(3))) return false;


        Decimal pushUpLength = close.minus(low);
        if (!(pushUpLength.isGreaterThanOrEqual(open.multipliedBy(0.0033)))) return false;

        return true;
    }

}
