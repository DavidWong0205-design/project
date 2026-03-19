package com.stockdata.project_data_provider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryDTO {

	private Chart chart;

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Chart {

		private List<Result> result;
		private Object error;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Result {

		private Meta meta;
		private List<Long> timestamp;
		private Indicators indicators;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {

		private String currency;
		private String symbol;
		private String exchangeName;
		private String fullExchangeName;
		private String instrumentType;
		private Long firstTradeDate;
		private Long regularMarketTime;
		private Boolean hasPrePostMarketData;
		private Integer gmtoffset;
		private String timezone;
		private String exchangeTimezoneName;
		private Double regularMarketPrice;
		private Double fiftyTwoWeekHigh;
		private Double fiftyTwoWeekLow;
		private Double regularMarketDayHigh;
		private Double regularMarketDayLow;
		private Long regularMarketVolume;
		private String longName;
		private String shortName;
		private Double chartPreviousClose;
		private Integer priceHint;
		private CurrentTradingPeriod currentTradingPeriod;
		private String dataGranularity;
		private String range;
		private List<String> validRanges;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CurrentTradingPeriod {

		private TradingPeriod pre;
		private TradingPeriod regular;
		private TradingPeriod post;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TradingPeriod {

		private String timezone;
		private Long start;
		private Long end;
		private Integer gmtoffset;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Indicators {

		private List<Quote> quote;
		private List<AdjClose> adjclose;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Quote {

		private List<Double> high;
		private List<Double> low;
		private List<Double> close;
		private List<Long> volume;
		private List<Double> open;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AdjClose {

		private List<Double> adjclose;
	}
}
