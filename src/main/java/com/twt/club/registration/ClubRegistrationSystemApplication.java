package com.twt.club.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClubRegistrationSystemApplication {
    static void main(String[] args) {
        SpringApplication.run(ClubRegistrationSystemApplication.class, args);
    }
}
