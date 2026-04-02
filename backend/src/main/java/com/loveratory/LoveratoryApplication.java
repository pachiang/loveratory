package com.loveratory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Loveratory 應用程式進入點。
 * 實驗室受試者報名管理系統。
 */
@SpringBootApplication
@EnableJpaAuditing
public class LoveratoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoveratoryApplication.class, args);
    }
}
