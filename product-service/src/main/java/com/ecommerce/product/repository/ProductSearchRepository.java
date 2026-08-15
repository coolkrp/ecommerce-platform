package com.ecommerce.product.repository;

import com.ecommerce.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, Long> {
}