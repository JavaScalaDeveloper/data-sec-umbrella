package com.arelore.data.sec.umbrella.server.worker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.arelore.data.sec.umbrella.server.worker",
        "com.arelore.data.sec.umbrella.server.core"
})
@MapperScan("com.arelore.data.sec.umbrella.server.core.mapper")
@EnableScheduling
/**
 * Worker 进程启动类。
 *
 * @author 黄佳豪
 */
public class DataSecUmbrellaWorkerApplication {

    /**
     * Worker 应用入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DataSecUmbrellaWorkerApplication.class, args);
    }
}
