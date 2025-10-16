package br.edu.atitus.product_service.services;

import java.util.Locale;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.config.CacheConfiguration;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class ProductCurrencyService {

    public record ResultadoConversao(double convertedValue, String environment) {
    }

    private record ConversaoEmCache(double convertedValue, String currencyEnvironment) {
    }

    private final CacheManager cacheManager;
    private final CurrencyClient currencyClient;

    public ProductCurrencyService(CacheManager cacheManager, CurrencyClient currencyClient) {
        this.cacheManager = cacheManager;
        this.currencyClient = currencyClient;
    }

    public ResultadoConversao converterPreco(Long productId, double value, String sourceCurrency, String targetCurrency) {
        String normalizedSource = normalizar(sourceCurrency);
        String normalizedTarget = normalizar(targetCurrency);
        String cacheKey = productId + "-" + normalizedSource + "-" + normalizedTarget;

        Cache cache = cacheManager.getCache(CacheConfiguration.PRODUCT_CONVERSION_CACHE);
        ConversaoEmCache cached = cache != null ? cache.get(cacheKey, ConversaoEmCache.class) : null;

        if (cached != null) {
            return new ResultadoConversao(cached.convertedValue(), montarAmbienteDoCache(cached.currencyEnvironment()));
        }

        CurrencyResponse response = chamarServicoDeMoedas(value, normalizedSource, normalizedTarget);
        if (response == null) {
            return new ResultadoConversao(-1d, "Currency service fallback");
        }

        if (cache != null) {
            cache.put(cacheKey, new ConversaoEmCache(response.getConvertedValue(), response.getEnviroment()));
        }

        return new ResultadoConversao(response.getConvertedValue(), response.getEnviroment());
    }

    @CircuitBreaker(name = "productCurrency", fallbackMethod = "retornoAlternativo")
    protected CurrencyResponse chamarServicoDeMoedas(double value, String sourceCurrency, String targetCurrency) {
        return currencyClient.buscarMoeda(value, sourceCurrency, targetCurrency);
    }

    protected CurrencyResponse retornoAlternativo(double value, String sourceCurrency, String targetCurrency, Throwable throwable) {
        return null;
    }

    private static String montarAmbienteDoCache(String currencyEnvironment) {
        if (currencyEnvironment == null || currencyEnvironment.isBlank()) {
            return "Product Cache";
        }
        return "Product Cache (" + currencyEnvironment + ")";
    }

    private static String normalizar(String currency) {
        return currency == null ? null : currency.toUpperCase(Locale.ROOT);
    }
}
