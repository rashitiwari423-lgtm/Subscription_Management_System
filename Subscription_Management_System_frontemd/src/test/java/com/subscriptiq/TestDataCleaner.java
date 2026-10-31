package com.subscriptiq;

import com.subscriptiq.ai.repository.AiInsightRepository;
import com.subscriptiq.ai.repository.AiQueryLogRepository;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.discount.repository.DiscountRepository;
import com.subscriptiq.invoice.repository.InvoiceItemRepository;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.product.repository.ProductRepository;
import com.subscriptiq.product.repository.ProductVariantRepository;
import com.subscriptiq.quotation.repository.QuotationItemRepository;
import com.subscriptiq.quotation.repository.QuotationRepository;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.tax.repository.TaxRepository;
import com.subscriptiq.usage.repository.UsageRecordRepository;
import com.subscriptiq.user.repository.PasswordResetTokenRepository;
import com.subscriptiq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestDataCleaner {

    private final AiInsightRepository aiInsightRepository;
    private final AiQueryLogRepository aiQueryLogRepository;
    private final OptimizationInsightRepository optimizationInsightRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final QuotationItemRepository quotationItemRepository;
    private final QuotationRepository quotationRepository;
    private final PlanRepository planRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final TaxRepository taxRepository;
    private final DiscountRepository discountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanAll() {
        // Child tables first to satisfy foreign key constraints:
        aiInsightRepository.deleteAllInBatch();
        aiQueryLogRepository.deleteAllInBatch();
        optimizationInsightRepository.deleteAllInBatch();
        usageRecordRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        invoiceItemRepository.deleteAllInBatch();
        invoiceRepository.deleteAllInBatch();
        subscriptionRepository.deleteAllInBatch();
        quotationItemRepository.deleteAllInBatch();
        quotationRepository.deleteAllInBatch();
        planRepository.deleteAllInBatch();
        productVariantRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        resetTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        taxRepository.deleteAllInBatch();
        discountRepository.deleteAllInBatch();
    }
}

