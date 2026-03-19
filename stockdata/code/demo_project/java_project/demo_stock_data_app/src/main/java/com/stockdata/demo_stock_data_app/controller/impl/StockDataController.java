
package com.stockdata.demo_stock_data_app.controller.impl;

import com.stockdata.demo_stock_data_app.controller.StockDataOperation;
import com.stockdata.demo_stock_data_app.dto.HeatmapStockDto;
import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.repository.RealTimeStockRepository;
import com.stockdata.demo_stock_data_app.repository.SPListRepository;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.model.dto.StockChartDTO;
import com.stockdata.demo_stock_data_app.service.StockDataService;
import com.stockdata.demo_stock_data_app.service.impl.StockDataServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class StockDataController implements StockDataOperation {

    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private SPListRepository spListRepository;

    @Autowired
    private RealTimeStockRepository realTimeStockRepository;

    @Override
    public List<HeatmapStockDto> heatmap() {
        List<SPListEntity> stocks = spListRepository.findAll();
        List<RealTimeStockEntity> realtimeList = realTimeStockRepository.findAll();
        java.util.Map<String, RealTimeStockEntity> realtimeMap = new java.util.HashMap<>();
        for (RealTimeStockEntity r : realtimeList) {
            realtimeMap.put(r.getSymbol(), r);
        }
        return stocks.stream().map(stock -> {
            HeatmapStockDto dto = new HeatmapStockDto();
            dto.setSymbol(stock.getSymbol());
            dto.setSector(stock.getSector());
            dto.setIndustry(stock.getIndustry());
            RealTimeStockEntity realtime = realtimeMap.get(stock.getSymbol());
            Double estimatedMarketCap = (realtime != null && realtime.getRegularMarketPrice() != null && realtime.getRegularMarketVolume() != null)
                    ? realtime.getRegularMarketPrice() * realtime.getRegularMarketVolume() : null;
            dto.setMarketCap(estimatedMarketCap);
            Double changePct = (realtime != null && realtime.getRegularMarketChangePercent() != null)
                    ? realtime.getRegularMarketChangePercent() : 0.0;
            dto.setChangePct(changePct);
            dto.setLogoUrl(buildLogoUrl(stock.getSymbol()));
            return dto;
        }).collect(Collectors.toList());
    }

    private String buildLogoUrl(String symbol) {
        return "/assets/logo/" + symbol;
    }

    @Override
    public RealTimeStockDTO getRealtimeData(String symbol) {
        return stockDataService.fetchRealtimeData(symbol);
    }

    @Override
    public SPListEntity getCompanyInfo(String symbol) {
        return stockDataService.getCompanyInfo(symbol).orElse(null);
    }

    @Override
    public StockChartDTO getHistoricalData(String symbol, String interval) {
        String normalized = normalizeGranularity(interval);
        // 取得目前時間作為 endDate，並根據粒度決定 startDate
        long now = System.currentTimeMillis() / 1000;
        long days = switch (normalized) {
            case "1m" -> 8L;
            case "5m", "15m", "30m", "90m" -> 60L;
            case "1h" -> 730L;
            default -> 3650L; // 10年，for 1d/1mo/3mo
        };
        long start = now - days * 86400L;
        return stockDataService.getHistoryWithAutoFill(symbol, start, now, normalized);
    }

    @PostMapping("/admin/fill")
    public String adminFillSingle(@RequestParam String symbol, @RequestParam String granularity) {
        try {
            stockDataService.autoFillAllHistory(symbol, granularity);
            return "ok";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }

    @PostMapping("/admin/fillAll")
    public String adminFillAll(@RequestParam(required = false) String granularity) {
        try {
            List<SPListEntity> list = spListRepository.findAll();
            List<String> granularities = new java.util.ArrayList<>();
            if (granularity != null && !granularity.isBlank()) {
                granularities.add(granularity);
            } else {
                granularities.add("5m");
                granularities.add("15m");
                granularities.add("30m");
                granularities.add("90m");
                granularities.add("1h");
            }
            // run in background to avoid HTTP timeout for long-running batch
            new Thread(() -> {
                for (SPListEntity e : list) {
                    for (String g : granularities) {
                        try {
                            stockDataService.autoFillAllHistory(e.getSymbol(), g);
                        } catch (Exception ex) {
                            // log and continue
                            System.err.println("batch fill failed for " + e.getSymbol() + " " + g + " : " + ex.getMessage());
                        }
                    }
                }
            }).start();
            return "started";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }

    @PostMapping("/admin/repair")
    public String adminRepair(@RequestParam String symbol, @RequestParam String granularity) {
        try {
            stockDataService.fillMissingFromLatest(symbol, granularity);
            return "ok";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }

    @PostMapping("/admin/simpleBackfill")
    public String adminSimpleBackfill(@RequestParam String symbol, @RequestParam String granularity) {
        try {
            stockDataService.simpleBackfillFromExisting(symbol, granularity);
            return "ok";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }

    @PostMapping("/admin/simpleBackfillAll")
    public String adminSimpleBackfillAll(@RequestParam(required = false) String granularity) {
        try {
            List<SPListEntity> list = spListRepository.findAll();
            List<String> granularities = new java.util.ArrayList<>();
            if (granularity != null && !granularity.isBlank()) {
                granularities.add(granularity);
            } else {
                granularities.add("5m");
                granularities.add("15m");
                granularities.add("30m");
                granularities.add("90m");
                granularities.add("1h");
            }
            new Thread(() -> {
                for (SPListEntity e : list) {
                    for (String g : granularities) {
                        try {
                            stockDataService.simpleBackfillFromExisting(e.getSymbol(), g);
                        } catch (Exception ex) {
                            System.err.println("simple backfill failed for " + e.getSymbol() + " " + g + " : " + ex.getMessage());
                        }
                    }
                }
            }).start();
            return "started";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }

    // 提取 normalization 方法，方便 controller 使用
    private String normalizeGranularity(String granularity) {
        if (stockDataService instanceof StockDataServiceImpl impl) {
            return impl.normalizeGranularity(granularity);
        }
        return granularity;
    }

    // 新增：前端「更新」按鈕呼叫的 API
    @PostMapping("/stock/update")
    public String updateStock(@RequestParam String symbol) {
        try {
            stockDataService.saveRealtimeData(symbol);
            return "ok";
        } catch (Exception ex) {
            return "error: " + ex.getMessage();
        }
    }
}