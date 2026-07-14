package com.buildflow.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BuildFlow ERP 系统主启动类
 * 启动SpringBoot应用，扫描Mapper接口
 */
@SpringBootApplication
@MapperScan("com.buildflow.erp.mapper")
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }
}
