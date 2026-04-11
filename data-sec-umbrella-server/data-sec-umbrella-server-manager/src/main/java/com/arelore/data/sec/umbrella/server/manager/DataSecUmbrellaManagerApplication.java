package com.arelore.data.sec.umbrella.server.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.arelore.data.sec.umbrella.server.manager",
        "com.arelore.data.sec.umbrella.server.core"
})
@MapperScan(
        basePackages = "com.arelore.data.sec.umbrella.server.core.mapper",
        sqlSessionFactoryRef = "sqlSessionFactory"
)
@EnableScheduling
public class DataSecUmbrellaManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSecUmbrellaManagerApplication.class, args);
    }
}
