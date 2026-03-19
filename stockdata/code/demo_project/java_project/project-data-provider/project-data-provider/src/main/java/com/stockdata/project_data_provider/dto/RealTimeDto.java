package com.stockdata.project_data_provider.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RealTimeDto {

    private String symbol;
    private Double regularMarketPrice;
    private Double regularMarketChangePercent;
    private Double regularMarketChange;
    private Double regularMarketDayHigh;
    private String regularMarketDayRange;
    private Double regularMarketDayLow;
    private Long regularMarketVolume;
    private Double regularMarketPreviousClose;
    private Long marketCap;
}
