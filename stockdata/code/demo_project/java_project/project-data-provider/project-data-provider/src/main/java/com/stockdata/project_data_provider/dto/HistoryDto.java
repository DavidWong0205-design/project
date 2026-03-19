package com.stockdata.project_data_provider.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryDto {

  private String symbol;
  private String dataGranularity;
  private List<Long> timestamp;
  private List<Double> open;
  private List<Double> high;
  private List<Double> close;
  private List<Long> volume;
  private List<Double> low;
}
