package com.ecommerce.product.service;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.mapper.ProductDocumentMapper;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.ProductSearchRepository;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchService(
        ProductRepository productRepository,
        ProductSearchRepository productSearchRepository,
        ElasticsearchOperations elasticsearchOperations) {

        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Transactional(readOnly = true)
    public void indexProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + productId
                        ));

        ProductDocument document =
                ProductDocumentMapper.toDocument(product);

        productSearchRepository.save(document);
    }

    @Transactional(readOnly = true)
    public int indexAllProducts() {

        List<Product> products = productRepository.findAll();

        List<ProductDocument> documents = products.stream()
            .map(ProductDocumentMapper::toDocument)
            .toList();

        productSearchRepository.saveAll(documents);

        return documents.size();
    }

    public List<ProductResponse> search(String query) {

    if (query == null || query.isBlank()) {
        return List.of();
    }

    NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(q -> q.multiMatch(m -> m
                    .query(query.trim())
                    .fields("name", "description", "sku")
            ))
            .build();

    SearchHits<ProductDocument> searchHits =
            elasticsearchOperations.search(
                    nativeQuery,
                    ProductDocument.class
            );

    List<Long> productIds = searchHits.getSearchHits()
            .stream()
            .map(hit -> hit.getContent().getId())
            .toList();
    
        if (productIds.isEmpty()) {
            return List.of();
        }

        return productRepository.findAllByIdsWithCategory(productIds)
        .stream()
        .map(this::toResponse)
        .toList();
    }

    private ProductResponse toResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        response.setImageUrl(product.getImageUrl());
        response.setActive(product.isActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }

    public void deleteProduct(Long productId) {
        productSearchRepository.deleteById(productId);
    }
}