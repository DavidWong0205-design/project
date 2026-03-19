package com.stockdata.demo_stock_data_app.controller.impl;

import com.stockdata.demo_stock_data_app.controller.LogoOperation;
import com.stockdata.demo_stock_data_app.service.LogoCacheService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoController implements LogoOperation {

  private final LogoCacheService logoCacheService;

  public LogoController(LogoCacheService logoCacheService) {
    this.logoCacheService = logoCacheService;
  }

  @Override
  public ResponseEntity<Resource> getLogo(@PathVariable String symbol) {
    Path cached = logoCacheService.ensureCached(symbol);
    if (cached == null || !Files.exists(cached)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(new FileSystemResource(cached.toFile()));
  }

  @Override
  public Map<String, Object> preload() {
    int success = logoCacheService.preloadAll();
    return Map.of("preloaded", success);
  }
}
