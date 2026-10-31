package com.subscriptiq.usage.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.usage.dto.CreateUsageRecordRequest;
import com.subscriptiq.usage.dto.UsageRecordResponse;
import org.springframework.data.domain.Pageable;

public interface UsageService {

    UsageRecordResponse recordUsage(CreateUsageRecordRequest request);

    UsageRecordResponse getUsageRecordById(Long id, UserPrincipal principal);

    PagedResponse<UsageRecordResponse> getAllUsageRecords(Pageable pageable);

    PagedResponse<UsageRecordResponse> getCustomerUsageRecords(Long customerId, UserPrincipal principal, Pageable pageable);

    PagedResponse<UsageRecordResponse> getSubscriptionUsageRecords(Long subscriptionId, UserPrincipal principal, Pageable pageable);
}

