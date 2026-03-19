package com.stockdata.demo_stock_data_app.controller.impl;

import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.mapper.RealTimeStockMapper;
import com.stockdata.demo_stock_data_app.service.RealTimeStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MarketCapController {
    @Autowired
    private RealTimeStockService realTimeStockService;

    @GetMapping("/stock/top-marketcap")
    public List<RealTimeStockDTO> getTop10ByMarketCap() {
        List<RealTimeStockEntity> entities = realTimeStockService.getTop10ByMarketCap();
        return RealTimeStockMapper.toDtoList(entities);
    }
}
