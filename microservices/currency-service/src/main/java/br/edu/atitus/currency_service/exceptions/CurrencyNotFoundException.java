package br.edu.atitus.currency_service.exceptions;

public class CurrencyNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CurrencyNotFoundException(String message) {
        super(message);
    }

    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
