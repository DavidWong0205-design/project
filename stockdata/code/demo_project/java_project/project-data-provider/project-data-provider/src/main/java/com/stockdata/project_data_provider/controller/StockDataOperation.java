package com.stockdata.project_data_provider.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.stockdata.project_data_provider.dto.HistoryDto;
import com.stockdata.project_data_provider.dto.RealTimeDto;

public interface StockDataOperation {
// 即時行情查詢
@GetMapping(value = "/stock/realtime")
RealTimeDto getRealTime(@RequestParam String symbol);

// 歷史股價查詢
@GetMapping(value = "/stock/history")
HistoryDto getHistory(@RequestParam String symbol,
@RequestParam int startDate, @RequestParam int endDate, @RequestParam String dataGranularity);
}


