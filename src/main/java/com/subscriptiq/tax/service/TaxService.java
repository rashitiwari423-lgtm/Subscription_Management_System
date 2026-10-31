package com.subscriptiq.tax.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.tax.dto.CreateTaxRequest;
import com.subscriptiq.tax.dto.TaxResponse;
import com.subscriptiq.tax.dto.UpdateTaxRequest;
import com.subscriptiq.tax.dto.UpdateTaxStatusRequest;
import com.subscriptiq.tax.enums.TaxStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaxService {

    TaxResponse createTax(CreateTaxRequest request);

    PagedResponse<TaxResponse> getAllTaxes(TaxStatus status, Pageable pageable);

    List<TaxResponse> getActiveTaxes();

    TaxResponse getTaxById(Long id);

    TaxResponse updateTax(Long id, UpdateTaxRequest request);

    TaxResponse updateTaxStatus(Long id, UpdateTaxStatusRequest request);
}

