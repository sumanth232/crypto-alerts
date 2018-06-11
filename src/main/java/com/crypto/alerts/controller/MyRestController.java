package com.crypto.alerts.controller;

import com.crypto.alerts.CommonUtil;
import com.crypto.alerts.GuppyAlertTask;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author sumanth on 10/06/18
 */
@Controller
public class MyRestController {

    @RequestMapping("/")
    public String welcome(Map<String, Object> model) {
        String msg = "REST end points :<br>" +
                "/guppy/BTCUSDT/4h (available 1m, 3m, 5m, 15m, 30m, 1h, 2h, 4h, 8h, 12h, 1d, 3d, 1w, 1M)<br>" +
                "/quoteAssets<br>" +
                "/guppy/quote/USDT/4h<br>" +
                "----------------------------<br>" +
                "by sumanth232@gmail.com";
        model.put("result", msg);
        return "index";
    }

    @RequestMapping("/guppy/{symbol}/{tf}")
    public String home(Map<String, Object> model, @PathVariable("symbol") String symbol, @PathVariable("tf") String tf) {
        String result = GuppyAlertTask.checkForAlerts(symbol, GuppyAlertTask.getCandleStickInterval(tf));
        model.put("result", result);
        return "index";
    }

    @RequestMapping("/quoteAssets")
    public String getAllQuoteAssets(Map<String, Object> model) {
        model.put("result", CommonUtil.getAllQuoteAssets());
        return "index";
    }

    @RequestMapping("/guppy/quote/{quoteAsset}/{tf}")
    public String getGuppyAnalysis(Map<String, Object> model, @PathVariable("quoteAsset") String quoteAsset, @PathVariable("tf") String tf) {
        List<String> symbols = CommonUtil.getQuoteMarkets(quoteAsset);
        StringBuffer sb = new StringBuffer();
        for (String symbol : symbols) {
            sb.append("<br>" + GuppyAlertTask.checkForAlerts(symbol, GuppyAlertTask.getCandleStickInterval(tf)));
        }
        model.put("result", sb.toString());
        return "index";
    }


}
