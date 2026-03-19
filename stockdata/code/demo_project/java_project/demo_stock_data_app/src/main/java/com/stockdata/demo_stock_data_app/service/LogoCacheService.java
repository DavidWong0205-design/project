package com.stockdata.demo_stock_data_app.service;

import com.stockdata.demo_stock_data_app.entity.SPListEntity;
import com.stockdata.demo_stock_data_app.repository.SPListRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LogoCacheService {

  private final RestTemplate restTemplate;
  private final SPListRepository spListRepository;
  private final Path cacheDir;

  public LogoCacheService(
      RestTemplate restTemplate,
      SPListRepository spListRepository,
      @Value("${app.logo-cache.dir:logo-cache}") String cacheDirPath) {
    this.restTemplate = restTemplate;
    this.spListRepository = spListRepository;
    this.cacheDir = Paths.get(cacheDirPath).toAbsolutePath().normalize();
  }

  public Path ensureCached(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      return null;
    }

    String normalized = normalizeSymbol(symbol);
    Path target = cacheDir.resolve(normalized + ".png");

    try {
      Files.createDirectories(cacheDir);
      if (Files.exists(target)) {
        return target;
      }

      for (String remoteUrl : buildCandidateUrls(symbol)) {
        try {
          ResponseEntity<byte[]> response = restTemplate.getForEntity(remoteUrl, byte[].class);
          if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().length == 0) {
            continue;
          }

          Files.write(target, response.getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
          return target;
        } catch (Exception ignored) {
        }
      }
      return null;
    } catch (Exception exception) {
      return null;
    }
  }

  public int preloadAll() {
    List<SPListEntity> stocks = spListRepository.findAll();
    int successCount = 0;
    for (SPListEntity stock : stocks) {
      Path cached = ensureCached(stock.getSymbol());
      if (cached != null) {
        successCount++;
      }
    }
    return successCount;
  }

  public Path resolveCachedPath(String symbol) {
    String normalized = normalizeSymbol(symbol);
    return cacheDir.resolve(normalized + ".png");
  }

  private String normalizeSymbol(String symbol) {
    return symbol.trim().toUpperCase().replaceAll("[^A-Z0-9._-]", "_");
  }

  private List<String> buildCandidateUrls(String symbol) {
    String normalized = symbol.trim().toUpperCase();
    return List.of(
        "https://eodhd.com/img/logos/US/" + normalized + ".png",
        "https://financialmodelingprep.com/image-stock/" + normalized + ".png",
        "https://storage.googleapis.com/iex/api/logos/" + normalized + ".png"
    );
  }
}
