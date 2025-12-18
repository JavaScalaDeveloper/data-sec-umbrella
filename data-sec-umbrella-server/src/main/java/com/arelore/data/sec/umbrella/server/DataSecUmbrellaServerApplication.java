package com.arelore.data.sec.umbrella.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.arelore.data.sec.umbrella.server.mapper")
public class DataSecUmbrellaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSecUmbrellaServerApplication.class, args);
    }

}
