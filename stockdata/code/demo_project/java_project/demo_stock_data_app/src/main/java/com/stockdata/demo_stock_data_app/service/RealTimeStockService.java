package com.stockdata.demo_stock_data_app.service;

import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.repository.RealTimeStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RealTimeStockService {
    @Autowired
    private RealTimeStockRepository realTimeStockRepository;

    public List<RealTimeStockEntity> getTop10ByVolume() {
        return realTimeStockRepository.findTop10ByOrderByRegularMarketVolumeDesc();
    }

    public List<RealTimeStockEntity> getTop10ByMarketCap() {
        return realTimeStockRepository.findTop10ByMarketCap();
    }
}
