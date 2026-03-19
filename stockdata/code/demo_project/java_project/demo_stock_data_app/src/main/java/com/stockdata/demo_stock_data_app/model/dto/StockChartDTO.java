package com.stockdata.demo_stock_data_app.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockChartDTO {
	private String symbol;
	private String dataGranularity;
	@JsonAlias({"timestamp", "time", "t"})
	private List<Long> timestamps;
	private List<Double> close;
	private List<Double> high;
	private List<Double> low;
	private List<Double> open;
	private List<Double> volume;
}
