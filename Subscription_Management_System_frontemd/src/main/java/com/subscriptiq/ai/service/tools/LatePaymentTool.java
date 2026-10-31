package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.CustomerSummaryDto;
import com.subscriptiq.ai.dto.LatePaymentResult;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LatePaymentTool {

    private final PaymentRepository paymentRepository;

    public LatePaymentResult analyze() {
        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        int totalLatePayments = 0;
        BigDecimal totalAmountInvolved = BigDecimal.ZERO;
        Map<Long, CustomerLateStats> customerStatsMap = new HashMap<>();

        for (Payment payment : completedPayments) {
            if (payment.getInvoice() != null && payment.getInvoice().getDueDate() != null && payment.getPaymentDate() != null) {
                LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
                LocalDate dueDate = payment.getInvoice().getDueDate();

                if (paymentDate.isAfter(dueDate)) {
                    totalLatePayments++;
                    BigDecimal paymentAmount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
                    totalAmountInvolved = totalAmountInvolved.add(paymentAmount);

                    Customer customer = payment.getCustomer() != null ? payment.getCustomer() : payment.getInvoice().getCustomer();
                    if (customer != null) {
                        long daysLate = ChronoUnit.DAYS.between(dueDate, paymentDate);
                        CustomerLateStats stats = customerStatsMap.computeIfAbsent(customer.getId(), k -> new CustomerLateStats(customer));
                        stats.incrementLateCount(paymentAmount, daysLate);
                    }
                }
            }
        }

        List<CustomerSummaryDto> affectedCustomers = new ArrayList<>();
        for (CustomerLateStats stats : customerStatsMap.values()) {
            affectedCustomers.add(CustomerSummaryDto.builder()
                    .customerId(stats.customer.getId())
                    .companyName(stats.customer.getCompanyName())
                    .email(stats.customer.getEmail())
                    .amount(stats.totalLateAmount)
                    .detail(String.format("%d late payments (avg delay: %d days)", stats.lateCount, stats.getAverageDelay()))
                    .build());
        }

        AiSeverity severity = totalLatePayments > 0 ? (totalLatePayments >= 5 ? AiSeverity.CRITICAL : AiSeverity.WARNING) : AiSeverity.INFO;

        return LatePaymentResult.builder()
                .latePaymentCount(totalLatePayments)
                .affectedCustomerCount(customerStatsMap.size())
                .totalAmountInvolved(totalAmountInvolved)
                .severity(severity)
                .recommendedAction(AiRecommendedAction.REVIEW_LATE_PAYMENTS)
                .affectedCustomers(affectedCustomers)
                .build();
    }

    private static class CustomerLateStats {
        private final Customer customer;
        private int lateCount = 0;
        private BigDecimal totalLateAmount = BigDecimal.ZERO;
        private long totalDelayDays = 0;

        public CustomerLateStats(Customer customer) {
            this.customer = customer;
        }

        public void incrementLateCount(BigDecimal amount, long delayDays) {
            this.lateCount++;
            this.totalLateAmount = this.totalLateAmount.add(amount);
            this.totalDelayDays += delayDays;
        }

        public long getAverageDelay() {
            return lateCount > 0 ? totalDelayDays / lateCount : 0;
        }
    }
}
