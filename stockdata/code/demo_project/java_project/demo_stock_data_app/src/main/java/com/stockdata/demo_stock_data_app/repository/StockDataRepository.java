package com.stockdata.demo_stock_data_app.repository;

import com.stockdata.demo_stock_data_app.entity.StockDataEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDataRepository extends JpaRepository<StockDataEntity, String> {
    List<StockDataEntity> findBySymbolAndTimerangeOrderByDateAsc(String symbol, String timerange);

    List<StockDataEntity> findByTimerangeAndDateBefore(String timerange, LocalDateTime threshold);
}
