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
public class RealTimeDTO {

	private QuoteResponse quoteResponse;

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QuoteResponse {

		private List<Result> result;
		private Object error;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Result {

		private String language;
		private String region;
		private String quoteType;
		private String typeDisp;
		private String quoteSourceName;
		private Boolean triggerable;
		private String customPriceAlertConfidence;
		private String currency;
		private String shortName;
		private String longName;
		private String marketState;
		private Double regularMarketChangePercent;
		private Double regularMarketPrice;
		private List<Object> corporateActions;
		private Long postMarketTime;
		private Long regularMarketTime;
		private String exchange;
		private String messageBoardId;
		private String exchangeTimezoneName;
		private String exchangeTimezoneShortName;
		private Long gmtOffSetMilliseconds;
		private String market;
		private Boolean esgPopulated;
		private Boolean hasPrePostMarketData;
		private Long firstTradeDateMilliseconds;
		private Integer priceHint;
		private Double postMarketChangePercent;
		private Double postMarketPrice;
		private Double postMarketChange;
		private Double regularMarketChange;
		private Double regularMarketDayHigh;
		private String regularMarketDayRange;
		private Double regularMarketDayLow;
		private Long regularMarketVolume;
		private Double regularMarketPreviousClose;
		private Double bid;
		private Double ask;
		private Integer bidSize;
		private Integer askSize;
		private String fullExchangeName;
		private String financialCurrency;
		private Double regularMarketOpen;
		private Long averageDailyVolume3Month;
		private Long averageDailyVolume10Day;
		private Double fiftyTwoWeekLowChange;
		private Double fiftyTwoWeekLowChangePercent;
		private String fiftyTwoWeekRange;
		private Double fiftyTwoWeekHighChange;
		private Double fiftyTwoWeekHighChangePercent;
		private Double fiftyTwoWeekLow;
		private Double fiftyTwoWeekHigh;
		private Double fiftyTwoWeekChangePercent;
		private Long earningsTimestamp;
		private Long earningsTimestampStart;
		private Long earningsTimestampEnd;
		private Long earningsCallTimestampStart;
		private Long earningsCallTimestampEnd;
		private Boolean isEarningsDateEstimate;
		private Double trailingAnnualDividendRate;
		private Double trailingPE;
		private Double trailingAnnualDividendYield;
		private Double epsTrailingTwelveMonths;
		private Double epsForward;
		private Double epsCurrentYear;
		private Double priceEpsCurrentYear;
		private Long sharesOutstanding;
		private Double bookValue;
		private Double fiftyDayAverage;
		private Double fiftyDayAverageChange;
		private Double fiftyDayAverageChangePercent;
		private Double twoHundredDayAverage;
		private Double twoHundredDayAverageChange;
		private Double twoHundredDayAverageChangePercent;
		private Long marketCap;
		private Double forwardPE;
		private Double priceToBook;
		private Integer sourceInterval;
		private Integer exchangeDataDelayedBy;
		private String averageAnalystRating;
		private Boolean tradeable;
		private Boolean cryptoTradeable;
		private String displayName;
		private String symbol;
	}
}