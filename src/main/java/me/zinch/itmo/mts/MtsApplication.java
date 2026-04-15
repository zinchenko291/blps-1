package me.zinch.itmo.mts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtsApplication.class, args);
    }

}
