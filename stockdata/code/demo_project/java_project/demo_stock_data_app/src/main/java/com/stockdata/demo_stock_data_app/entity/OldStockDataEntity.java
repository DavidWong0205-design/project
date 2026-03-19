package com.stockdata.demo_stock_data_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "old_stock_data")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OldStockDataEntity {
  private String symbol;
  private String timerange;
  private LocalDateTime date;
  private Double open;
  private Double high;
  private Double low;
  private Double close;
  private Double adj_close;
  private Long volume;
  @Id // 使用 MD5 字符串作為 ID
  private String id;
}
