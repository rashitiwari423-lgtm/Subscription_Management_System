package com.subscriptiq.invoice.service;

import java.time.LocalDate;

public interface OverdueInvoiceService {

    int processOverdueInvoices(LocalDate currentDate);
}

