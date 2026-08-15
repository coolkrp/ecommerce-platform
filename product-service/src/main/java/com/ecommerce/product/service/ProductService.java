package com.ecommerce.product.service;

import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.ProductEvent;
import com.ecommerce.product.event.ProductEventType;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.kafka.ProductEventProducer;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSearchService productSearchService;
    private final ProductEventProducer productEventProducer;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
        ProductSearchService productSearchService,
        ProductEventProducer productEventProducer) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSearchService = productSearchService;
        this.productEventProducer = productEventProducer;
    }

    public ProductResponse create(ProductRequest request) {

        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException(request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(request.getCategoryId()));

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        product.setActive(
                request.getActive() == null || request.getActive()
        );

        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product savedProduct = productRepository.save(product);

        productEventProducer.publish(
            createProductEvent(
                savedProduct,
                ProductEventType.PRODUCT_CREATED
            )
        );

        return toResponse(savedProduct);
    }

    public List<ProductResponse> getAll() {

        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (!product.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {

            throw new ProductAlreadyExistsException(request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        product.setUpdatedAt(Instant.now());

        Product updatedProduct = productRepository.save(product);

        productEventProducer.publish(
            createProductEvent(
                updatedProduct,
                ProductEventType.PRODUCT_UPDATED
            )
        );

        return toResponse(updatedProduct);
    }

    public void delete(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);

        productEventProducer.publish(
            createProductEvent(
                product,
                ProductEventType.PRODUCT_DELETED
            )
        );
    }

    private ProductResponse toResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.isActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }

    public List<ProductResponse> getByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
            .orElseThrow(() ->
                    new CategoryNotFoundException(categoryId));

    return productRepository.findByCategoryId(categoryId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private ProductEvent createProductEvent(
        Product product,
        ProductEventType eventType) {

        Long categoryId = product.getCategory() != null
            ? product.getCategory().getId()
            : null;

        return new ProductEvent(
            eventType,
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getSku(),
            product.getPrice(),
            product.getStockQuantity(),
            categoryId,
            product.getImageUrl(),
            product.isActive()
        );
    }
}