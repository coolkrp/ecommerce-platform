package com.ecommerce.product.kafka;

import com.ecommerce.product.event.ProductEvent;
import com.ecommerce.product.service.ProductSearchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    public ProductEventConsumer(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @KafkaListener(
            topics = "product-events",
            groupId = "product-search-consumer"
    )
    public void consume(ProductEvent event) {

        if (event.getEventType() == null) {
            return;
        }

        switch (event.getEventType()) {

            case PRODUCT_CREATED:
            case PRODUCT_UPDATED:
                productSearchService.indexProduct(event);
                break;

            case PRODUCT_DELETED:
                productSearchService.deleteProduct(event.getProductId());
                break;

            default:
                break;
        }
    }
}