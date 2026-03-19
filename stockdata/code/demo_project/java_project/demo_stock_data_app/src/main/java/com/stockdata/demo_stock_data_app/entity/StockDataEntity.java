package com.stockdata.demo_stock_data_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_prices")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockDataEntity {
  private String symbol;
  private String timerange;
  private LocalDateTime date;
  private Double open;
  private Double high;
  private Double low;
  private Double close;
  private Double adj_close;
  private Double volume;

  @Id
  private String id;
}
