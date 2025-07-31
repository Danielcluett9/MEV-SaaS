package com.mevanalytics.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MEVPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(MEVPlatformApplication.class, args);
    }
}
