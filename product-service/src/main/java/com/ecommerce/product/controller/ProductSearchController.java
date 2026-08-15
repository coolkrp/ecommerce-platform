package com.ecommerce.product.controller;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.service.ProductSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products/search")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex() {

        int count = productSearchService.indexAllProducts();

        return ResponseEntity.ok(
                Map.of(
                        "message", "Products indexed successfully",
                        "count", count
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam String q) {

        return ResponseEntity.ok(
                productSearchService.search(q)
        );
    }
}