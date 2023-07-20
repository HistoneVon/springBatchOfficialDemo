package com.example.springbatchofficialdemo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author histonevon
 */
@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchOfficialDemoApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(SpringBatchOfficialDemoApplication.class, args)));
    }

}
