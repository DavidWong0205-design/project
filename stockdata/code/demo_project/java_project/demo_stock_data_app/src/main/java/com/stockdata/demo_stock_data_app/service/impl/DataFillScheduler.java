package com.stockdata.demo_stock_data_app.service.impl;

import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.repository.SPListRepository;
import com.stockdata.demo_stock_data_app.service.StockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataFillScheduler {
    private static final Logger log = LoggerFactory.getLogger(DataFillScheduler.class);

    @Autowired
    private SPListRepository spListRepository;

    @Autowired
    private StockDataService stockDataService;

    // 每天凌晨 03:00 自動補齊重要粒度（5m/15m/30m/90m/1h）
    @Scheduled(cron = "0 0 3 * * *")
    public void nightlyBatchFill() {
        log.info("Starting nightly batch data fill");
        List<SPListEntity> list = spListRepository.findAll();
        if (list == null || list.isEmpty()) {
            log.info("No symbols found for batch fill");
            return;
        }
        String[] granularities = new String[]{"5m", "15m", "30m", "90m", "1h"};
        for (SPListEntity s : list) {
            String symbol = s.getSymbol();
            for (String g : granularities) {
                try {
                    log.info("Filling symbol={} granularity={}", symbol, g);
                    stockDataService.autoFillAllHistory(symbol, g);
                } catch (Exception ex) {
                    log.error("Failed filling {} {}: {}", symbol, g, ex.getMessage());
                }
            }
        }
        log.info("Nightly batch data fill completed");
    }
}
