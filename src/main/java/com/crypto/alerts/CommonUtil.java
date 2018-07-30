package com.crypto.alerts;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.general.SymbolStatus;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author sumanth on 10/06/18
 *
 * https://github.com/binance-exchange/binance-java-api
 * https://github.com/binance-exchange/binance-java-api/blob/master/src/main/java/com/binance/api/client/BinanceApiRestClient.java
 */
public class CommonUtil {

    // BTC vs List of symbols, USDT vs List of symbols
    private static Map<String, List<String>> allMarkets = Maps.newHashMap();

    private static BinanceApiRestClient client;

    public static Map<CandlestickInterval, Integer> candlestickIntervalVsMinutes;

    static {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        client = factory.newRestClient();

        Map<CandlestickInterval, Integer> candlestickIntervalVsMinutesTemp = Maps.newHashMap();
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.ONE_MINUTE, 1);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.FIVE_MINUTES, 5);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.FIFTEEN_MINUTES, 15);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.HALF_HOURLY, 30);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.HOURLY, 60);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.TWO_HOURLY, 120);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.FOUR_HOURLY, 240);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.SIX_HOURLY, 360);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.EIGHT_HOURLY, 480);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.TWELVE_HOURLY, 720);
        candlestickIntervalVsMinutesTemp.put(CandlestickInterval.DAILY, 1440);
        candlestickIntervalVsMinutes = Collections.unmodifiableMap(candlestickIntervalVsMinutesTemp);

        //updateMarkets();
    }

    public static BinanceApiRestClient getClient() {
        return client;
    }

    public static void updateMarkets() {
        ExchangeInfo exchangeInfo = client.getExchangeInfo();
        List<SymbolInfo> symbols = exchangeInfo.getSymbols();
        for (SymbolInfo symbol : symbols) {
            if (symbol.getStatus() == SymbolStatus.TRADING && !symbol.getBaseAsset().toString().equals("TUSD")) {
                allMarkets.computeIfAbsent(symbol.getQuoteAsset(), x -> Lists.newArrayList());
                allMarkets.get(symbol.getQuoteAsset()).add(symbol.getSymbol().toString());
            }
        }
    }

    public static List<String> getAllQuoteAssets() {
        return Lists.newArrayList(allMarkets.keySet());
    }

    public static List<String> getQuoteMarkets(String quoteAsset) {
        return allMarkets.get(quoteAsset);
    }

    public static boolean strictlyIncreasing(List<Double> values) {
        if (values == null) return true;
        int size = values.size();
        if (size == 0 || size == 1) return true;
        for (int i = 1; i < size; i++)
            if (values.get(i - 1) > values.get(i)) return false;
        return true;
    }

    public static boolean strictlyDecreasing(List<Double> values) {
        if (values == null) return true;
        int size = values.size();
        if (size == 0 || size == 1) return true;
        for (int i = 1; i < size; i++)
            if (values.get(i - 1) < values.get(i)) return false;
        return true;
    }

    // candle results will start coming from the startMs time (if present)
    public static List<Candlestick> getCandlestickBarsUnlimited(String symbol, CandlestickInterval interval, Integer count, Long startMs, Long endMs) {
        int batchSize = 1000;
        List<Candlestick> result = Lists.newArrayList();
        if (startMs == null && endMs == null) endMs = System.currentTimeMillis();
        List<Candlestick> partialBars;
        while (count > 0 && (startMs == null || endMs == null || startMs <= endMs)) {
            if (count <= batchSize) {
                partialBars = client.getCandlestickBars(symbol, interval, count, startMs, endMs);
                count -= batchSize;
            } else {
                partialBars = client.getCandlestickBars(symbol, interval, batchSize, startMs, endMs);
                if (startMs != null) startMs = partialBars.get(partialBars.size() - 1).getCloseTime() + 1;
                else endMs = partialBars.get(0).getOpenTime() - 1;
                assert (partialBars.size() == batchSize);
                count -= batchSize;
            }
            if (startMs != null) result.addAll(partialBars);
            else result.addAll(0, partialBars);
        }
        return result;
    }
}
