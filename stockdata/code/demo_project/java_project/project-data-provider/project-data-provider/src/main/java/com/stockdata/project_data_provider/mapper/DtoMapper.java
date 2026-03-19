package com.stockdata.project_data_provider.mapper;

import org.springframework.stereotype.Component;
import com.stockdata.project_data_provider.dto.HistoryDto;
import com.stockdata.project_data_provider.dto.RealTimeDto;
import com.stockdata.project_data_provider.model.dto.HistoryDTO;
import com.stockdata.project_data_provider.model.dto.RealTimeDTO;

@Component
public class DtoMapper {

	public RealTimeDto map(RealTimeDTO source) {
		RealTimeDTO.Result result = source.getQuoteResponse().getResult().get(0);

		return RealTimeDto.builder() //
				.symbol(result.getSymbol()) //
				.regularMarketPrice(result.getRegularMarketPrice()) //
				.regularMarketChangePercent(result.getRegularMarketChangePercent()) //
				.regularMarketChange(result.getRegularMarketChange()) //
				.regularMarketDayHigh(result.getRegularMarketDayHigh()) //
				.regularMarketDayRange(result.getRegularMarketDayRange()) //
				.regularMarketDayLow(result.getRegularMarketDayLow()) //
				.regularMarketVolume(result.getRegularMarketVolume()) //
				.regularMarketPreviousClose(result.getRegularMarketPreviousClose()) //
				.marketCap(result.getMarketCap()) //
				.build();
	}

	public HistoryDto map(HistoryDTO source) {
		HistoryDTO.Result result = source.getChart().getResult().get(0);

		HistoryDTO.Meta meta = result.getMeta();
		HistoryDTO.Quote quote = result.getIndicators().getQuote().get(0);

		return HistoryDto.builder() //
				.symbol(meta.getSymbol()) //
				.dataGranularity(meta.getDataGranularity()) //
				.timestamp(result.getTimestamp()) //
				.open(quote.getOpen()) //
				.high(quote.getHigh()) //
				.close(quote.getClose()) //
				.volume(quote.getVolume()) //
				.low(quote.getLow()) //
				.build();
	}
}
