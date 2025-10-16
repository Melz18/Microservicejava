package br.edu.atitus.product_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;
import br.edu.atitus.product_service.services.ProductCurrencyService;

@RestController
@RequestMapping("products")
public class OpenProductController {

        private final ProductRepository repository;
        private final ProductCurrencyService productCurrencyService;

        public OpenProductController(ProductRepository repository, ProductCurrencyService productCurrencyService) {
                super();
                this.repository = repository;
                this.productCurrencyService = productCurrencyService;
        }
	
	@Value("${server.port}")
	private int serverPort;
	
	@GetMapping("/{idProduct}/{targetCurrency}")
        public ResponseEntity<ProductEntity> obterProduto(
                        @PathVariable Long idProduct,
                        @PathVariable String targetCurrency
                        ) throws Exception {
		
		ProductEntity product = repository.findById(idProduct)
				.orElseThrow(() -> new Exception("Product not found"));
		
		product.setEnviroment("Product-service running on Port: " + serverPort);
		
		if (targetCurrency.equalsIgnoreCase(product.getCurrency()))
			product.setConvertedPrice(product.getPrice());
                else {
                        ProductCurrencyService.ResultadoConversao conversion = productCurrencyService.converterPreco(
                                        product.getId(),
                                        product.getPrice(),
                                        product.getCurrency(),
                                        targetCurrency);
                        product.setConvertedPrice(conversion.convertedValue());
                        product.setEnviroment(product.getEnviroment()
                                        + " - " + conversion.environment());
                }
		
		return ResponseEntity.ok(product);
	}

}
