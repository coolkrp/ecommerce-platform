package com.ecommerce.product.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String sku) {
        super("Product already exists with SKU: " + sku);
    }
}