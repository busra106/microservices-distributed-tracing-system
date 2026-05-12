package com.example.paymentservice.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @GetMapping("/payment")
    public String payment() {
        log.info("Handling GET /payment");
        return "Payment OK";
    }

    // Hata senaryosu: kasıtlı RuntimeException fırlatır.
    // order-service bu endpoint'i /order/fail üzerinden çağırır.
    // Sleuth her iki span'ı da hata olarak işaretler → Zipkin'de trace zinciri kırmızı.
    @GetMapping("/payment/fail")
    public String paymentFail() {
        log.error("Simulating payment failure — card declined");
        throw new RuntimeException("Payment processing failed: card declined");
    }
}

