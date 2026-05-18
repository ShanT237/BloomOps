package com.floreria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FloreriaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FloreriaApplication.class, args);
    }
}
