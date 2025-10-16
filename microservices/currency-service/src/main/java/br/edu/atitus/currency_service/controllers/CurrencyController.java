package br.edu.atitus.currency_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.exceptions.CurrencyNotFoundException;
import br.edu.atitus.currency_service.services.CurrencyConversionService;

@RestController
@RequestMapping("currency")
public class CurrencyController {

        private final CurrencyConversionService conversionService;

        public CurrencyController(CurrencyConversionService conversionService) {
                this.conversionService = conversionService;
        }

        @GetMapping("/{value}/{source}/{target}")
        public ResponseEntity<CurrencyEntity> obterConversao(
                        @PathVariable double value,
                        @PathVariable String source,
                        @PathVariable String target) {

                try {
                        CurrencyEntity currency = conversionService.obterConversao(value, source, target);
                        return ResponseEntity.ok(currency);
                } catch (CurrencyNotFoundException ex) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
                }

        }


	

}
