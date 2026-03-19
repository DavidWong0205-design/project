package com.stockdata.demo_stock_data_app.controller;

import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/assets/logo")
public interface LogoOperation {

	@GetMapping(value = "/{symbol}", produces = MediaType.IMAGE_PNG_VALUE)
	ResponseEntity<Resource> getLogo(@PathVariable String symbol);

	@GetMapping("/preload")
	Map<String, Object> preload();
}
