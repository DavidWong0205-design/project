package com.stockdata.demo_stock_data_app.mapper;

import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.entity.StockDataEntity;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.model.dto.StockChartDTO;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

	private DtoMapper() {
	}

	public static RealTimeStockDTO toRealtimeDto(RealTimeStockEntity entity) {
		if (entity == null) {
			return null;
		}

		RealTimeStockDTO dto = new RealTimeStockDTO();
		dto.setSymbol(entity.getSymbol());
		dto.setRegularMarketPrice(entity.getRegularMarketPrice());
		dto.setRegularMarketChange(entity.getRegularMarketChange());
		dto.setRegularMarketChangePercent(entity.getRegularMarketChangePercent());
		dto.setRegularMarketDayHigh(entity.getRegularMarketDayHigh());
		dto.setRegularMarketDayLow(entity.getRegularMarketDayLow());
		dto.setRegularMarketDayRange(entity.getRegularMarketDayRange());
		dto.setRegularMarketVolume(entity.getRegularMarketVolume());
		dto.setRegularMarketPreviousClose(entity.getRegularMarketPreviousClose());
		return dto;
	}

	public static StockChartDTO toStockChartDto(List<StockDataEntity> entities, String symbol, String granularity) {
		StockChartDTO dto = new StockChartDTO();
		dto.setSymbol(symbol);
		dto.setDataGranularity(granularity);

		List<StockDataEntity> safeList = entities == null ? Collections.emptyList() : entities;
		dto.setTimestamps(safeList.stream()
				.map(entity -> entity.getDate().atZone(ZoneId.systemDefault()).toEpochSecond())
				.collect(Collectors.toList()));
		dto.setOpen(safeList.stream().map(StockDataEntity::getOpen).collect(Collectors.toList()));
		dto.setHigh(safeList.stream().map(StockDataEntity::getHigh).collect(Collectors.toList()));
		dto.setLow(safeList.stream().map(StockDataEntity::getLow).collect(Collectors.toList()));
		dto.setClose(safeList.stream().map(StockDataEntity::getClose).collect(Collectors.toList()));
		dto.setVolume(safeList.stream().map(StockDataEntity::getVolume).collect(Collectors.toList()));
		return dto;
	}
}
