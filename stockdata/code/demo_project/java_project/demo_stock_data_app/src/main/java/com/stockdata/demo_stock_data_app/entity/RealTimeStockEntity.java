package com.stockdata.demo_stock_data_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "realtime_stock")
public class RealTimeStockEntity {
    @Id
    private String symbol;
    private Double regularMarketPrice;
    private Double regularMarketChange;
    private Double regularMarketChangePercent;
    private Double regularMarketDayHigh;
    private Double regularMarketDayLow;
    private String regularMarketDayRange;
    private Double regularMarketPreviousClose;
    private Long regularMarketVolume;
}
