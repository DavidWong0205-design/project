package com.stockdata.demo_stock_data_app.service;

import java.util.Optional;
import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.model.dto.RealTimeStockDTO;
import com.stockdata.demo_stock_data_app.model.dto.StockChartDTO;

public interface StockDataService {

    // 1. 從 data-provider-app 抓取數據 (僅抓取，不存庫)
    StockChartDTO fetchHistoricalData(String symbol, long startDate, long endDate, String granularity);

    // 2. 【新增】抓取並保存到數據庫 (整合 HashUtil 生成 MD5 ID)
    void saveHistoricalData(String symbol, long startDate, long endDate, String granularity);

    // 3. 從數據庫讀取歷史數據 (增加 granularity 參數)
    StockChartDTO getHistoryFromDb(String symbol, String granularity);

    // 4. 獲取實時數據快照
    RealTimeStockDTO fetchRealtimeData(String symbol);

    // 5. 保存實時數據到數據庫
    void saveRealtimeData(String symbol);

    // 6. 數據歸檔：將舊數據移至 old_stock_data 表
    void archiveOldData(int daysThreshold);

    // 7. 讀取公司背景資料 (stock_info)
    Optional<SPListEntity> getCompanyInfo(String symbol);

    /**
     * 查詢K線，若DB有缺口則自動調API補齊並寫回DB，最終返回完整K線。
     * @param symbol 股票代碼
     * @param startDate 開始時間（秒）
     * @param endDate 結束時間（秒）
     * @param granularity K線粒度（如1h, 1d等）
     * @return 完整K線數據
     */
    StockChartDTO getHistoryWithAutoFill(String symbol, long startDate, long endDate, String granularity);

    // 一鍵補齊最近的歷史資料（依 granularity 決定天數）
    void autoFillAllHistory(String symbol, String granularity);

    // 根據資料庫中最新 timestamp 偵測缺口並補齊到現在
    void fillMissingFromLatest(String symbol, String granularity);

    // 使用現有已存的 K 線資料，對缺失時間點做簡單補齊（複製前一個可用K線）
    void simpleBackfillFromExisting(String symbol, String granularity);
}


//為什麼 getHistoryFromDb 要加 granularity？
//如果你只傳 symbol，數據庫會把這支股票所有的 1m, 5m, 1d 
// 數據全部混在一起返回給你，前端圖表會亂掉。加上粒度參數後，你可以精確獲取某個時間維度的數據。