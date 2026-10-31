package com.subscriptiq.analytics.service;

import com.subscriptiq.analytics.dto.RevenueForecastResponse;

public interface RevenueForecastService {

    RevenueForecastResponse generateForecast(int months, String methodology);
}
