package com.stockdata.project_data_provider.service.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.stockdata.project_data_provider.dto.HistoryDto;
import com.stockdata.project_data_provider.dto.RealTimeDto;
import com.stockdata.project_data_provider.mapper.DtoMapper;
import com.stockdata.project_data_provider.model.dto.HistoryDTO;
import com.stockdata.project_data_provider.model.dto.RealTimeDTO;
import com.stockdata.project_data_provider.service.StockDataService;

@Service
public class StockDataServiceImpl implements StockDataService {

    private static final String CRUMB = "BFA4DOtGZ3b";
    private static final String COOKIE =
            "A1=d=AQABBOPXSGkCECN3P4koQW0nxjIC6z6qrYMFEgEBCAFhmWnJaViia3sB_eMDAAcI49dIaT6qrYM&S=AQAAAttp9NT8E1iA4_KiFmawDDg";

    private final RestTemplate restTemplate;
    private final DtoMapper dtoMapper;

    public StockDataServiceImpl(RestTemplate restTemplate,
            DtoMapper dtoMapper) {
        this.restTemplate = restTemplate;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public HistoryDto getHistoricalData(String symbol, int startDate,
            int endDate, String dataGranularity) {
        String interval =
                (dataGranularity == null || dataGranularity.isBlank()) ? "1d"
                        : dataGranularity;
        String url = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=%s&events=history&crumb=%s",
                symbol, startDate, endDate, interval, CRUMB);

        ResponseEntity<HistoryDTO> response = restTemplate.exchange(url,
                HttpMethod.GET, createEntity(), HistoryDTO.class);
        return dtoMapper.map(response.getBody());
    }


    @Override
    public RealTimeDto getRealTimeData(String symbol) {
        String url = String.format(
                "https://query1.finance.yahoo.com/v7/finance/quote?symbols=%s&crumb=%s",
                symbol, CRUMB);

        ResponseEntity<RealTimeDTO> response = restTemplate.exchange(url,
                HttpMethod.GET, createEntity(), RealTimeDTO.class);
        return dtoMapper.map(response.getBody());
    }

    private HttpEntity<String> createEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept", "application/json");
        headers.set("Cookie", COOKIE);
        return new HttpEntity<>(headers);
    }

}


