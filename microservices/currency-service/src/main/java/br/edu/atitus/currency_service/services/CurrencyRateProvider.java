package br.edu.atitus.currency_service.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.edu.atitus.currency_service.clients.BcbCurrencyClient;
import br.edu.atitus.currency_service.clients.dto.BcbCurrencyResponse;
import br.edu.atitus.currency_service.exceptions.CurrencyNotFoundException;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class CurrencyRateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRateProvider.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final MathContext DIVISION_CONTEXT = MathContext.DECIMAL64;

    private final BcbCurrencyClient bcbCurrencyClient;
    private final CurrencyRepository currencyRepository;
    private final int maxSearchDays;

    public CurrencyRateProvider(BcbCurrencyClient bcbCurrencyClient,
            CurrencyRepository currencyRepository,
            @Value("${currency-service.clients.bcb.max-search-days:7}") int maxSearchDays) {
        this.bcbCurrencyClient = bcbCurrencyClient;
        this.currencyRepository = currencyRepository;
        this.maxSearchDays = Math.max(1, maxSearchDays);
    }

    @CircuitBreaker(name = "bcbCurrency", fallbackMethod = "carregarDoBanco")
    public CurrencyRate carregarTaxa(String source, String target) {
        String normalizedSource = normalizar(source);
        String normalizedTarget = normalizar(target);

        BigDecimal sourceToBrl = buscarTaxaParaMoeda(normalizedSource);
        BigDecimal targetToBrl = buscarTaxaParaMoeda(normalizedTarget);

        BigDecimal conversionRate = sourceToBrl.divide(targetToBrl, DIVISION_CONTEXT);

        return new CurrencyRate(null, normalizedSource, normalizedTarget, conversionRate.doubleValue(), "API BCB");
    }

    public CurrencyRate carregarDoBanco(String source, String target, Throwable throwable) {
        String normalizedSource = normalizar(source);
        String normalizedTarget = normalizar(target);

        return currencyRepository.findBySourceAndTarget(normalizedSource, normalizedTarget)
                .map(entity -> new CurrencyRate(entity.getId(), entity.getSource(), entity.getTarget(),
                        entity.getConversionRate(), "Local Database"))
                .orElseThrow(() -> new CurrencyNotFoundException(
                        "Currency not found: " + normalizedSource + " -> " + normalizedTarget, throwable));
    }

    private BigDecimal buscarTaxaParaMoeda(String currency) {
        if ("BRL".equals(currency)) {
            return BigDecimal.ONE;
        }

        LocalDate date = LocalDate.now();

        for (int attempt = 0; attempt < maxSearchDays; attempt++) {
            String formattedDate = DATE_FORMATTER.format(date);
            try {
                BcbCurrencyResponse response = bcbCurrencyClient.buscarMoeda(encapsular(currency), encapsular(formattedDate), "json");
                BigDecimal rate = response.primeiraTaxaDisponivel()
                        .orElse(null);
                if (rate != null) {
                    return rate;
                }
            } catch (Exception ex) {
                LOGGER.debug("Error retrieving rate from BCB for {} on {}: {}", currency, formattedDate, ex.getMessage());
                throw ex;
            }
            date = date.minusDays(1);
        }

        throw new CurrencyNotFoundException("BCB does not provide rate for currency: " + currency);
    }

    private static String normalizar(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new CurrencyNotFoundException("Currency must be provided");
        }
        return currency.toUpperCase(Locale.ROOT);
    }

    private static String encapsular(String value) {
        return "'" + value + "'";
    }
}
