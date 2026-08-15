package com.ecommerce.product.mapper;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.entity.Product;

public final class ProductDocumentMapper {

    private ProductDocumentMapper() {
    }

    public static ProductDocument toDocument(Product product) {

        return new ProductDocument(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory().getId(),
                product.getImageUrl(),
                product.isActive()
        );
    }
}