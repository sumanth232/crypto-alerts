package com.crypto.alerts;

import com.google.common.collect.Lists;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.text.MessageFormat;
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

    public void evaluate() {
        int currentTrend = getTrend(timeSeries.getBarCount() - 1);
        int previousTrend = getTrend(timeSeries.getBarCount() - 2);

        System.out.println("currentTrend = " + currentTrend);
        if (currentTrend != previousTrend) {
            System.out.println(MessageFormat.format("Trend changed from {0} to {1}", previousTrend, currentTrend));
        }
    }

    private int getTrend(int index) {
        List<Double> guppyValues = Lists.newArrayList();
        for (Integer period : PERIODS) {
            guppyValues.add(new EMAIndicator(closePrice, period).getValue(index).doubleValue());
        }

        List<Double> slowerEMAs = guppyValues.subList(PERIODS.size() / 2, PERIODS.size());
        if (strictlyIncreasing(slowerEMAs)) return 1;
        else if (strictlyDecreasing(slowerEMAs)) return -1;
        else return 0;
    }

    private boolean strictlyIncreasing(List<Double> values) {
        if (values == null) return true;
        int size = values.size();
        if (size == 0 || size == 1) return true;
        for (int i = 1; i < size; i++)
            if (values.get(i-1) > values.get(i)) return false;
        return true;
    }

    private boolean strictlyDecreasing(List<Double> values) {
        if (values == null) return true;
        int size = values.size();
        if (size == 0 || size == 1) return true;
        for (int i = 1; i < size; i++)
            if (values.get(i-1) < values.get(i)) return false;
        return true;
    }
}
