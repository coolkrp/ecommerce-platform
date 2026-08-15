package com.ecommerce.product.kafka;

import com.ecommerce.product.event.ProductEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventProducer {

    private static final String PRODUCT_EVENTS_TOPIC = "product-events";

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public ProductEventProducer(
            KafkaTemplate<String, ProductEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ProductEvent event) {

        kafkaTemplate.send(
                PRODUCT_EVENTS_TOPIC,
                event.getProductId().toString(),
                event
        );
    }
}