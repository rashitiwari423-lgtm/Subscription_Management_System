package com.subscriptiq.ai.service.classifier;

import com.subscriptiq.ai.enums.AiIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class AiIntentClassifierImpl implements AiIntentClassifier {

    // Prompt injection, destructive actions, or secret extraction patterns:
    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)\\b(ignore\\s+(all\\s+)?(rules|instructions)|system\\s+prompt|jailbreak|bypass\\s+rules|" +
            "delete|drop\\s+table|truncate|alter\\s+table|insert\\s+into|update\\s+\\w+\\s+set|exec\\(|select\\s+.*from|" +
            "run\\s+sql|execute\\s+sql|run\\s+this\\s+sql|database\\s+password|secret_key|jwt_secret|private\\s+key)\\b"
    );

    @Override
    public boolean isMaliciousOrInjection(String question) {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }
        return INJECTION_PATTERN.matcher(question).find();
    }

    @Override
    public AiIntent classifyIntent(String question) {
        if (question == null || question.trim().isEmpty()) {
            return AiIntent.UNSUPPORTED_QUERY;
        }

        // Section 16 prompt injection check
        if (isMaliciousOrInjection(question)) {
            log.warn("Blocked potential prompt injection or unsafe query: [{}]", question);
            return AiIntent.UNSUPPORTED_QUERY;
        }

        String normalized = question.toLowerCase().trim();

        // 1. BUSINESS_OVERVIEW
        // e.g. "Mere business mein abhi kya problem hai?", "business overview", "problems in business"
        if (containsAny(normalized, "kya problem hai", "kya problem", "business overview", "business health",
                "business risk", "business status", "business mein", "overview of business", "company health")) {
            return AiIntent.BUSINESS_OVERVIEW;
        }

        // 2. OVERDUE_PAYMENTS
        // e.g. "Show overdue payments", "overdue invoices", "baki payment"
        if (containsAny(normalized, "overdue", "unpaid overdue", "overdue payment", "overdue invoice", "pending invoice")) {
            return AiIntent.OVERDUE_PAYMENTS;
        }

        // 3. EXPIRING_SUBSCRIPTIONS
        // e.g. "Which subscriptions are expiring?", "expiring subscriptions"
        if (containsAny(normalized, "expir", "expiry", "about to end", "renewal due", "ending soon")) {
            return AiIntent.EXPIRING_SUBSCRIPTIONS;
        }

        // 4. LATE_PAYMENTS
        // e.g. "Which customers pay late?", "late payments", "late payers"
        if (containsAny(normalized, "pay late", "late payment", "late payer", "delayed payment", "payment delay")) {
            return AiIntent.LATE_PAYMENTS;
        }

        // 5. REVENUE_SUMMARY
        // e.g. "How much revenue do we have?", "revenue summary", "total revenue"
        if (containsAny(normalized, "revenue", "income", "collection summary", "how much revenue", "total revenue")) {
            return AiIntent.REVENUE_SUMMARY;
        }

        // 6. UNUSED_RESOURCES
        // e.g. "Which resources are unused?", "unused resources", "idle capacity", "idle licenses"
        if (containsAny(normalized, "unused", "idle resource", "idle license", "idle capacity", "unassigned license")) {
            return AiIntent.UNUSED_RESOURCES;
        }

        // 7. OPTIMIZATION_SUMMARY
        // e.g. "How much can we save?", "cost savings", "optimization summary"
        if (containsAny(normalized, "save", "saving", "optimize cost", "cut cost", "reduce cost", "optimization")) {
            return AiIntent.OPTIMIZATION_SUMMARY;
        }

        // 8. PLAN_PERFORMANCE
        // e.g. "Which plans are losing customers?", "plan performance", "plans performance"
        if (containsAny(normalized, "plan", "losing customer", "best plan", "worst plan", "plan performance")) {
            return AiIntent.PLAN_PERFORMANCE;
        }

        // 9. SUBSCRIPTION_SUMMARY
        // e.g. "Show subscription summary", "how many subscriptions"
        if (containsAny(normalized, "subscription summary", "subscription overview", "how many subscription", "total subscription")) {
            return AiIntent.SUBSCRIPTION_SUMMARY;
        }

        return AiIntent.UNSUPPORTED_QUERY;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

