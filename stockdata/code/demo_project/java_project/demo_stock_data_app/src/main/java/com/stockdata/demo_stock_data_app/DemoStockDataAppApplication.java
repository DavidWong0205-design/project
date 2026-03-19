package com.stockdata.demo_stock_data_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoStockDataAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoStockDataAppApplication.class, args);
	}

}
