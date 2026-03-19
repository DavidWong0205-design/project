package com.stockdata.demo_stock_data_app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapStockDto {
  private String symbol;
  private String sector;
  private String industry;
  private Double marketCap;
  private Double changePct;
  private String logoUrl;
}