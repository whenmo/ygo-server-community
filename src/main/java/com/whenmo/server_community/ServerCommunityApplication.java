package com.whenmo.server_community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.whenmo" })
@EnableJpaRepositories(basePackages = "com.whenmo.repository")
@EntityScan(basePackages = "com.whenmo.model")
public class ServerCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerCommunityApplication.class, args);
    }

}
