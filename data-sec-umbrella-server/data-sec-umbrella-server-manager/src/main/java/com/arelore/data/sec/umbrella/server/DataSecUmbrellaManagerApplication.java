package com.arelore.data.sec.umbrella.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.arelore.data.sec.umbrella.server")
@MapperScan("com.arelore.data.sec.umbrella.server.mapper")
@EnableScheduling
public class DataSecUmbrellaManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSecUmbrellaManagerApplication.class, args);
    }
}
