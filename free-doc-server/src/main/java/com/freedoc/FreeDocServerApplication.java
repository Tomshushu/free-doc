package com.freedoc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.freedoc.mapper")
public class FreeDocServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreeDocServerApplication.class, args);
    }

}
