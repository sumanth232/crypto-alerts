package com.crypto.alerts;

import com.google.common.collect.Lists;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * @author sumanth on 10/06/18
 */
public class GuppyStrategy {

    private static List<Integer> PERIODS = Lists.newArrayList(3, 5, 8, 10, 12, 15, 30, 35, 40, 45, 50, 60);
    private TimeSeries timeSeries;
    private ClosePriceIndicator closePrice;

    public GuppyStrategy(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
        this.closePrice = new ClosePriceIndicator(timeSeries);
    }

    public String evaluate() {
        int currentTrend = getTrend(timeSeries.getBarCount() - 1);
        int previousTrend = getTrend(timeSeries.getBarCount() - 2);

        StringBuffer sb = new StringBuffer(MessageFormat.format("previousTrend = {0}, currentTrend = {1}", previousTrend, currentTrend));
        List<Double> slowerEMAs = getSlowerEMAs(timeSeries.getBarCount() - 1);
        double currentPrice = timeSeries.getBar(timeSeries.getEndIndex()).getClosePrice().doubleValue();
        double minEma = Collections.min(slowerEMAs);
        double maxEma = Collections.max(slowerEMAs);

        if (currentPrice != 0) {
            double slowEma = slowerEMAs.size() > 0 ? slowerEMAs.get(0) : -1;
            double slowestEma = slowerEMAs.size() == 6 ? slowerEMAs.get(5) : -1;
            sb.append(String.format(" | currentPrice = %.2f, slowEMA = %.2f (%.2f %%), slowestEMA = %.2f (%.2f %%)", currentPrice, slowEma,
                    relativePercent(slowEma, currentPrice), slowestEma, relativePercent(slowestEma, currentPrice)));
        } else {
            sb.append(String.format(" | currentPrice = %.2f, maxEma = %.2f (%.2f age), minEma = %.2f (%.2f ages)", currentPrice, maxEma,
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

    private int getTrend(int index) {
        List<Double> slowerEMAs = getSlowerEMAs(index);
        if (strictlyIncreasing(slowerEMAs)) return 1;
        else if (strictlyDecreasing(slowerEMAs)) return -1;
        else return 0;
    }

    private List<Double> getSlowerEMAs(int index) {
        List<Double> guppyValues = Lists.newArrayList();
        for (Integer period : PERIODS) {
            guppyValues.add(new EMAIndicator(closePrice, period).getValue(index).doubleValue());
        }

        return guppyValues.subList(PERIODS.size() / 2, PERIODS.size());
    }

    private boolean strictlyIncreasing(List<Double> values) {
        if (values == null) return true;
        int size = values.size();
        if (size == 0 || size == 1) return true;
        for (int i = 1; i < size; i++)
            if (values.get(i - 1) > values.get(i)) return false;
        return true;
    }

    private boolean strictlyDecreasing(List<Double> values) {
        if (values == null) return true;
        int size = values.size();
        if (size == 0 || size == 1) return true;
        for (int i = 1; i < size; i++)
            if (values.get(i - 1) < values.get(i)) return false;
        return true;
    }
}
