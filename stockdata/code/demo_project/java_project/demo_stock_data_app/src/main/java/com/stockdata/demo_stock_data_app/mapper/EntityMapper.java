package com.stockdata.demo_stock_data_app.mapper;

import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.entity.StockDataEntity;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.model.dto.StockChartDTO;
import com.stockdata.demo_stock_data_app.util.HashUtil;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class EntityMapper {

	private EntityMapper() {
	}

	public static RealTimeStockEntity toRealtimeEntity(RealTimeStockDTO dto) {
		if (dto == null) {
			return null;
		}

		RealTimeStockEntity entity = new RealTimeStockEntity();
		entity.setSymbol(dto.getSymbol());
		entity.setRegularMarketPrice(dto.getRegularMarketPrice());
		entity.setRegularMarketChange(dto.getRegularMarketChange());
		entity.setRegularMarketChangePercent(dto.getRegularMarketChangePercent());
		entity.setRegularMarketDayHigh(dto.getRegularMarketDayHigh());
		entity.setRegularMarketDayLow(dto.getRegularMarketDayLow());
		entity.setRegularMarketDayRange(dto.getRegularMarketDayRange());
		entity.setRegularMarketVolume(dto.getRegularMarketVolume());
		entity.setRegularMarketPreviousClose(dto.getRegularMarketPreviousClose());
		return entity;
	}

	public static List<StockDataEntity> toStockDataEntities(StockChartDTO dto, String symbol, String granularity) {
		List<StockDataEntity> entities = new ArrayList<>();
		if (dto == null || dto.getClose() == null || dto.getTimestamps() == null) {
			return entities;
		}

		int count = Math.min(dto.getClose().size(), dto.getTimestamps().size());
		for (int i = 0; i < count; i++) {
			Long timestamp = dto.getTimestamps().get(i);
			if (timestamp == null) {
				continue;
			}

			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
			    // 強制 timerange 只用 normalized granularity
			    StockDataEntity entity = StockDataEntity.builder()
				    .id(HashUtil.makeId(symbol, granularity, String.valueOf(timestamp)))
				    .symbol(symbol)
				    .timerange(granularity) // 這裡的 granularity 必須是 normalizedGranularity
				    .date(date)
				    .open(getByIndex(dto.getOpen(), i))
				    .high(getByIndex(dto.getHigh(), i))
				    .low(getByIndex(dto.getLow(), i))
				    .close(getByIndex(dto.getClose(), i))
				    .volume(getByIndex(dto.getVolume(), i))
				    .build();
			    entities.add(entity);
		}

		return entities;
	}

	private static Double getByIndex(List<Double> values, int index) {
		if (values == null || index < 0 || index >= values.size()) {
			return null;
		}
		return values.get(index);
	}
}
