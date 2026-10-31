package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LatePaymentReviewExecutor implements ActionExecutor {

    private final PaymentRepository paymentRepository;

    @Override
    public AiActionType getActionType() {
        return AiActionType.REVIEW_LATE_PAYMENTS;
    }

    @Override
    public ActionPreviewDto preview(AiInsight insight) {
        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        int totalLatePayments = 0;
        BigDecimal totalAmountInvolved = BigDecimal.ZERO;
        Set<Long> affectedCustomerIds = new HashSet<>();
        List<String> affectedResources = new ArrayList<>();

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
                        affectedCustomerIds.add(customer.getId());
                    }

                    long daysLate = ChronoUnit.DAYS.between(dueDate, paymentDate);
                    String customerName = customer != null ? customer.getCompanyName() : "Unknown Customer";
                    affectedResources.add(String.format("Payment %s (%s, amount: %s, %d days late)",
                            payment.getPaymentNumber(), customerName, paymentAmount, daysLate));
                }
            }
        }

        List<String> warnings = new ArrayList<>();
        warnings.add("Action generates an internal late payment risk report. Customer billing terms remain unchanged.");

        return ActionPreviewDto.builder()
                .title("Review Historical Late Payments")
                .description(String.format("Analyze late payment risk patterns for %d customers with %d historically delayed transactions totaling %s.",
                        affectedCustomerIds.size(), totalLatePayments, totalAmountInvolved))
                .affectedCustomers(affectedCustomerIds.size())
                .affectedSubscriptions(0)
                .affectedInvoices(0)
                .potentialRevenue(BigDecimal.ZERO)
                .potentialSavings(BigDecimal.ZERO)
                .affectedResources(affectedResources)
                .warnings(warnings)
                .build();
    }

    @Override
    public ActionExecutionResult execute(AiActionRequest action, UserPrincipal user) {
        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        int totalLatePayments = 0;
        Set<String> affectedCustomerNames = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Payment payment : completedPayments) {
            if (payment.getInvoice() != null && payment.getInvoice().getDueDate() != null && payment.getPaymentDate() != null) {
                LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
                LocalDate dueDate = payment.getInvoice().getDueDate();

                if (paymentDate.isAfter(dueDate)) {
                    totalLatePayments++;
                    totalAmount = totalAmount.add(payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO);
                    Customer customer = payment.getCustomer() != null ? payment.getCustomer() : payment.getInvoice().getCustomer();
                    if (customer != null) {
                        affectedCustomerNames.add(customer.getCompanyName());
                    }
                }
            }
        }

        int affectedCount = affectedCustomerNames.size();
        String summary = String.format("Late payment risk review completed for %d customers across %d historically delayed payments (total volume: %s).",
                affectedCount, totalLatePayments, totalAmount);

        log.info("LatePaymentReviewExecutor executed by [{}]: {}", user != null ? user.getEmail() : "system", summary);

        return ActionExecutionResult.builder()
                .status(AiActionExecutionStatus.SUCCESS)
                .affectedCount(affectedCount)
                .summary(summary)
                .build();
    }
}

