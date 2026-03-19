package com.stockdata.project_data_provider.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.stockdata.project_data_provider.controller.StockDataOperation;
import com.stockdata.project_data_provider.dto.HistoryDto;
import com.stockdata.project_data_provider.dto.RealTimeDto;
import com.stockdata.project_data_provider.service.StockDataService;

@RestController
public class StockDataController implements StockDataOperation {
  @Autowired
  private StockDataService stockDataService;

  @Override
  public RealTimeDto getRealTime(String symbol) {
    return stockDataService.getRealTimeData(symbol);
  }

  @Override
  public HistoryDto getHistory(String symbol, int startDate, int endDate, String dataGranularity) {
    return stockDataService.getHistoricalData(symbol, startDate, endDate, dataGranularity);
  }
}
