package com.stockdata.demo_stock_data_app.service.impl;

import com.stockdata.demo_stock_data_app.entity.OldStockDataEntity;
import com.stockdata.demo_stock_data_app.entity.RealTimeStockEntity;
import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.entity.StockDataEntity;
import com.stockdata.demo_stock_data_app.mapper.DtoMapper;
import com.stockdata.demo_stock_data_app.mapper.EntityMapper;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.model.dto.StockChartDTO;
import com.stockdata.demo_stock_data_app.repository.OldStockDataRepository;
import com.stockdata.demo_stock_data_app.repository.RealTimeStockRepository;
import com.stockdata.demo_stock_data_app.repository.SPListRepository;
import com.stockdata.demo_stock_data_app.repository.StockDataRepository;
import com.stockdata.demo_stock_data_app.service.StockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockDataServiceImpl implements StockDataService {

    private static final long SECONDS_PER_DAY = 86_400L;
    private static final long SAFETY_SECONDS = 300L;

    private static final Map<String, String> GRANULARITY_ALIAS = Map.of(
            "8d_1m", "1m",
            "60d_5m", "5m",
            "60d_15m", "15m",
            "60d_30m", "30m",
            "60d_90m", "90m",
            "730d_1h", "1h",
            "max_1d", "1d",
            "max_1mo", "1mo",
            "max_3mo", "3mo"
    );

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StockDataRepository stockDataRepository; // 歷史數據庫

    @Autowired
    private RealTimeStockRepository realTimeRepository; // 實時數據庫

    @Autowired
    private OldStockDataRepository oldStockDataRepository; // 歸檔數據庫

    @Autowired
    private SPListRepository spListRepository; // 公司背景資料

    /**
     * 查詢K線，若DB有缺口則自動調API補齊並寫回DB，最終返回完整K線。
     */
    @Override
    @Transactional
    public StockChartDTO getHistoryWithAutoFill(String symbol, long startDate, long endDate, String granularity) {
        long t0 = System.currentTimeMillis();
        String normalizedGranularity = normalizeGranularity(granularity);
        long now = System.currentTimeMillis() / 1000;
        String[] allowed = {"1m","5m","15m","30m","90m","1h","1d","1mo","3mo"};
        boolean valid = java.util.Arrays.asList(allowed).contains(normalizedGranularity);
        if (!valid) normalizedGranularity = "1d";

        // 1. 先取得DB現有資料
        List<StockDataEntity> dbList = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalizedGranularity);
        if ((dbList == null || dbList.isEmpty()) && !normalizedGranularity.equals("1m")) {
            // 若無該粒度，嘗試用1m聚合
            List<StockDataEntity> minList = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, "1m");
            dbList = aggregateToGranularity(minList, normalizedGranularity);
        }
        if (dbList == null) dbList = new java.util.ArrayList<>();

        // 僅在資料真的缺時才補資料
        long step = switch (normalizedGranularity) {
            case "1m" -> 60L;
            case "5m" -> 300L;
            case "15m" -> 900L;
            case "30m" -> 1800L;
            case "90m" -> 5400L;
            case "1h" -> 3600L;
            case "1d" -> 86400L;
            case "1mo" -> 2592000L;
            default -> 3600L;
        };
        long lastTs = dbList.isEmpty() ? 0 : dbList.get(dbList.size() - 1).getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        // 只在「最新一筆資料距離現在超過一個粒度」且「目前資料筆數大於0」時才補資料
        if (!dbList.isEmpty() && (now - lastTs > step)) {
            saveHistoricalData(symbol, lastTs + step, now, normalizedGranularity);
            dbList = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalizedGranularity);
        }

        // 嚴格去重
        java.util.Map<Long, StockDataEntity> uniqueMap = new java.util.LinkedHashMap<>();
        for (StockDataEntity e : dbList) {
            long ts = e.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            if (!uniqueMap.containsKey(ts)) {
                uniqueMap.put(ts, e);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println("[getHistoryWithAutoFill] symbol=" + symbol + ", granularity=" + normalizedGranularity + ", elapsed=" + (t1-t0) + "ms, resultCount=" + uniqueMap.size());
        return DtoMapper.toStockChartDto(new java.util.ArrayList<>(uniqueMap.values()), symbol, normalizedGranularity);

    }

    // 自動聚合1m資料為其他粒度
    private List<StockDataEntity> aggregateToGranularity(List<StockDataEntity> minList, String targetGranularity) {
        if (minList == null || minList.isEmpty()) return new java.util.ArrayList<>();
        // 先依timestamp去重，避免1m資料重複導致聚合重複
        java.util.Map<Long, StockDataEntity> uniqueMap = new java.util.LinkedHashMap<>();
        for (StockDataEntity e : minList) {
            long ts = e.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            if (!uniqueMap.containsKey(ts)) {
                uniqueMap.put(ts, e);
            }
        }
        java.util.List<StockDataEntity> deduped = new java.util.ArrayList<>(uniqueMap.values());
        long step = switch (targetGranularity) {
            case "5m" -> 300L;
            case "15m" -> 900L;
            case "30m" -> 1800L;
            case "90m" -> 5400L;
            case "1h" -> 3600L;
            case "1d" -> 86400L;
            case "1mo" -> 2592000L;
            case "3mo" -> 7776000L;
            default -> 60L;
        };
        java.util.List<StockDataEntity> result = new java.util.ArrayList<>();
        java.util.List<StockDataEntity> bucket = new java.util.ArrayList<>();
        long currentBucketStart = -1;
        for (StockDataEntity e : deduped) {
            long ts = e.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            long bucketStart = ts - (ts % step);
            if (currentBucketStart == -1) currentBucketStart = bucketStart;
            if (bucketStart != currentBucketStart && !bucket.isEmpty()) {
                result.add(aggregateBucket(bucket, currentBucketStart, targetGranularity));
                bucket.clear();
                currentBucketStart = bucketStart;
            }
            bucket.add(e);
        }
        if (!bucket.isEmpty()) {
            result.add(aggregateBucket(bucket, currentBucketStart, targetGranularity));
        }
        return result;
    }

    // 聚合一個bucket為一筆K線
    private StockDataEntity aggregateBucket(java.util.List<StockDataEntity> bucket, long ts, String granularity) {
        if (bucket == null || bucket.isEmpty()) return null;
        StockDataEntity first = bucket.get(0);
        StockDataEntity last = bucket.get(bucket.size() - 1);
        double open = first.getOpen();
        double close = last.getClose();
        double high = bucket.stream().mapToDouble(StockDataEntity::getHigh).max().orElse(open);
        double low = bucket.stream().mapToDouble(StockDataEntity::getLow).min().orElse(open);
        double volume = bucket.stream().mapToDouble(StockDataEntity::getVolume).sum();
        StockDataEntity agg = new StockDataEntity();
        agg.setId(com.stockdata.demo_stock_data_app.util.HashUtil.makeId(first.getSymbol(), granularity, String.valueOf(ts)));
        agg.setSymbol(first.getSymbol());
        agg.setTimerange(granularity);
        agg.setDate(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(ts), java.time.ZoneId.systemDefault()));
        agg.setOpen(open);
        agg.setHigh(high);
        agg.setLow(low);
        agg.setClose(close);
        agg.setVolume(volume);
        return agg;
    }
    /**
     * 補齊缺漏時間點（用前一筆數據填補），支援所有粒度。
     */
    private void fillGapByPrevious(String symbol, String normalized, long start, long now) {
        List<Long> expected = getExpectedTimestamps(start, now, normalized);
        // 只補到 now 之前的最後一個 expected timestamp
        long nowEpoch = System.currentTimeMillis() / 1000;
        if (!expected.isEmpty() && expected.get(expected.size() - 1) > nowEpoch) {
            // 移除所有超過 now 的 timestamp
            int lastIdx = expected.size() - 1;
            while (lastIdx >= 0 && expected.get(lastIdx) > nowEpoch) {
                expected.remove(lastIdx);
                lastIdx--;
            }
        }
        List<StockDataEntity> existing = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalized);

        java.util.Map<Long, StockDataEntity> map = new java.util.HashMap<>();
        for (StockDataEntity e : existing) {
            long ts = e.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            map.put(ts, e);
        }

        java.util.List<Long> sortedTs = new java.util.ArrayList<>(map.keySet());
        java.util.Collections.sort(sortedTs);

        java.util.List<StockDataEntity> toSave = new java.util.ArrayList<>();

        // 找到最後一筆已存在的資料
        StockDataEntity lastEntity = null;
        Long lastTs = null;
        StockDataEntity firstEntity = null;
        Long firstTs = null;
        if (!sortedTs.isEmpty()) {
            lastTs = sortedTs.get(sortedTs.size() - 1);
            lastEntity = map.get(lastTs);
            firstTs = sortedTs.get(0);
            firstEntity = map.get(firstTs);
        }

        for (Long ts : expected) {
            if (map.containsKey(ts)) continue;
            // 找到前一個已存在的時間點
            Long prev = null;
            int idx = java.util.Collections.binarySearch(sortedTs, ts);
            if (idx >= 0) {
                prev = sortedTs.get(idx);
            } else {
                int ins = -idx - 1;
                if (ins - 1 >= 0) prev = sortedTs.get(ins - 1);
            }
            // 如果沒有可用的前一筆，且這是 expected 的最後一筆，且有 lastEntity，則用最後一筆資料補
            if (prev == null && lastEntity != null && lastTs != null && ts > lastTs.longValue()) {
                prev = lastTs;
            }
            // 新增：如果沒有可用的前一筆，且這是 expected 的最前面，且有 firstEntity，則用最早一筆資料補
            if (prev == null && firstEntity != null && firstTs != null && ts < firstTs.longValue()) {
                prev = firstTs;
            }
            if (prev == null) continue; // 沒有可用的前一筆，跳過

            StockDataEntity src = map.get(prev);
            StockDataEntity copy = new StockDataEntity();
            copy.setId(com.stockdata.demo_stock_data_app.util.HashUtil.makeId(symbol, normalized, String.valueOf(ts)));
            copy.setSymbol(symbol);
            copy.setTimerange(normalized);
            copy.setDate(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(ts), java.time.ZoneId.systemDefault()));
            copy.setOpen(src.getOpen());
            copy.setHigh(src.getHigh());
            copy.setLow(src.getLow());
            copy.setClose(src.getClose());
            copy.setVolume(src.getVolume());

            toSave.add(copy);
            // 將新時間點也加入 map & sortedTs，讓後續的缺口可以基於剛填補的資料繼續填補
            map.put(ts, copy);
            int pos = java.util.Collections.binarySearch(sortedTs, ts);
            if (pos < 0) sortedTs.add(-pos - 1, ts);
        }

        if (!toSave.isEmpty()) {
            // 再次過濾掉已存在 id，避免任何重複
            List<StockDataEntity> oldEntities = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalized);
            java.util.Set<String> existIds = new java.util.HashSet<>();
            for (StockDataEntity e : oldEntities) existIds.add(e.getId());
            List<StockDataEntity> onlyNew = new java.util.ArrayList<>();
            for (StockDataEntity e : toSave) {
                if (!existIds.contains(e.getId())) {
                    onlyNew.add(e);
                }
            }
            if (!onlyNew.isEmpty()) {
                stockDataRepository.saveAll(onlyNew);
            }
        }
    }

    // 原本的補缺口邏輯，供自動補滿時呼叫
    private void getHistoryWithAutoFillCore(String symbol, long startDate, long endDate, String granularity) {
        List<StockDataEntity> dbList = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, granularity);
        List<Long> dbTimestamps = dbList.stream()
                .map(e -> e.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond())
                .sorted()
                .toList();
        List<Long> shouldHave = getExpectedTimestamps(startDate, endDate, granularity);
        List<long[]> missingRanges = new java.util.ArrayList<>();
        Long gapStart = null;
        for (Long ts : shouldHave) {
            if (!dbTimestamps.contains(ts)) {
                if (gapStart == null) gapStart = ts;
            } else {
                if (gapStart != null) {
                    missingRanges.add(new long[]{gapStart, ts - 1});
                    gapStart = null;
                }
            }
        }
        if (gapStart != null) {
            missingRanges.add(new long[]{gapStart, shouldHave.get(shouldHave.size() - 1)});
        }
        for (long[] range : missingRanges) {
            long s = range[0];
            long e = range[1];
            long maxDays = getMaxDaysForGranularity(granularity);
            long window = maxDays * SECONDS_PER_DAY;
            if (maxDays <= 0) {
                // no limit, fetch whole range once
                saveHistoricalData(symbol, s, e, granularity);
            } else {
                long chunkStart = s;
                while (chunkStart <= e) {
                    long chunkEnd = Math.min(e, chunkStart + window - 1);
                    saveHistoricalData(symbol, chunkStart, chunkEnd, granularity);
                    chunkStart = chunkEnd + 1;
                }
            }
        }
    }

    private long getMaxDaysForGranularity(String granularity) {
        return switch (granularity) {
            case "1m" -> 8L;
            case "5m", "15m", "30m", "90m" -> 60L;
            case "1h" -> 730L;
            default -> -1L; // unlimited
        };
    }

    /**
     * 自動補滿指定 symbol 與 granularity 的所有歷史資料（60天/730天）
     */
    public void autoFillAllHistory(String symbol, String granularity) {
        long now = System.currentTimeMillis() / 1000;
        long days = switch (granularity) {
            case "1m" -> 8L;
            case "5m", "15m", "30m", "90m" -> 60L;
            case "1h" -> 730L;
            default -> 3650L; // 10年，for 1d/1mo/3mo
        };
        long start = now - days * 86400L;
        getHistoryWithAutoFill(symbol, start, now, granularity);
    }

    @Override
    @Transactional
    public void fillMissingFromLatest(String symbol, String granularity) {
        String normalized = normalizeGranularity(granularity);
        List<StockDataEntity> existing = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalized);
        long now = System.currentTimeMillis() / 1000;
        long startEpoch;
        if (existing == null || existing.isEmpty()) {
            // no data yet: start at maxDays window start
            long days = getMaxDaysForGranularity(normalized);
            if (days <= 0) {
                startEpoch = now - (3650L * SECONDS_PER_DAY);
            } else {
                startEpoch = now - (days * SECONDS_PER_DAY);
            }
        } else {
            StockDataEntity last = existing.get(existing.size() - 1);
            startEpoch = last.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            // advance one step to avoid duplicating last timestamp
            long step = switch (normalized) {
                case "1m" -> 60L;
                case "5m" -> 300L;
                case "15m" -> 900L;
                case "30m" -> 1800L;
                case "90m" -> 5400L;
                case "1h" -> 3600L;
                case "1d" -> 86400L;
                default -> 3600L;
            };
            startEpoch = startEpoch + step;
        }

        // Build expected timestamps from startEpoch to now and call core fill
        getHistoryWithAutoFillCore(symbol, startEpoch, now, normalized);
    }

    /**
     * 根據粒度生成應有的K線時間戳列表
     */
    private List<Long> getExpectedTimestamps(long start, long end, String granularity) {
        List<Long> result = new ArrayList<>();
        long step;
        switch (granularity) {
            case "1m" -> step = 60L;
            case "5m" -> step = 300L;
            case "15m" -> step = 900L;
            case "30m" -> step = 1800L;
            case "90m" -> step = 5400L;
            case "1h" -> step = 3600L;
            case "1d" -> step = 86400L;
            case "1mo" -> step = 2592000L;
            default -> step = 3600L;
        }
        java.time.ZoneId zone = java.time.ZoneId.of("America/New_York");
        java.time.LocalDate startDate = java.time.Instant.ofEpochSecond(start).atZone(zone).toLocalDate();
        java.time.LocalDate endDate = java.time.Instant.ofEpochSecond(end).atZone(zone).toLocalDate();
        for (java.time.LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            // 美股開市時間 09:30-16:00（美東時間），週末不交易
            java.time.DayOfWeek dow = day.getDayOfWeek();
            if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) continue;
            long open = day.atTime(9, 30).atZone(zone).toEpochSecond();
            long close = day.atTime(16, 0).atZone(zone).toEpochSecond();
            for (long t = open; t < close; t += step) {
                if (t >= start && t <= end) result.add(t);
            }
        }
        // 日線、月線仍用原邏輯
        if (granularity.equals("1d") || granularity.equals("1mo")) {
            for (long t = start; t <= end; t += step) {
                result.add(t);
            }
        }
        return result;
    }

    // 1. 從 data-provider-app 抓取數據 (僅抓取)
    @Override
    public StockChartDTO fetchHistoricalData(String symbol, long start, long end, String granularity) {
        String normalizedGranularity = normalizeGranularity(granularity);

        long effectiveEnd = end > 0 ? end : (System.currentTimeMillis() / 1000);
        long effectiveStart = clampStartByGranularity(start, effectiveEnd, normalizedGranularity);

        String url = String.format("http://localhost:8080/stock/history?symbol=%s&startDate=%d&endDate=%d&dataGranularity=%s",
                                    symbol, effectiveStart, effectiveEnd, normalizedGranularity);

        StockChartDTO lastResponse = null;
        RestClientException lastException = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                StockChartDTO response = restTemplate.getForObject(url, StockChartDTO.class);
                lastResponse = response;
                boolean hasClose = response != null
                        && response.getClose() != null
                        && !response.getClose().isEmpty()
                        && response.getClose().stream().anyMatch(v -> v != null && v != 0.0);
                if (hasClose) {
                    if (response != null) {
                        if (response.getSymbol() == null || response.getSymbol().isBlank()) {
                            response.setSymbol(symbol);
                        }
                        if (response.getDataGranularity() == null || response.getDataGranularity().isBlank()) {
                            response.setDataGranularity(normalizedGranularity);
                        }
                    }
                    return response;
                }
            } catch (RestClientException ex) {
                lastException = ex;
            }
        }

        if (lastResponse != null) {
            if (lastResponse.getSymbol() == null || lastResponse.getSymbol().isBlank()) {
                lastResponse.setSymbol(symbol);
            }
            if (lastResponse.getDataGranularity() == null || lastResponse.getDataGranularity().isBlank()) {
                lastResponse.setDataGranularity(normalizedGranularity);
            }
            if (lastResponse.getTimestamps() == null) lastResponse.setTimestamps(new ArrayList<>());
            if (lastResponse.getClose() == null) lastResponse.setClose(new ArrayList<>());
            if (lastResponse.getOpen() == null) lastResponse.setOpen(new ArrayList<>());
            if (lastResponse.getHigh() == null) lastResponse.setHigh(new ArrayList<>());
            if (lastResponse.getLow() == null) lastResponse.setLow(new ArrayList<>());
            if (lastResponse.getVolume() == null) lastResponse.setVolume(new ArrayList<>());
            return lastResponse;
        }

        if (lastException != null) {
            StockChartDTO fallback = new StockChartDTO();
            fallback.setSymbol(symbol);
            fallback.setDataGranularity(normalizedGranularity);
            fallback.setTimestamps(new ArrayList<>());
            fallback.setClose(new ArrayList<>());
            fallback.setOpen(new ArrayList<>());
            fallback.setHigh(new ArrayList<>());
            fallback.setLow(new ArrayList<>());
            fallback.setVolume(new ArrayList<>());
            return fallback;
        }

        StockChartDTO fallback = new StockChartDTO();
        fallback.setSymbol(symbol);
        fallback.setDataGranularity(normalizedGranularity);
        fallback.setTimestamps(new ArrayList<>());
        fallback.setClose(new ArrayList<>());
        fallback.setOpen(new ArrayList<>());
        fallback.setHigh(new ArrayList<>());
        fallback.setLow(new ArrayList<>());
        fallback.setVolume(new ArrayList<>());
        return fallback;
    }

    public String normalizeGranularity(String granularity) {
        // 默認 granularity: Trend Range 預設 1m，其餘預設 1d
        if (granularity == null || granularity.isBlank()) {
            // 可根據調用場景判斷，這裡直接預設 1m
            return "1m";
        }
        // max_1d、max_1mo、max_3mo 直接保留原值，不做映射
        String trimmed = granularity.trim();
        if (trimmed.startsWith("max_")) return trimmed;
        return GRANULARITY_ALIAS.getOrDefault(trimmed, trimmed);
    }

    private long clampStartByGranularity(long requestedStart, long end, String normalizedGranularity) {
        long safeEnd = end > 0 ? end : (System.currentTimeMillis() / 1000);
        long fallbackStart = safeEnd - (30L * SECONDS_PER_DAY);
        long safeRequestedStart = requestedStart > 0 ? requestedStart : fallbackStart;

        Long maxDays = switch (normalizedGranularity) {
            case "1m" -> 8L;
            case "5m", "15m", "30m", "90m" -> 60L;
            case "1h" -> 730L;
            default -> null;
        };

        if (maxDays == null) {
            return safeRequestedStart;
        }

        long maxSeconds = maxDays * SECONDS_PER_DAY;
        long safeMaxWindow = Math.max(1L, maxSeconds - SAFETY_SECONDS);
        long minAllowedStart = safeEnd - safeMaxWindow;

        return Math.max(safeRequestedStart, minAllowedStart);
    }

    // 2. 抓取並保存到數據庫 (核心：生成 MD5 ID)
    @Override
    @Transactional
    public void saveHistoricalData(String symbol, long start, long end, String granularity) {
        StockChartDTO dto = fetchHistoricalData(symbol, start, end, granularity);
        if (dto == null || dto.getClose() == null) return;

        String normalizedGranularity = normalizeGranularity(granularity);
        List<StockDataEntity> newEntities = EntityMapper.toStockDataEntities(dto, symbol, normalizedGranularity);
        if (newEntities.isEmpty()) return;

        // 只新增新資料（根據 id 唯一性），不刪除舊資料
        List<StockDataEntity> oldEntities = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalizedGranularity);
        java.util.Set<String> existIds = new java.util.HashSet<>();
        for (StockDataEntity e : oldEntities) existIds.add(e.getId());
        List<StockDataEntity> onlyNew = new java.util.ArrayList<>();
        for (StockDataEntity e : newEntities) {
            if (!existIds.contains(e.getId())) {
                onlyNew.add(e);
            }
        }
        if (!onlyNew.isEmpty()) {
            stockDataRepository.saveAll(onlyNew);
        }
        // 過時資料的歸檔由 archiveOldData 方法獨立處理
    }

    // 3. 從數據庫讀取歷史數據 (轉回 DTO 格式給前端)
    @Override
    public StockChartDTO getHistoryFromDb(String symbol, String granularity) {
        List<StockDataEntity> entities = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, granularity);

        return DtoMapper.toStockChartDto(entities, symbol, granularity);
    }

    // 4. 獲取實時數據快照
    @Override
    public RealTimeStockDTO fetchRealtimeData(String symbol) {
        String url = "http://localhost:8080/stock/realtime?symbol=" + symbol;
        try {
            return restTemplate.getForObject(url, RealTimeStockDTO.class );
        } catch (RestClientException ex) {
            return realTimeRepository.findBySymbol(symbol)
                    .map(DtoMapper::toRealtimeDto)
                    .orElseGet(() -> {
                        RealTimeStockDTO dto = new RealTimeStockDTO();
                        dto.setSymbol(symbol);
                        dto.setRegularMarketPrice(0.0);
                        dto.setRegularMarketChange(0.0);
                        dto.setRegularMarketChangePercent(0.0);
                        dto.setRegularMarketDayHigh(0.0);
                        dto.setRegularMarketDayLow(0.0);
                        dto.setRegularMarketDayRange("N/A");
                        dto.setRegularMarketVolume(0L);
                        dto.setRegularMarketPreviousClose(0.0);
                        return dto;
                    });
        }
    }


    // 5. 保存實時數據到數據庫
    @Override
    @Transactional
    public void saveRealtimeData(String symbol) {
        RealTimeStockDTO dto = fetchRealtimeData(symbol);
        if (dto == null) return;

        RealTimeStockEntity entity = EntityMapper.toRealtimeEntity(dto);
        if (entity == null) return;

        if (entity.getSymbol() == null || entity.getSymbol().isBlank()) {
            entity.setSymbol(symbol);
        }

        realTimeRepository.save(entity);
    }

    /**
     * 簡單補齊：使用已有的 K 線資料，對缺失時間點複製前一筆可用的 K 線（最簡單的填補策略）
     */
    @Override
    @Transactional
    public void simpleBackfillFromExisting(String symbol, String granularity) {
        String normalized = normalizeGranularity(granularity);
        long now = System.currentTimeMillis() / 1000;
        long days = getMaxDaysForGranularity(normalized);
        if (days <= 0) days = 3650L; // fallback for unlimited
        long start = now - days * SECONDS_PER_DAY;

        List<Long> expected = getExpectedTimestamps(start, now, normalized);
        List<StockDataEntity> existing = stockDataRepository.findBySymbolAndTimerangeOrderByDateAsc(symbol, normalized);

        java.util.Map<Long, StockDataEntity> map = new java.util.HashMap<>();
        for (StockDataEntity e : existing) {
            long ts = e.getDate().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            map.put(ts, e);
        }

        java.util.List<Long> sortedTs = new java.util.ArrayList<>(map.keySet());
        java.util.Collections.sort(sortedTs);

        java.util.List<StockDataEntity> toSave = new java.util.ArrayList<>();

        for (Long ts : expected) {
            if (map.containsKey(ts)) continue;
            // 找到前一個已存在的時間點
            Long prev = null;
            int idx = java.util.Collections.binarySearch(sortedTs, ts);
            if (idx >= 0) {
                prev = sortedTs.get(idx);
            } else {
                int ins = -idx - 1;
                if (ins - 1 >= 0) prev = sortedTs.get(ins - 1);
            }
            if (prev == null) continue; // 沒有可用的前一筆，跳過

            StockDataEntity src = map.get(prev);
            StockDataEntity copy = new StockDataEntity();
            copy.setId(com.stockdata.demo_stock_data_app.util.HashUtil.makeId(symbol, normalized, String.valueOf(ts)));
            copy.setSymbol(symbol);
            copy.setTimerange(normalized);
            copy.setDate(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(ts), java.time.ZoneId.systemDefault()));
            copy.setOpen(src.getOpen());
            copy.setHigh(src.getHigh());
            copy.setLow(src.getLow());
            copy.setClose(src.getClose());
            copy.setVolume(src.getVolume());

            toSave.add(copy);
            // 將新時間點也加入 map & sortedTs，讓後續的缺口可以基於剛填補的資料繼續填補
            map.put(ts, copy);
            int pos = java.util.Collections.binarySearch(sortedTs, ts);
            if (pos < 0) sortedTs.add(-pos - 1, ts);
        }

        if (!toSave.isEmpty()) {
            stockDataRepository.saveAll(toSave);
        }
    }

    // 6. 數據歸檔：將舊數據移至 old_stock_data 表
    @Override
    @Transactional
    public void archiveOldData(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        // 找出 8d_1m 粒度的舊數據
        List<StockDataEntity> oldData = stockDataRepository.findByTimerangeAndDateBefore("8d_1m", threshold);
        
        if (oldData.isEmpty()) return;

        List<OldStockDataEntity> archiveList = oldData.stream().map(item -> {
            OldStockDataEntity old = new OldStockDataEntity();
            old.setId(item.getId()); // 保持 ID 一致
            old.setSymbol(item.getSymbol());
            old.setClose(item.getClose());
            old.setDate(item.getDate());
            return old;
        }).collect(Collectors.toList());

        oldStockDataRepository.saveAll(archiveList);
        stockDataRepository.deleteAll(oldData); // 從原表刪除
    }

    //7. 讀取公司背景資料 (stock_info)
        @Override
    public Optional<SPListEntity> getCompanyInfo(String symbol) {
        return spListRepository.findBySymbol(symbol);
    }

}
