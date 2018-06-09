package com.crypto.alerts;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.ta4j.core.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author sumanth on 08/06/18
 */
public class GuppyAlertTask implements Runnable {

    private static Map<CandlestickInterval, Integer> candlestickIntervalVsMinutes;

    private static Map<CandlestickInterval, List<String>> intervalVsSymbol;
    static {
        Map<CandlestickInterval, Integer> candlestickIntervalVsMinutesTemp = Maps.newHashMap();
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.ONE_MINUTE, 1);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.FIVE_MINUTES, 5);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.FIFTEEN_MINUTES, 15);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.HALF_HOURLY, 30);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.HOURLY, 60);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.TWO_HOURLY, 120);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.FOUR_HOURLY, 240);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.EIGHT_HOURLY, 480);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.TWELVE_HOURLY, 720);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.DAILY, 1440);
        candlestickIntervalVsMinutes = Collections.unmodifiableMap(candlestickIntervalVsMinutesTemp);



        Map<CandlestickInterval, List<String>> intervalVsSymbolTemp = Maps.newHashMap();
        intervalVsSymbolTemp.put(CandlestickInterval.ONE_MINUTE, Lists.newArrayList("BTCUSDT, BCCUSDT"));
        intervalVsSymbolTemp.put(CandlestickInterval.FIVE_MINUTES, Lists.newArrayList("BTCUSDT, BCCUSDT"));
        intervalVsSymbol = Collections.unmodifiableMap(intervalVsSymbolTemp);
    }

    /**
     * Key is the start/open time of the candle, and the value contains candlestick date.
     */
    private Map<Long, Candlestick> candlesticksCache;

    @Override
    public void run() {

    }

    private static BinanceApiRestClient getClient() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        return factory.newRestClient();
    }

    public static void main(String[] args) {
        String symbol = "BTCUSDT";
        CandlestickInterval interval = CandlestickInterval.FOUR_HOURLY;
        BinanceApiRestClient client = getClient();

        long currentTimeMs = System.currentTimeMillis();
//        int numPriods = 200;
//        List<Candlestick> candlestickBars = client.getCandlestickBars(symbol, CandlestickInterval.FOUR_HOURLY, numPriods, currentTimeMs - TimeUnit.HOURS.toMillis(numPriods*4), currentTimeMs);
        //List<Candlestick> candlestickBars ; //= client.getCandlestickBars(symbol, CandlestickInterval.FOUR_HOURLY);
        checkForAlerts(symbol, interval);



        /*List<Bar> bars = new ArrayList<>();
        for (Candlestick candle : candlestickBars) {
            ZonedDateTime endZonedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(candle.getCloseTime()), ZoneId.systemDefault());
            bars.add(new BaseBar(Duration.ofMinutes(candlestickIntervalVsMinutes.get(interval)), endZonedTime, Decimal.valueOf(candle.getOpen()), Decimal.valueOf(candle.getHigh()), Decimal.valueOf(candle.getLow()), Decimal.valueOf(candle.getClose()), Decimal.valueOf(candle.getVolume()), Decimal.valueOf(candle.getQuoteAssetVolume())));
        }

        BaseTimeSeries timeSeries = new BaseTimeSeries("binance_" + interval.toString() + "_" + symbol, bars);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator emaIndicator = new EMAIndicator(closePrice, 40);*/
        //Decimal val = emaIndicator.getValue(numPriods-2);

        int a = 2;
    }

    private void checkForAlerts() {
        for (Map.Entry<CandlestickInterval, List<String>> intervalListEntry : intervalVsSymbol.entrySet()) {
            for (String symbol : intervalListEntry.getValue()) {
                checkForAlerts(symbol, intervalListEntry.getKey());
            }
        }
    }

    /**
     * Initializes the candlestick cache by using the REST API.
     */
    private static void checkForAlerts(String symbol, CandlestickInterval interval) {
        BinanceApiRestClient client = getClient();

        long currentTimeMs = System.currentTimeMillis();
        List<Candlestick> candlestickBars = client.getCandlestickBars(symbol, CandlestickInterval.FOUR_HOURLY);
        List<Bar> bars = new ArrayList<>();
        for (Candlestick candle : candlestickBars) {
            ZonedDateTime endZonedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(candle.getCloseTime()), ZoneId.systemDefault());
            bars.add(new BaseBar(Duration.ofMinutes(candlestickIntervalVsMinutes.get(interval)), endZonedTime, Decimal.valueOf(candle.getOpen()), Decimal.valueOf(candle.getHigh()), Decimal.valueOf(candle.getLow()), Decimal.valueOf(candle.getClose()), Decimal.valueOf(candle.getVolume()), Decimal.valueOf(candle.getQuoteAssetVolume())));
        }

        BaseTimeSeries timeSeries = new BaseTimeSeries("binance_" + interval.toString() + "_" + symbol, bars);
        /*ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator emaIndicator = new EMAIndicator(closePrice, 50);

        this.candlesticksCache = new TreeMap<>();
        for (Candlestick candlestickBar : candlestickBars) {
            candlesticksCache.put(candlestickBar.getOpenTime(), candlestickBar);
        }*/
        GuppyStrategy guppyStrategy = new GuppyStrategy(timeSeries);
        guppyStrategy.evaluate();
    }
}
