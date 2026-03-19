package com.stockdata.demo_stock_data_app.controller;

import com.stockdata.demo_stock_data_app.dto.HeatmapStockDto;
import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.model.dto.StockChartDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface StockDataOperation {

  @GetMapping("/stock/heatmap")
  List<HeatmapStockDto> heatmap();

  @GetMapping("/stock/realtime")
  RealTimeStockDTO getRealtimeData(@RequestParam String symbol);

  @GetMapping("/stock/company-info")
  SPListEntity getCompanyInfo(@RequestParam String symbol);

  @GetMapping("/stock/candlesticks")
  StockChartDTO getHistoricalData(
      @RequestParam String symbol,
      @RequestParam String interval
  );
}