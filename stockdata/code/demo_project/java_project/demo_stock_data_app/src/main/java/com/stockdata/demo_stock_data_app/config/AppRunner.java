package com.stockdata.demo_stock_data_app.config;

import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.repository.SPListRepository;
import com.stockdata.demo_stock_data_app.service.StockDataService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {

	private final SPListRepository spListRepository;
	private final StockDataService stockDataService;

	public AppRunner(SPListRepository spListRepository, StockDataService stockDataService) {
		this.spListRepository = spListRepository;
		this.stockDataService = stockDataService;
	}

	@Override
	public void run(String... args) {
		List<SPListEntity> stocks = spListRepository.findAll();
		for (SPListEntity stock : stocks) {
			try {
				stockDataService.saveRealtimeData(stock.getSymbol());
			} catch (Exception ignored) {
			}
		}
	}
}
