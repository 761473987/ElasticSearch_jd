package com.zy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zy.es.mapper")
public class EsJdApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsJdApplication.class, args);
    }

}
