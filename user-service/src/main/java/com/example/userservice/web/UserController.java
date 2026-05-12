package com.example.userservice.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/user")
    public String user() {
        log.info("Handling GET /user");
        return "User OK";
    }

    // Hata senaryosu: kasıtlı RuntimeException fırlatır.
    // Sleuth, exception'ı span'a hata olarak işaretler → Zipkin'de kırmızı görünür.
    @GetMapping("/user/fail")
    public String userFail() {
        log.error("Simulating user lookup failure — user not found");
        throw new RuntimeException("User not found: userId=999");
    }
}

