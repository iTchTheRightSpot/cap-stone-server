package com.sarabrandserver.product.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.RepositoryTestData;
import com.sarabrandserver.product.projection.PriceCurrencyPojo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class PriceCurrencyRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private PriceCurrencyRepo currencyRepo;

    @Test
    void priceCurrencyByProductUUIDAndCurrency() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        var optional = currencyRepo
                .priceCurrencyByProductUuidAndCurrency(products.getFirst().getUuid(), NGN);
        assertFalse(optional.isEmpty());

        PriceCurrencyPojo pojo = optional.get();
        assertNotNull(pojo.getName());
        assertNotNull(pojo.getDescription());
        assertNotNull(pojo.getCurrency());
        assertNotNull(pojo.getPrice());
    }

    @Test
    void updateProductPriceByProductUuidAndCurrency() {
        // given
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var products = productRepo.findAll();
        assertFalse(products.isEmpty());

        // then
        currencyRepo
                .updateProductPriceByProductUuidAndCurrency(
                        products.getFirst().getUuid(),
                        new BigDecimal("10.52"),
                        USD
                );

        var optional = currencyRepo
                .priceCurrencyByProductUuidAndCurrency(products.getFirst().getUuid(), USD);
        assertFalse(optional.isEmpty());
        assertNotNull(optional.get().getPrice());
        assertEquals(new BigDecimal("10.52"), optional.get().getPrice());
    }

}