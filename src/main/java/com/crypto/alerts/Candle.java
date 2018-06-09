package com.crypto.alerts;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author sumanth on 09/06/18
 */
@Entity
public class Candle {

    // https://sammchardy.github.io/binance/2018/01/08/historical-data-download-binance.html

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    // ETHBTC, base asset is ETH, quote asset is BTC
    private String baseAsset;
    private String quoteAsset;

    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Long closeTime;
    private String quoteAssetVolume;
    private Long numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;
}
