package com.shutterstock.oss.opentree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
public class OpenTreeApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenTreeApplication.class, args);
    }}
