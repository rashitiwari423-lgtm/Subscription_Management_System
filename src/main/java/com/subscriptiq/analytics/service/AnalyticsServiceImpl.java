package com.subscriptiq.analytics.service;

import com.subscriptiq.analytics.dto.AnalyticsOverviewResponse;
import com.subscriptiq.analytics.dto.ChurnAnalyticsResponse;
import com.subscriptiq.analytics.dto.CustomerAnalyticsResponse;
import com.subscriptiq.analytics.dto.CustomerRevenueDto;
import com.subscriptiq.analytics.dto.DashboardSummaryResponse;
import com.subscriptiq.analytics.dto.MonthlyAmountDto;
import com.subscriptiq.analytics.dto.MonthlyChurnDto;
import com.subscriptiq.analytics.dto.MonthlyCountDto;
import com.subscriptiq.analytics.dto.MonthlyRevenueDto;
import com.subscriptiq.analytics.dto.OptimizationAnalyticsResponse;
import com.subscriptiq.analytics.dto.OptimizationOpportunityDto;
import com.subscriptiq.analytics.dto.PaymentAnalyticsResponse;
import com.subscriptiq.analytics.dto.PlanChurnDto;
import com.subscriptiq.analytics.dto.PlanCustomerCountDto;
import com.subscriptiq.analytics.dto.PlanPerformanceItemDto;
import com.subscriptiq.analytics.dto.PlanRevenueDto;
import com.subscriptiq.analytics.dto.PlanSubscriptionCountDto;
import com.subscriptiq.analytics.dto.RevenueAnalyticsResponse;
import com.subscriptiq.analytics.dto.SubscriptionAnalyticsResponse;
import com.subscriptiq.analytics.dto.UtilizationSummaryDto;
import com.subscriptiq.analytics.repository.AnalyticsRepository;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PlanRepository planRepository;
    private final OptimizationInsightRepository optimizationInsightRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public AnalyticsOverviewResponse getOverview() {
        long totalCustomers = customerRepository.count();
        long activeCustomers = analyticsRepository.countActiveCustomers();

        long totalSubscriptions = subscriptionRepository.count();
        long activeSubscriptions = analyticsRepository.countActiveSubscriptions();
        long expiringSubscriptions = analyticsRepository.countExpiringSubscriptions(LocalDate.now().plusDays(30));

        long totalInvoices = invoiceRepository.count();
        long paidInvoices = invoiceRepository.findByStatus(InvoiceStatus.PAID).size();
        long overdueInvoices = invoiceRepository.findByStatus(InvoiceStatus.OVERDUE).size();

        BigDecimal totalBilledAmount = analyticsRepository.sumTotalBilledAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalCollectedAmount = analyticsRepository.sumTotalCollectedAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal outstandingAmount = totalBilledAmount.subtract(totalCollectedAmount);
        if (outstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            outstandingAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            outstandingAmount = outstandingAmount.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal potentialOptimizationSavings = BigDecimal.ZERO;
        List<OptimizationInsight> insights = optimizationInsightRepository.findAll();
        for (OptimizationInsight insight : insights) {
            if (insight.getStatus() != OptimizationStatus.REJECTED && insight.getStatus() != OptimizationStatus.EXECUTED) {
                if (insight.getPotentialMonthlySavings() != null) {
                    potentialOptimizationSavings = potentialOptimizationSavings.add(insight.getPotentialMonthlySavings());
                }
            }
        }
        potentialOptimizationSavings = potentialOptimizationSavings.setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyRecurringRevenue = analyticsRepository.sumMonthlyRecurringRevenue().setScale(2, RoundingMode.HALF_UP);

        return AnalyticsOverviewResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .totalSubscriptions(totalSubscriptions)
                .activeSubscriptions(activeSubscriptions)
                .expiringSubscriptions(expiringSubscriptions)
                .totalInvoices(totalInvoices)
                .paidInvoices(paidInvoices)
                .overdueInvoices(overdueInvoices)
                .totalBilledAmount(totalBilledAmount)
                .totalCollectedAmount(totalCollectedAmount)
                .outstandingAmount(outstandingAmount)
                .potentialOptimizationSavings(potentialOptimizationSavings)
                .monthlyRecurringRevenue(monthlyRecurringRevenue)
                .build();
    }

    @Override
    public RevenueAnalyticsResponse getRevenueAnalytics(LocalDate startDate, LocalDate endDate, String billingCycle, Long customerId, Long planId) {
        validateDateRange(startDate, endDate);

        List<Invoice> allInvoices = invoiceRepository.findAll();

        // Filter invoices
        List<Invoice> filteredInvoices = allInvoices.stream()
                .filter(inv -> {
                    if (startDate != null && inv.getInvoiceDate() != null && inv.getInvoiceDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && inv.getInvoiceDate() != null && inv.getInvoiceDate().isAfter(endDate)) {
                        return false;
                    }
                    if (customerId != null && (inv.getCustomer() == null || !inv.getCustomer().getId().equals(customerId))) {
                        return false;
                    }
                    if (planId != null) {
                        if (inv.getSubscription() == null || inv.getSubscription().getPlan() == null || !inv.getSubscription().getPlan().getId().equals(planId)) {
                            return false;
                        }
                    }
                    if (billingCycle != null && !billingCycle.trim().isEmpty()) {
                        if (inv.getSubscription() == null || inv.getSubscription().getPlan() == null ||
                                !inv.getSubscription().getPlan().getBillingCycle().name().equalsIgnoreCase(billingCycle.trim())) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        BigDecimal totalBilledRevenue = BigDecimal.ZERO;
        BigDecimal totalPaidRevenue = BigDecimal.ZERO;
        BigDecimal overdueRevenue = BigDecimal.ZERO;

        Map<YearMonth, BigDecimal> monthlyBilled = new TreeMap<>();
        Map<YearMonth, BigDecimal> monthlyPaid = new TreeMap<>();
        Map<Long, PlanRevenueAccumulator> planMap = new HashMap<>();
        Map<Long, CustomerRevenueAccumulator> customerMap = new HashMap<>();

        for (Invoice inv : filteredInvoices) {
            BigDecimal billed = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal paid = inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal balance = inv.getBalanceAmount() != null ? inv.getBalanceAmount() : BigDecimal.ZERO;

            totalBilledRevenue = totalBilledRevenue.add(billed);
            totalPaidRevenue = totalPaidRevenue.add(paid);

            if (inv.getStatus() == InvoiceStatus.OVERDUE) {
                overdueRevenue = overdueRevenue.add(balance);
            }

            // Monthly breakdown
            if (inv.getInvoiceDate() != null) {
                YearMonth ym = YearMonth.from(inv.getInvoiceDate());
                monthlyBilled.put(ym, monthlyBilled.getOrDefault(ym, BigDecimal.ZERO).add(billed));
                monthlyPaid.put(ym, monthlyPaid.getOrDefault(ym, BigDecimal.ZERO).add(paid));
            }

            // Plan breakdown
            if (inv.getSubscription() != null && inv.getSubscription().getPlan() != null) {
                Plan plan = inv.getSubscription().getPlan();
                PlanRevenueAccumulator acc = planMap.computeIfAbsent(plan.getId(), k ->
                        new PlanRevenueAccumulator(plan.getId(), plan.getName(), plan.getBillingCycle().name()));
                acc.billedRevenue = acc.billedRevenue.add(billed);
                acc.subscriptionIds.add(inv.getSubscription().getId());
            }

            // Customer breakdown
            if (inv.getCustomer() != null) {
                Customer cust = inv.getCustomer();
                CustomerRevenueAccumulator cAcc = customerMap.computeIfAbsent(cust.getId(), k ->
                        new CustomerRevenueAccumulator(cust.getId(), cust.getCompanyName()));
                cAcc.billedRevenue = cAcc.billedRevenue.add(billed);
                cAcc.paidRevenue = cAcc.paidRevenue.add(paid);
            }
        }

        BigDecimal outstandingRevenue = totalBilledRevenue.subtract(totalPaidRevenue);
        if (outstandingRevenue.compareTo(BigDecimal.ZERO) < 0) {
            outstandingRevenue = BigDecimal.ZERO;
        }

        // Build monthly list with MoM growth
        List<MonthlyRevenueDto> revenueByMonth = new ArrayList<>();
        BigDecimal previousMonthBilled = null;

        for (Map.Entry<YearMonth, BigDecimal> entry : monthlyBilled.entrySet()) {
            YearMonth ym = entry.getKey();
            BigDecimal currentBilled = entry.getValue();
            BigDecimal currentPaid = monthlyPaid.getOrDefault(ym, BigDecimal.ZERO);

            Double momGrowth;
            if (previousMonthBilled == null) {
                momGrowth = 0.0;
            } else if (previousMonthBilled.compareTo(BigDecimal.ZERO) == 0) {
                momGrowth = currentBilled.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
            } else {
                BigDecimal diff = currentBilled.subtract(previousMonthBilled);
                momGrowth = diff.divide(previousMonthBilled, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            revenueByMonth.add(MonthlyRevenueDto.builder()
                    .month(ym.format(MONTH_FORMATTER))
                    .billedRevenue(currentBilled.setScale(2, RoundingMode.HALF_UP))
                    .paidRevenue(currentPaid.setScale(2, RoundingMode.HALF_UP))
                    .momGrowthPercentage(momGrowth)
                    .build());

            previousMonthBilled = currentBilled;
        }

        // Build plan list
        List<PlanRevenueDto> revenueByPlan = planMap.values().stream()
                .map(p -> PlanRevenueDto.builder()
                        .planId(p.planId)
                        .planName(p.planName)
                        .billingCycle(p.billingCycle)
                        .billedRevenue(p.billedRevenue.setScale(2, RoundingMode.HALF_UP))
                        .subscriptionCount(p.subscriptionIds.size())
                        .build())
                .toList();

        // Build customer list
        List<CustomerRevenueDto> revenueByCustomer = customerMap.values().stream()
                .map(c -> CustomerRevenueDto.builder()
                        .customerId(c.customerId)
                        .companyName(c.companyName)
                        .billedRevenue(c.billedRevenue.setScale(2, RoundingMode.HALF_UP))
                        .paidRevenue(c.paidRevenue.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        // Calculate overall growth rate
        Double overallGrowthRate = 0.0;
        if (revenueByMonth.size() >= 2) {
            BigDecimal firstMonthBilled = revenueByMonth.get(0).getBilledRevenue();
            BigDecimal lastMonthBilled = revenueByMonth.get(revenueByMonth.size() - 1).getBilledRevenue();
            if (firstMonthBilled.compareTo(BigDecimal.ZERO) > 0) {
                overallGrowthRate = lastMonthBilled.subtract(firstMonthBilled)
                        .divide(firstMonthBilled, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }
        }

        return RevenueAnalyticsResponse.builder()
                .totalBilledRevenue(totalBilledRevenue.setScale(2, RoundingMode.HALF_UP))
                .totalPaidRevenue(totalPaidRevenue.setScale(2, RoundingMode.HALF_UP))
                .outstandingRevenue(outstandingRevenue.setScale(2, RoundingMode.HALF_UP))
                .overdueRevenue(overdueRevenue.setScale(2, RoundingMode.HALF_UP))
                .revenueGrowthRate(overallGrowthRate)
                .revenueByMonth(revenueByMonth)
                .revenueByPlan(revenueByPlan)
                .revenueByCustomer(revenueByCustomer)
                .build();
    }

    @Override
    public SubscriptionAnalyticsResponse getSubscriptionAnalytics(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        List<Subscription> filtered = allSubscriptions.stream()
                .filter(s -> {
                    if (startDate != null && s.getStartDate() != null && s.getStartDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && s.getStartDate() != null && s.getStartDate().isAfter(endDate)) {
                        return false;
                    }
                    return true;
                })
                .toList();

        long total = filtered.size();
        long active = 0, pending = 0, paused = 0, expired = 0, closed = 0, draft = 0;

        Map<YearMonth, Long> newSubsByMonth = new TreeMap<>();
        Map<YearMonth, Long> cancellationsByMonth = new TreeMap<>();
        Map<Long, PlanSubscriptionAccumulator> planMap = new HashMap<>();
        Map<String, Long> cycleMap = new HashMap<>();

        for (Subscription s : filtered) {
            if (s.getStatus() == SubscriptionStatus.ACTIVE) active++;
            else if (s.getStatus() == SubscriptionStatus.PENDING) pending++;
            else if (s.getStatus() == SubscriptionStatus.PAUSED) paused++;
            else if (s.getStatus() == SubscriptionStatus.EXPIRED) expired++;
            else if (s.getStatus() == SubscriptionStatus.CLOSED) closed++;
            else if (s.getStatus() == SubscriptionStatus.DRAFT) draft++;

            // New subscriptions by month (using startDate)
            if (s.getStartDate() != null) {
                YearMonth ym = YearMonth.from(s.getStartDate());
                newSubsByMonth.put(ym, newSubsByMonth.getOrDefault(ym, 0L) + 1);
            }

            // Cancellations / Closures
            if (s.getStatus() == SubscriptionStatus.CLOSED || s.getStatus() == SubscriptionStatus.EXPIRED) {
                LocalDate eventDate = s.getEndDate() != null ? s.getEndDate()
                        : (s.getUpdatedAt() != null ? s.getUpdatedAt().toLocalDate() : s.getStartDate());
                if (eventDate != null) {
                    YearMonth ym = YearMonth.from(eventDate);
                    cancellationsByMonth.put(ym, cancellationsByMonth.getOrDefault(ym, 0L) + 1);
                }
            }

            // Subscriptions by plan
            if (s.getPlan() != null) {
                Plan plan = s.getPlan();
                PlanSubscriptionAccumulator pAcc = planMap.computeIfAbsent(plan.getId(),
                        k -> new PlanSubscriptionAccumulator(plan.getId(), plan.getName()));
                pAcc.totalCount++;
                if (s.getStatus() == SubscriptionStatus.ACTIVE) {
                    pAcc.activeCount++;
                }

                // Subscriptions by billing cycle
                String cycle = plan.getBillingCycle() != null ? plan.getBillingCycle().name() : "MONTHLY";
                cycleMap.put(cycle, cycleMap.getOrDefault(cycle, 0L) + 1);
            }
        }

        List<MonthlyCountDto> newMonthlyList = newSubsByMonth.entrySet().stream()
                .map(e -> MonthlyCountDto.builder().month(e.getKey().format(MONTH_FORMATTER)).count(e.getValue()).build())
                .toList();

        List<MonthlyCountDto> cancellationMonthlyList = cancellationsByMonth.entrySet().stream()
                .map(e -> MonthlyCountDto.builder().month(e.getKey().format(MONTH_FORMATTER)).count(e.getValue()).build())
                .toList();

        List<PlanSubscriptionCountDto> planList = planMap.values().stream()
                .map(p -> PlanSubscriptionCountDto.builder()
                        .planId(p.planId)
                        .planName(p.planName)
                        .count(p.totalCount)
                        .activeCount(p.activeCount)
                        .build())
                .toList();

        return SubscriptionAnalyticsResponse.builder()
                .totalSubscriptions(total)
                .activeSubscriptions(active)
                .pendingSubscriptions(pending)
                .pausedSubscriptions(paused)
                .expiredSubscriptions(expired)
                .closedSubscriptions(closed)
                .draftSubscriptions(draft)
                .newSubscriptionsByMonth(newMonthlyList)
                .cancellationsByMonth(cancellationMonthlyList)
                .subscriptionsByPlan(planList)
                .subscriptionsByBillingCycle(cycleMap)
                .build();
    }

    @Override
    public CustomerAnalyticsResponse getCustomerAnalytics(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Customer> allCustomers = customerRepository.findAll();

        List<Customer> filtered = allCustomers.stream()
                .filter(c -> {
                    if (startDate != null && c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().isAfter(endDate)) {
                        return false;
                    }
                    return true;
                })
                .toList();

        long total = filtered.size();
        long active = filtered.stream().filter(c -> c.getStatus() == CustomerStatus.ACTIVE).count();
        long inactive = total - active;

        // New customers by month
        Map<YearMonth, Long> monthlyNewCustomers = new TreeMap<>();
        for (Customer c : filtered) {
            if (c.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(c.getCreatedAt());
                monthlyNewCustomers.put(ym, monthlyNewCustomers.getOrDefault(ym, 0L) + 1);
            }
        }

        List<MonthlyCountDto> newCustomersList = monthlyNewCustomers.entrySet().stream()
                .map(e -> MonthlyCountDto.builder().month(e.getKey().format(MONTH_FORMATTER)).count(e.getValue()).build())
                .toList();

        // Customers by plan and customer subscription status
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        Map<Long, Set<Long>> planCustomersMap = new HashMap<>();
        Map<String, Set<Long>> statusCustomerMap = new HashMap<>();

        for (Subscription sub : allSubscriptions) {
            if (sub.getCustomer() != null) {
                Long custId = sub.getCustomer().getId();

                if (sub.getPlan() != null) {
                    planCustomersMap.computeIfAbsent(sub.getPlan().getId(), k -> new HashSet<>()).add(custId);
                }

                String statusName = sub.getStatus() != null ? sub.getStatus().name() : "UNKNOWN";
                statusCustomerMap.computeIfAbsent(statusName, k -> new HashSet<>()).add(custId);
            }
        }

        List<Plan> allPlans = planRepository.findAll();
        List<PlanCustomerCountDto> customersByPlan = allPlans.stream()
                .map(p -> PlanCustomerCountDto.builder()
                        .planId(p.getId())
                        .planName(p.getName())
                        .customerCount(planCustomersMap.getOrDefault(p.getId(), Collections.emptySet()).size())
                        .build())
                .toList();

        Map<String, Long> customersByStatus = new HashMap<>();
        for (Map.Entry<String, Set<Long>> entry : statusCustomerMap.entrySet()) {
            customersByStatus.put(entry.getKey(), (long) entry.getValue().size());
        }

        // Average subscriptions per customer
        long totalSubs = allSubscriptions.size();
        double avgSubs = total > 0 ? (double) totalSubs / total : 0.0;
        avgSubs = Math.round(avgSubs * 100.0) / 100.0;

        return CustomerAnalyticsResponse.builder()
                .totalCustomers(total)
                .activeCustomers(active)
                .inactiveCustomers(inactive)
                .newCustomersByMonth(newCustomersList)
                .customersByPlan(customersByPlan)
                .customersBySubscriptionStatus(customersByStatus)
                .averageSubscriptionsPerCustomer(avgSubs)
                .build();
    }

    @Override
    public PaymentAnalyticsResponse getPaymentAnalytics(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Payment> allPayments = paymentRepository.findAll();

        List<Payment> filtered = allPayments.stream()
                .filter(p -> {
                    if (startDate != null && p.getPaymentDate() != null && p.getPaymentDate().toLocalDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && p.getPaymentDate() != null && p.getPaymentDate().toLocalDate().isAfter(endDate)) {
                        return false;
                    }
                    return true;
                })
                .toList();

        long total = filtered.size();
        long success = 0, pending = 0, failed = 0, refunded = 0;
        BigDecimal totalCollected = BigDecimal.ZERO;
        Map<YearMonth, PaymentMonthlyAccumulator> monthlyCollectionMap = new TreeMap<>();

        for (Payment p : filtered) {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                success++;
                BigDecimal amt = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                totalCollected = totalCollected.add(amt);

                if (p.getPaymentDate() != null) {
                    YearMonth ym = YearMonth.from(p.getPaymentDate());
                    PaymentMonthlyAccumulator acc = monthlyCollectionMap.computeIfAbsent(ym, k -> new PaymentMonthlyAccumulator());
                    acc.amount = acc.amount.add(amt);
                    acc.count++;
                }
            } else if (p.getStatus() == PaymentStatus.PENDING) {
                pending++;
            } else if (p.getStatus() == PaymentStatus.FAILED) {
                failed++;
            } else if (p.getStatus() == PaymentStatus.REFUNDED) {
                refunded++;
            }
        }

        // Overdue invoice balance
        BigDecimal overdueInvoiceAmount = BigDecimal.ZERO;
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus(InvoiceStatus.OVERDUE);
        for (Invoice inv : overdueInvoices) {
            if (inv.getBalanceAmount() != null) {
                overdueInvoiceAmount = overdueInvoiceAmount.add(inv.getBalanceAmount());
            }
        }

        double successRate = total > 0 ? ((double) success / total) * 100.0 : 0.0;
        successRate = Math.round(successRate * 100.0) / 100.0;

        List<MonthlyAmountDto> monthlyCollections = monthlyCollectionMap.entrySet().stream()
                .map(e -> MonthlyAmountDto.builder()
                        .month(e.getKey().format(MONTH_FORMATTER))
                        .amount(e.getValue().amount.setScale(2, RoundingMode.HALF_UP))
                        .count(e.getValue().count)
                        .build())
                .toList();

        return PaymentAnalyticsResponse.builder()
                .totalPayments(total)
                .totalCollected(totalCollected.setScale(2, RoundingMode.HALF_UP))
                .successfulPayments(success)
                .pendingPayments(pending)
                .failedPayments(failed)
                .refundedPayments(refunded)
                .overdueInvoiceAmount(overdueInvoiceAmount.setScale(2, RoundingMode.HALF_UP))
                .paymentSuccessRate(successRate)
                .monthlyPaymentCollection(monthlyCollections)
                .build();
    }

    @Override
    public ChurnAnalyticsResponse getChurnAnalytics(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        LocalDate periodStart = startDate != null ? startDate : LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();

        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        long closedCount = 0;
        long expiredCount = 0;
        long activeAtStart = 0;

        Map<YearMonth, ChurnMonthlyAccumulator> monthlyChurnMap = new TreeMap<>();
        Map<Long, PlanChurnAccumulator> planChurnMap = new HashMap<>();

        // Initialize plan churn map
        List<Plan> plans = planRepository.findAll();
        for (Plan plan : plans) {
            planChurnMap.put(plan.getId(), new PlanChurnAccumulator(plan.getId(), plan.getName()));
        }

        for (Subscription sub : allSubscriptions) {
            // Check if active at beginning of period
            boolean wasActiveAtStart = sub.getStartDate() != null && !sub.getStartDate().isAfter(periodStart) &&
                    (sub.getEndDate() == null || !sub.getEndDate().isBefore(periodStart));
            if (wasActiveAtStart) {
                activeAtStart++;
            }

            // Track plan subscription counts
            if (sub.getPlan() != null && planChurnMap.containsKey(sub.getPlan().getId())) {
                PlanChurnAccumulator pAcc = planChurnMap.get(sub.getPlan().getId());
                pAcc.totalSubscriptions++;
                if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                    pAcc.activeSubscriptions++;
                }
            }

            // Check if closed/expired during the period
            LocalDate churnDate = null;
            if (sub.getStatus() == SubscriptionStatus.CLOSED) {
                churnDate = sub.getEndDate() != null ? sub.getEndDate()
                        : (sub.getUpdatedAt() != null ? sub.getUpdatedAt().toLocalDate() : null);
                if (churnDate != null && !churnDate.isBefore(periodStart) && !churnDate.isAfter(periodEnd)) {
                    closedCount++;
                }
            } else if (sub.getStatus() == SubscriptionStatus.EXPIRED) {
                churnDate = sub.getEndDate() != null ? sub.getEndDate()
                        : (sub.getUpdatedAt() != null ? sub.getUpdatedAt().toLocalDate() : null);
                if (churnDate != null && !churnDate.isBefore(periodStart) && !churnDate.isAfter(periodEnd)) {
                    expiredCount++;
                }
            }

            if (churnDate != null && !churnDate.isBefore(periodStart) && !churnDate.isAfter(periodEnd)) {
                YearMonth ym = YearMonth.from(churnDate);
                monthlyChurnMap.computeIfAbsent(ym, k -> new ChurnMonthlyAccumulator()).churnedCount++;

                if (sub.getPlan() != null && planChurnMap.containsKey(sub.getPlan().getId())) {
                    planChurnMap.get(sub.getPlan().getId()).churnedSubscriptions++;
                }
            }
        }

        long totalChurned = closedCount + expiredCount;

        // Calculate overall churn rate
        double overallChurnRate = 0.0;
        if (activeAtStart > 0) {
            overallChurnRate = Math.min(100.0, ((double) totalChurned / activeAtStart) * 100.0);
            overallChurnRate = Math.round(overallChurnRate * 100.0) / 100.0;
        }

        // Monthly churn list
        List<MonthlyChurnDto> churnByMonth = new ArrayList<>();
        for (Map.Entry<YearMonth, ChurnMonthlyAccumulator> entry : monthlyChurnMap.entrySet()) {
            YearMonth ym = entry.getKey();
            long churnedInMonth = entry.getValue().churnedCount;

            // Count subscriptions active at start of this month
            LocalDate monthStart = ym.atDay(1);
            long activeAtMonthStart = allSubscriptions.stream()
                    .filter(s -> s.getStartDate() != null && !s.getStartDate().isAfter(monthStart) &&
                            (s.getEndDate() == null || !s.getEndDate().isBefore(monthStart)))
                    .count();

            double monthRate = activeAtMonthStart > 0 ? Math.min(100.0, ((double) churnedInMonth / activeAtMonthStart) * 100.0) : 0.0;
            monthRate = Math.round(monthRate * 100.0) / 100.0;

            churnByMonth.add(MonthlyChurnDto.builder()
                    .month(ym.format(MONTH_FORMATTER))
                    .activeAtStart(activeAtMonthStart)
                    .churnedCount(churnedInMonth)
                    .churnRate(monthRate)
                    .build());
        }

        // Plan churn list
        List<PlanChurnDto> churnByPlan = planChurnMap.values().stream()
                .map(p -> {
                    double pRate = p.totalSubscriptions > 0 ? ((double) p.churnedSubscriptions / p.totalSubscriptions) * 100.0 : 0.0;
                    pRate = Math.round(pRate * 100.0) / 100.0;
                    return PlanChurnDto.builder()
                            .planId(p.planId)
                            .planName(p.planName)
                            .totalSubscriptions(p.totalSubscriptions)
                            .activeSubscriptions(p.activeSubscriptions)
                            .churnedSubscriptions(p.churnedSubscriptions)
                            .churnRate(pRate)
                            .build();
                })
                .toList();

        return ChurnAnalyticsResponse.builder()
                .churnRate(overallChurnRate)
                .closedSubscriptions(closedCount)
                .expiredSubscriptions(expiredCount)
                .totalChurned(totalChurned)
                .methodology("HISTORICAL_SUBSCRIPTION_LIFECYCLE")
                .churnByMonth(churnByMonth)
                .churnByPlan(churnByPlan)
                .build();
    }

    @Override
    public List<PlanPerformanceItemDto> getPlanPerformance(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Plan> plans = planRepository.findAll();
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        List<Invoice> allInvoices = invoiceRepository.findAll();

        List<PlanPerformanceItemDto> results = new ArrayList<>();

        for (Plan plan : plans) {
            long totalSubs = 0;
            long activeSubs = 0;
            long churnedSubs = 0;
            Set<Long> subIds = new HashSet<>();

            for (Subscription s : allSubscriptions) {
                if (s.getPlan() != null && s.getPlan().getId().equals(plan.getId())) {
                    if (startDate != null && s.getStartDate() != null && s.getStartDate().isBefore(startDate)) {
                        continue;
                    }
                    if (endDate != null && s.getStartDate() != null && s.getStartDate().isAfter(endDate)) {
                        continue;
                    }

                    totalSubs++;
                    subIds.add(s.getId());

                    if (s.getStatus() == SubscriptionStatus.ACTIVE) {
                        activeSubs++;
                    } else if (s.getStatus() == SubscriptionStatus.CLOSED || s.getStatus() == SubscriptionStatus.EXPIRED) {
                        churnedSubs++;
                    }
                }
            }

            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (Invoice inv : allInvoices) {
                if (inv.getSubscription() != null && subIds.contains(inv.getSubscription().getId())) {
                    if (startDate != null && inv.getInvoiceDate() != null && inv.getInvoiceDate().isBefore(startDate)) {
                        continue;
                    }
                    if (endDate != null && inv.getInvoiceDate() != null && inv.getInvoiceDate().isAfter(endDate)) {
                        continue;
                    }
                    if (inv.getTotalAmount() != null) {
                        totalRevenue = totalRevenue.add(inv.getTotalAmount());
                    }
                }
            }

            BigDecimal avgRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            if (totalSubs > 0) {
                avgRevenue = totalRevenue.divide(BigDecimal.valueOf(totalSubs), 2, RoundingMode.HALF_UP);
            }

            double churnRate = totalSubs > 0 ? ((double) churnedSubs / totalSubs) * 100.0 : 0.0;
            churnRate = Math.round(churnRate * 100.0) / 100.0;

            results.add(PlanPerformanceItemDto.builder()
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .billingCycle(plan.getBillingCycle() != null ? plan.getBillingCycle().name() : "MONTHLY")
                    .price(plan.getPrice() != null ? plan.getPrice() : BigDecimal.ZERO)
                    .totalSubscriptions(totalSubs)
                    .activeSubscriptions(activeSubs)
                    .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                    .averageRevenuePerSubscription(avgRevenue)
                    .churnRate(churnRate)
                    .growthRate(0.0)
                    .build());
        }

        return results;
    }

    @Override
    public OptimizationAnalyticsResponse getOptimizationAnalytics() {
        List<OptimizationInsight> allInsights = optimizationInsightRepository.findAll();

        int unusedResources = 0;
        BigDecimal totalMonthlySavings = BigDecimal.ZERO;
        BigDecimal totalAnnualSavings = BigDecimal.ZERO;
        List<OptimizationOpportunityDto> opportunities = new ArrayList<>();

        double sumUtilization = 0.0;
        int activeCount = 0;
        int underutilizedCount = 0;
        int optimalCount = 0;

        for (OptimizationInsight insight : allInsights) {
            if (insight.getStatus() != OptimizationStatus.REJECTED && insight.getStatus() != OptimizationStatus.EXECUTED) {
                activeCount++;
                int unused = insight.getUnusedQuantity() != null ? insight.getUnusedQuantity() : 0;
                unusedResources += unused;

                BigDecimal monthly = insight.getPotentialMonthlySavings() != null ? insight.getPotentialMonthlySavings() : BigDecimal.ZERO;
                BigDecimal annual = insight.getPotentialAnnualSavings() != null ? insight.getPotentialAnnualSavings() : BigDecimal.ZERO;

                totalMonthlySavings = totalMonthlySavings.add(monthly);
                totalAnnualSavings = totalAnnualSavings.add(annual);

                if (insight.getUtilizationPercentage() != null) {
                    double util = insight.getUtilizationPercentage().doubleValue();
                    sumUtilization += util;
                    if (util < 50.0) {
                        underutilizedCount++;
                    } else {
                        optimalCount++;
                    }
                }

                String custName = insight.getCustomer() != null ? insight.getCustomer().getCompanyName() : "N/A";
                String resType = insight.getUsageRecord() != null && insight.getUsageRecord().getResourceType() != null
                        ? insight.getUsageRecord().getResourceType() : "RESOURCE";
                String rec = insight.getRecommendation() != null ? insight.getRecommendation().name() : "N/A";
                opportunities.add(OptimizationOpportunityDto.builder()
                        .insightId(insight.getId())
                        .resourceName(insight.getResourceName())
                        .resourceType(resType)
                        .customerName(custName)
                        .unusedQuantity(unused)
                        .potentialMonthlySavings(monthly.setScale(2, RoundingMode.HALF_UP))
                        .potentialAnnualSavings(annual.setScale(2, RoundingMode.HALF_UP))
                        .recommendation(rec)
                        .status(insight.getStatus() != null ? insight.getStatus().name() : "PENDING")
                        .build());
            }
        }

        double avgUtilization = activeCount > 0 ? sumUtilization / activeCount : 0.0;
        avgUtilization = Math.round(avgUtilization * 100.0) / 100.0;

        UtilizationSummaryDto utilSummary = UtilizationSummaryDto.builder()
                .averageUtilizationPercentage(avgUtilization)
                .underutilizedCount(underutilizedCount)
                .optimalCount(optimalCount)
                .build();

        return OptimizationAnalyticsResponse.builder()
                .unusedResources(unusedResources)
                .potentialMonthlySavings(totalMonthlySavings.setScale(2, RoundingMode.HALF_UP))
                .potentialAnnualSavings(totalAnnualSavings.setScale(2, RoundingMode.HALF_UP))
                .activeOpportunitiesCount(activeCount)
                .opportunities(opportunities)
                .utilizationSummary(utilSummary)
                .build();
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        AnalyticsOverviewResponse overview = getOverview();
        RevenueAnalyticsResponse revenue = getRevenueAnalytics(null, null, null, null, null);
        SubscriptionAnalyticsResponse subs = getSubscriptionAnalytics(null, null);
        CustomerAnalyticsResponse custs = getCustomerAnalytics(null, null);
        PaymentAnalyticsResponse payments = getPaymentAnalytics(null, null);
        ChurnAnalyticsResponse churn = getChurnAnalytics(null, null);
        OptimizationAnalyticsResponse opt = getOptimizationAnalytics();

        return DashboardSummaryResponse.builder()
                .revenue(DashboardSummaryResponse.RevenueSnapshot.builder()
                        .totalBilledRevenue(revenue.getTotalBilledRevenue())
                        .totalPaidRevenue(revenue.getTotalPaidRevenue())
                        .outstandingRevenue(revenue.getOutstandingRevenue())
                        .overdueRevenue(revenue.getOverdueRevenue())
                        .revenueGrowthRate(revenue.getRevenueGrowthRate())
                        .build())
                .subscriptions(DashboardSummaryResponse.SubscriptionSnapshot.builder()
                        .totalSubscriptions(subs.getTotalSubscriptions())
                        .activeSubscriptions(subs.getActiveSubscriptions())
                        .pausedSubscriptions(subs.getPausedSubscriptions())
                        .expiredSubscriptions(subs.getExpiredSubscriptions())
                        .closedSubscriptions(subs.getClosedSubscriptions())
                        .monthlyRecurringRevenue(overview.getMonthlyRecurringRevenue())
                        .build())
                .customers(DashboardSummaryResponse.CustomerSnapshot.builder()
                        .totalCustomers(custs.getTotalCustomers())
                        .activeCustomers(custs.getActiveCustomers())
                        .inactiveCustomers(custs.getInactiveCustomers())
                        .averageSubscriptionsPerCustomer(custs.getAverageSubscriptionsPerCustomer())
                        .build())
                .payments(DashboardSummaryResponse.PaymentSnapshot.builder()
                        .totalPayments(payments.getTotalPayments())
                        .totalCollected(payments.getTotalCollected())
                        .successfulPayments(payments.getSuccessfulPayments())
                        .paymentSuccessRate(payments.getPaymentSuccessRate())
                        .build())
                .churn(DashboardSummaryResponse.ChurnSnapshot.builder()
                        .churnRate(churn.getChurnRate())
                        .totalChurned(churn.getTotalChurned())
                        .methodology(churn.getMethodology())
                        .build())
                .optimization(DashboardSummaryResponse.OptimizationSnapshot.builder()
                        .unusedResources(opt.getUnusedResources())
                        .potentialMonthlySavings(opt.getPotentialMonthlySavings())
                        .potentialAnnualSavings(opt.getPotentialAnnualSavings())
                        .activeOpportunitiesCount(opt.getActiveOpportunitiesCount())
                        .build())
                .build();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must not be after endDate");
        }
    }

    // Accumulator helper structures
    private static class PlanRevenueAccumulator {
        Long planId;
        String planName;
        String billingCycle;
        BigDecimal billedRevenue = BigDecimal.ZERO;
        Set<Long> subscriptionIds = new HashSet<>();

        PlanRevenueAccumulator(Long planId, String planName, String billingCycle) {
            this.planId = planId;
            this.planName = planName;
            this.billingCycle = billingCycle;
        }
    }

    private static class CustomerRevenueAccumulator {
        Long customerId;
        String companyName;
        BigDecimal billedRevenue = BigDecimal.ZERO;
        BigDecimal paidRevenue = BigDecimal.ZERO;

        CustomerRevenueAccumulator(Long customerId, String companyName) {
            this.customerId = customerId;
            this.companyName = companyName;
        }
    }

    private static class PlanSubscriptionAccumulator {
        Long planId;
        String planName;
        long totalCount = 0;
        long activeCount = 0;

        PlanSubscriptionAccumulator(Long planId, String planName) {
            this.planId = planId;
            this.planName = planName;
        }
    }

    private static class PaymentMonthlyAccumulator {
        BigDecimal amount = BigDecimal.ZERO;
        long count = 0;
    }

    private static class ChurnMonthlyAccumulator {
        long churnedCount = 0;
    }

    private static class PlanChurnAccumulator {
        Long planId;
        String planName;
        long totalSubscriptions = 0;
        long activeSubscriptions = 0;
        long churnedSubscriptions = 0;

        PlanChurnAccumulator(Long planId, String planName) {
            this.planId = planId;
            this.planName = planName;
        }
    }
}
