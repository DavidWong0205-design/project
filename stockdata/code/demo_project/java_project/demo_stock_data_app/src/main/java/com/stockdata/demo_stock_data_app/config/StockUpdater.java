package com.stockdata.demo_stock_data_app.config;

import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.repository.SPListRepository;
import com.stockdata.demo_stock_data_app.service.StockDataService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockUpdater {

	private final SPListRepository spListRepository;
	private final StockDataService stockDataService;

	public StockUpdater(SPListRepository spListRepository, StockDataService stockDataService) {
		this.spListRepository = spListRepository;
		this.stockDataService = stockDataService;
	}

	@Scheduled(fixedDelayString = "${app.realtime-update-ms:300000}", initialDelayString = "${app.realtime-initial-delay-ms:30000}")
	public void updateRealtimeSnapshot() {
		List<SPListEntity> stocks = spListRepository.findAll();
		for (SPListEntity stock : stocks) {
			try {
				stockDataService.saveRealtimeData(stock.getSymbol());
			} catch (Exception ignored) {
			}
		}
	}

	@Scheduled(fixedDelayString = "${app.historical-update-ms:21600000}", initialDelayString = "${app.historical-initial-delay-ms:90000}")
	public void saveHistoricalSnapshot() {
		long end = Instant.now().getEpochSecond();
		long start = Instant.now().minus(8, ChronoUnit.DAYS).getEpochSecond();
		List<SPListEntity> stocks = spListRepository.findAll();
		for (SPListEntity stock : stocks) {
			try {
				stockDataService.saveHistoricalData(stock.getSymbol(), start, end, "8d_1m");
			} catch (Exception ignored) {
			}
		}
	}

	@Scheduled(cron = "${app.archive-cron:0 30 2 * * *}")
	public void archiveOldDataDaily() {
		try {
			stockDataService.archiveOldData(7);
		} catch (Exception ignored) {
		}
	}
}
