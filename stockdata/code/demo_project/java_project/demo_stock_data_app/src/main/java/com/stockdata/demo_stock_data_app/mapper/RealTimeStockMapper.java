package com.stockdata.demo_stock_data_app.mapper;

import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import java.util.List;
import java.util.stream.Collectors;

public class RealTimeStockMapper {
    public static RealTimeStockDTO toDto(RealTimeStockEntity entity) {
        if (entity == null) return null;
        RealTimeStockDTO dto = new RealTimeStockDTO();
        dto.setSymbol(entity.getSymbol());
        dto.setRegularMarketPrice(entity.getRegularMarketPrice());
        dto.setRegularMarketChange(entity.getRegularMarketChange());
        dto.setRegularMarketChangePercent(entity.getRegularMarketChangePercent());
        dto.setRegularMarketDayHigh(entity.getRegularMarketDayHigh());
        dto.setRegularMarketDayRange(entity.getRegularMarketDayRange());
        dto.setRegularMarketDayLow(entity.getRegularMarketDayLow());
        dto.setRegularMarketVolume(entity.getRegularMarketVolume());
        dto.setRegularMarketPreviousClose(entity.getRegularMarketPreviousClose());
        return dto;
    }
    public static List<RealTimeStockDTO> toDtoList(List<RealTimeStockEntity> entities) {
        return entities.stream().map(RealTimeStockMapper::toDto).collect(Collectors.toList());
    }
}
