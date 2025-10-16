package br.edu.atitus.currency_service.services;

public record CurrencyRate(Long id, String source, String target, double rate, String origin) {
}
