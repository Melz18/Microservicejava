package br.edu.atitus.currency_service.services;

import java.util.Locale;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import br.edu.atitus.currency_service.config.CacheConfiguration;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.exceptions.CurrencyNotFoundException;

@Service
public class CurrencyConversionService {

    private final CacheManager cacheManager;
    private final CurrencyRateProvider rateProvider;

    public CurrencyConversionService(CacheManager cacheManager, CurrencyRateProvider rateProvider) {
        this.cacheManager = cacheManager;
        this.rateProvider = rateProvider;
    }

    public CurrencyEntity obterConversao(double value, String source, String target) {
        String normalizedSource = normalizar(source);
        String normalizedTarget = normalizar(target);
        String cacheKey = normalizedSource + "-" + normalizedTarget;

        Cache cache = cacheManager.getCache(CacheConfiguration.CURRENCY_RATES_CACHE);
        CurrencyRate cachedRate = cache != null ? cache.get(cacheKey, CurrencyRate.class) : null;

        CurrencyRate rate;
        String environment;

        if (cachedRate != null) {
            rate = cachedRate;
            environment = "Cache";
        } else {
            rate = rateProvider.carregarTaxa(normalizedSource, normalizedTarget);
            environment = rate.origin();
            if (cache != null) {
                cache.put(cacheKey, rate);
            }
        }

        CurrencyEntity response = new CurrencyEntity();
        response.setId(rate.id());
        response.setSource(rate.source());
        response.setTarget(rate.target());
        response.setConversionRate(rate.rate());
        response.setConvertedValue(value * rate.rate());
        response.setEnviroment(environment);

        return response;
    }

    private static String normalizar(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new CurrencyNotFoundException("Currency must be provided");
        }
        return currency.toUpperCase(Locale.ROOT);
    }
}
