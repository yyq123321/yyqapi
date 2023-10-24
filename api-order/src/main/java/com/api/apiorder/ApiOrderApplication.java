package com.api.apiorder;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDubbo
@EnableTransactionManagement
@MapperScan("com.api.apiorder.mapper")
@EnableDiscoveryClient
public class ApiOrderApplication {

    public static void main(String[] args) {
        //将dubbo缓存的绝对目录改成相对目录，避免后续项目上线出现问题
        String rootPath = System.getProperty("user.dir");
        String subDirectory = "apiOrderDubboCache";
        String fullPath = rootPath + "/" + subDirectory;
        System.setProperty("user.home", fullPath);
        SpringApplication.run(ApiOrderApplication.class, args);
    }
}
