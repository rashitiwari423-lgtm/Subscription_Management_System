package com.subscriptiq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "subscriptiq.billing")
public class BillingProperties {

    private Scheduler scheduler = new Scheduler();
    private Invoice invoice = new Invoice();

    @Data
    public static class Scheduler {
        private boolean enabled = true;
        private String cron = "0 0 1 * * *";
    }

    @Data
    public static class Invoice {
        private int dueDays = 7;
    }
}

