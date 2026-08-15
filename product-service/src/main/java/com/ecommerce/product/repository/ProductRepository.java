package com.ecommerce.product.repository;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.entity.Product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = "category")
    List<Product> findAll();

    @EntityGraph(attributePaths = "category")
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryId(Long categoryId);

    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.category
        WHERE p.id IN :ids
        """)
    List<Product> findAllByIdsWithCategory(@Param("ids") List<Long> ids);
}