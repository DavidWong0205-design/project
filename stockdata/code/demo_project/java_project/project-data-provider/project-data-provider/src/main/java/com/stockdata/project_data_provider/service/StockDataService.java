package com.stockdata.project_data_provider.service;

import com.stockdata.project_data_provider.dto.HistoryDto;
import com.stockdata.project_data_provider.dto.RealTimeDto;

public interface StockDataService {

  HistoryDto getHistoricalData(String symbol, int startDate, int endDate, String dataGranularity);

  RealTimeDto getRealTimeData(String symbol);
}
