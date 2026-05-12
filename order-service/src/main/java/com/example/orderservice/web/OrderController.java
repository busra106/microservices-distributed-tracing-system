package com.example.orderservice.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final RestTemplate restTemplate;
    private final String paymentBaseUrl;
    private final String userBaseUrl;

    public OrderController(RestTemplate restTemplate,
                           @Value("${services.payment.base-url}") String paymentBaseUrl,
                           @Value("${services.user.base-url}") String userBaseUrl) {
        this.restTemplate = restTemplate;
        this.paymentBaseUrl = paymentBaseUrl;
        this.userBaseUrl = userBaseUrl;
    }

    @GetMapping("/order")
    public String order() {
        log.info("Handling GET /order. Calling user-service and payment-service...");

        String userResponse = restTemplate.getForObject(userBaseUrl + "/user", String.class);
        log.info("Received response from user-service: {}", userResponse);

        String paymentResponse = restTemplate.getForObject(paymentBaseUrl + "/payment", String.class);
        log.info("Received response from payment-service: {}", paymentResponse);

        return "Order OK | " + userResponse + " | " + paymentResponse;
    }

    // Hata senaryosu: payment-service'de kasıtlı exception tetikler.
    // Zipkin'de bu trace hata (kırmızı) olarak görünür.
    @GetMapping("/order/fail")
    public String orderFail() {
        log.info("Handling GET /order/fail. Triggering error scenario via payment-service...");
        try {
            restTemplate.getForObject(paymentBaseUrl + "/payment/fail", String.class);
        } catch (HttpServerErrorException e) {
            log.error("Downstream call to payment/fail returned HTTP {}: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
        return "Should not reach here";
    }
}

