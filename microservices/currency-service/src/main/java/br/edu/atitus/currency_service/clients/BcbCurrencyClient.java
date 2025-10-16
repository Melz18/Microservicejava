package br.edu.atitus.currency_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.edu.atitus.currency_service.clients.dto.BcbCurrencyResponse;

@FeignClient(name = "bcbCurrencyClient", url = "${currency-service.clients.bcb.url}")
public interface BcbCurrencyClient {

    @GetMapping("/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)")
    BcbCurrencyResponse buscarMoeda(
            @RequestParam("@moeda") String currency,
            @RequestParam("@dataCotacao") String quotationDate,
            @RequestParam("$format") String format);
}
