package br.edu.atitus.currency_service.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfiguration {

    public static final String CURRENCY_RATES_CACHE = "currencyRates";

    @Bean
    public CacheManager gerenciadorDeCache() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CURRENCY_RATES_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(500));
        return cacheManager;
    }
}
