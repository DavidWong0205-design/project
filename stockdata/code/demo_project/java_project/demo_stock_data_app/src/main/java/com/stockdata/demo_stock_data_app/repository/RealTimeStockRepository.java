package com.stockdata.demo_stock_data_app.repository;

import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RealTimeStockRepository extends JpaRepository<RealTimeStockEntity, String> {
    Optional<RealTimeStockEntity> findBySymbol(String symbol);

    // 取成交量最高前10名
    List<RealTimeStockEntity> findTop10ByOrderByRegularMarketVolumeDesc();

    // 取市值最高前10名（市值=價格*成交量）
    @Query("SELECT r FROM RealTimeStockEntity r WHERE r.regularMarketPrice IS NOT NULL AND r.regularMarketVolume IS NOT NULL ORDER BY (r.regularMarketPrice * r.regularMarketVolume) DESC")
    List<RealTimeStockEntity> findTop10ByMarketCap();
}
