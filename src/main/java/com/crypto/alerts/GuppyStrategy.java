package com.crypto.alerts;

import com.google.common.collect.Lists;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.Collections;
import java.util.List;

import static com.crypto.alerts.GuppyUtil.getSlowerEMAs;
import static com.crypto.alerts.GuppyUtil.getTrend;
import static com.crypto.alerts.GuppyUtil.getTrendFromInt;

/**
 * @author sumanth on 10/06/18
 */
public class GuppyStrategy {

    private TimeSeries timeSeries;
    private static ClosePriceIndicator closePriceIndicator;

    public GuppyStrategy(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
        this.closePriceIndicator = new ClosePriceIndicator(timeSeries);
    }

    public String evaluate() {
        int currentTrend = getTrend(timeSeries.getBarCount() - 1, closePriceIndicator);
        int previousTrend = getTrend(timeSeries.getBarCount() - 2, closePriceIndicator);

        String trendMsg;
        if (currentTrend == previousTrend) {
            trendMsg = getTrendFromInt(currentTrend);
        } else {
            trendMsg = getTrendFromInt(previousTrend) + " -> " + getTrendFromInt(currentTrend);
        }

        StringBuffer sb = new StringBuffer(trendMsg);
        List<Double> slowerEMAs = getSlowerEMAs(timeSeries.getBarCount() - 1, closePriceIndicator);
        double currentPrice = timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().doubleValue();
        double minEma = Collections.min(slowerEMAs);
        double maxEma = Collections.max(slowerEMAs);

        if (currentTrend != 0) {
            double slowEma = slowerEMAs.size() > 0 ? slowerEMAs.get(0) : -1;
            double slowestEma = slowerEMAs.size() == 6 ? slowerEMAs.get(5) : -1;
            sb.append(String.format(" | currentPrice = %.6f, slowEMA = %.6f (%.2f %%), slowestEMA = %.6f (%.2f %%)", currentPrice, slowEma,
                    relativePercent(slowEma, currentPrice), slowestEma, relativePercent(slowestEma, currentPrice)));
        } else {
            sb.append(String.format(" | currentPrice = %.6f, maxEma = %.6f (%.2f %%), minEma = %.6f (%.2f %%)", currentPrice, maxEma,
                    relativePercent(maxEma, currentPrice), minEma, relativePercent(minEma, currentPrice)));
        }

        if (isInRange(minEma, maxEma, currentPrice)) sb.append(" | INBETWEEN SLOWER GUPPIES");

        return sb.toString();
    }

    private double relativePercent(double value, double base) {
        return 100 * ((value / base) - 1);
    }

    private boolean isInRange(double lowerBound, double upperBound, double value) {
        return lowerBound <= value && value <= upperBound;
    }

}
