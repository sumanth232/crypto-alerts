package com.crypto.alerts;

import com.google.common.collect.Lists;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.List;

import static com.crypto.alerts.CommonUtil.strictlyDecreasing;
import static com.crypto.alerts.CommonUtil.strictlyIncreasing;

/**
 * @author sumanth on 29/07/18
 */
public class GuppyUtil {

    public static List<Integer> PERIODS = Lists.newArrayList(3, 5, 8, 10, 12, 15, 30, 35, 40, 45, 50, 60);

    public static int getTrend(int index, ClosePriceIndicator closePriceIndicator) {
        List<Double> slowerEMAs = getSlowerEMAs(index, closePriceIndicator);
        if (strictlyIncreasing(slowerEMAs)) return -1;
        else if (strictlyDecreasing(slowerEMAs)) return 1;
        else return 0;
    }

    public static List<Double> getSlowerEMAs(int index, ClosePriceIndicator closePriceIndicator) {
        List<Double> guppyValues = Lists.newArrayList();
        for (Integer period : PERIODS) {
            guppyValues.add(new EMAIndicator(closePriceIndicator, period).getValue(index).doubleValue());
        }

        return guppyValues.subList(PERIODS.size() / 2, PERIODS.size());
    }

    public static List<Double> getFasterEMAs(int index, ClosePriceIndicator closePriceIndicator) {
        List<Double> guppyValues = Lists.newArrayList();
        for (Integer period : PERIODS) {
            guppyValues.add(new EMAIndicator(closePriceIndicator, period).getValue(index).doubleValue());
        }

        return guppyValues.subList(0, PERIODS.size() / 2);
    }

    public static String getTrendFromInt(int i) {
        switch (i) {
            case -1:
                return "DOWN";
            case 0:
                return "COMPRESSING";
            case 1:
                return "UP";
            default:
                return "NA";
        }
    }
}
