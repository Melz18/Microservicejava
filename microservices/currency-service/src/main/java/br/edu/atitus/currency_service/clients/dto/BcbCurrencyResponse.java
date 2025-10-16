package br.edu.atitus.currency_service.clients.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BcbCurrencyResponse {

    private List<BcbCurrencyValue> value;

    public List<BcbCurrencyValue> getValue() {
        return value;
    }

    public void setValue(List<BcbCurrencyValue> value) {
        this.value = value;
    }

    public Optional<BigDecimal> primeiraTaxaDisponivel() {
        if (value == null) {
            return Optional.empty();
        }
        return value.stream()
                .map(BcbCurrencyValue::taxaEfetiva)
                .filter(Objects::nonNull)
                .findFirst();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BcbCurrencyValue {

        @JsonProperty("cotacaoVenda")
        private BigDecimal saleRate;

        @JsonProperty("cotacaoCompra")
        private BigDecimal purchaseRate;

        public BigDecimal getSaleRate() {
            return saleRate;
        }

        public void setSaleRate(BigDecimal saleRate) {
            this.saleRate = saleRate;
        }

        public BigDecimal getPurchaseRate() {
            return purchaseRate;
        }

        public void setPurchaseRate(BigDecimal purchaseRate) {
            this.purchaseRate = purchaseRate;
        }

        private BigDecimal taxaEfetiva() {
            if (saleRate != null) {
                return saleRate;
            }
            return purchaseRate;
        }
    }
}
