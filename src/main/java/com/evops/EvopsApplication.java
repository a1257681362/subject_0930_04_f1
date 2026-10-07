package com.evops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.evops.**.mapper")
public class EvopsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvopsApplication.class, args);
    }
}
