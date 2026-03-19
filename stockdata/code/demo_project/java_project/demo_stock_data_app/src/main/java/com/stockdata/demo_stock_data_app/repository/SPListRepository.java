package com.stockdata.demo_stock_data_app.repository;

import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SPListRepository extends JpaRepository<SPListEntity, String> {
    Optional<SPListEntity> findBySymbol(String symbol);

    List<SPListEntity> findBySector(String sector);

    List<SPListEntity> findByIndustry(String industry);
}
