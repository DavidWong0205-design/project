package com.stockdata.demo_stock_data_app.repository;

import com.stockdata.demo_stock_data_app.entity.OldStockDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OldStockDataRepository extends JpaRepository<OldStockDataEntity, String> {
}
