package com.subscriptiq.subscription.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    PagedResponse<SubscriptionResponse> getSubscriptions(String search, SubscriptionStatus status, Pageable pageable);

    SubscriptionResponse getSubscriptionById(Long id, UserPrincipal principal);

    PagedResponse<SubscriptionResponse> getCustomerSubscriptions(Long customerId, UserPrincipal principal, Pageable pageable);

    SubscriptionResponse pauseSubscription(Long id);

    SubscriptionResponse resumeSubscription(Long id);

    SubscriptionResponse closeSubscription(Long id);

    SubscriptionResponse renewSubscription(Long id);
}

