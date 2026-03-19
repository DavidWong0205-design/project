package com.stockdata.demo_stock_data_app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "stock_info")
public class SPListEntity {
  @Id
  private String symbol;
  private String company_name;
  private String sector;
  private String industry;
}
