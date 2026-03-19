package com.stockdata.demo_stock_data_app.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RealTimeStockDTO {
	private String symbol;
	private Double regularMarketPrice;
	private Double regularMarketChangePercent;
	private Double regularMarketChange;
	private Double regularMarketDayHigh;
	private String regularMarketDayRange;
	private Double regularMarketDayLow;
	private Long regularMarketVolume;
	private Double regularMarketPreviousClose;
}
