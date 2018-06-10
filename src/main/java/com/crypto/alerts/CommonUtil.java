package com.crypto.alerts;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.general.SymbolStatus;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author sumanth on 10/06/18
 */
public class CommonUtil {

    // BTC vs List of symbols, USDT vs List of symbols
    private static Map<String, List<String>> allMarkets = Maps.newHashMap();

    private static BinanceApiRestClient client;

    static {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        client = factory.newRestClient();
        updateMarkets();
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
}
