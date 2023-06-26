package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.product.client.ClientProductService;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.projection.ClientProductPojo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class ClientProductServiceTest {
    private ClientProductService clientService;

    @Mock private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        this.clientService = new ClientProductService(this.productRepository);
    }

    @Test
    void fetchAll() {
        // Given
        // When
        // Then
    }

    private List<Product> products() {
        List<Product> list = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            var product = Product.builder()
                    .name(new Faker().commerce().productName())
                    .description(new Faker().lorem().characters(50))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency("USD")
                    .productDetails(new HashSet<>())
                    .build();

            extracted(product);
            extracted(product);
            extracted(product);

            list.add(product);
        }

        return list;
    }

    private static void extracted(Product product) {
        var size = ProductSize.builder()
                .size("medium")
                .productDetails(new HashSet<>())
                .build();
        // ProductInventory
        var inventory = ProductInventory.builder()
                .quantity(new Faker().number().numberBetween(10, 40))
                .productDetails(new HashSet<>())
                .build();
        // ProductImage
        var image0 = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(new Faker().file().fileName())
                .build();

        var image1 = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(new Faker().file().fileName())
                .build();

        var image2 = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(new Faker().file().fileName())
                .build();

        // ProductColour
        var colour = ProductColour.builder()
                .colour(new Faker().color().name())
                .productDetails(new HashSet<>())
                .build();
        // ProductDetail
        var detail = ProductDetail.builder()
                .sku(UUID.randomUUID().toString())
                .isVisible(true)
                .createAt(new Date())
                .modifiedAt(null)
                .productImages(new HashSet<>())
                .build();

        detail.setProductSize(size);
        detail.setProductInventory(inventory);
        detail.setProductColour(colour);
        detail.addImages(image0);
        detail.addImages(image1);
        detail.addImages(image2);
        // Add detail to product
        product.addDetail(detail);
    }

    private record PojoImpl (Product p) implements ClientProductPojo {

        @Override
        public String getName() {
            return p.getName();
        }

        @Override
        public String getDesc() {
            return p.getDescription();
        }

        @Override
        public BigDecimal getPrice() {
            return null;
        }

        @Override
        public String getCurrency() {
            return null;
        }

        @Override
        public String getSku() {
            return null;
        }

        @Override
        public String getSize() {
            return null;
        }

        @Override
        public String getQuantity() {
            return null;
        }

        @Override
        public String getColour() {
            return null;
        }

        @Override
        public List<String> getImage() {
            return null;
        }
    }

}