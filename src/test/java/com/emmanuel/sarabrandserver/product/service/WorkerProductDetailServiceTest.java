package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.AbstractUnitTest;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.ProductDetailDTO;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import com.emmanuel.sarabrandserver.util.TestingData;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkerProductDetailServiceTest extends AbstractUnitTest {

    @Value(value = "${aws.bucket}") private String BUCKET;

    @Value(value = "${spring.profiles.active}") private String ACTIVEPROFILE;

    private WorkerProductDetailService productDetailService;
    @Mock private ProductRepository productRepository;
    @Mock private ProductSKUService productSKUService;
    @Mock private ProductImageRepo productImageRepo;
    @Mock private ProductDetailRepo detailRepo;
    @Mock private CustomUtil customUtil;
    @Mock private HelperService helperService;

    @BeforeEach
    void setUp() {
        this.productDetailService = new WorkerProductDetailService(
                this.detailRepo,
                this.productSKUService,
                this.productImageRepo,
                this.productRepository,
                this.customUtil,
                this.helperService
        );
        this.productDetailService.setACTIVEPROFILE(ACTIVEPROFILE);
        this.productDetailService.setBUCKET(BUCKET);
    }

    @Test
    @DisplayName(value = "Create a new ProductDetail.")
    void create() {
        // Given
        var si = TestingData.sizeInventoryDTOArray(4);
        var files = TestingData.files(3);
        var product = Product.builder().uuid("product uuid").build();
        var dto = ProductDetailDTO.builder()
                .uuid(product.getUuid())
                .visible(true)
                .colour(new Faker().commerce().color())
                .sizeInventory(si)
                .files(files)
                .build();

        // When
        when(productRepository.findByProductUuid(anyString())).thenReturn(Optional.of(product));
        when(detailRepo.productDetailByColour(anyString())).thenReturn(Optional.empty());
        when(customUtil.toUTC(any(Date.class))).thenReturn(Optional.of(new Date()));

        // Then
        productDetailService.create(dto, files);
        verify(this.detailRepo, times(1)).save(any(ProductDetail.class));
    }

    @Test
    @DisplayName(value = "Create a new ProductDetail. Colour exists")
    void createE() {
        // Given
        var si = TestingData.sizeInventoryDTOArray(4);
        var files = TestingData.files(3);
        var product = Product.builder().uuid("product uuid").build();
        var detail = ProductDetail.builder().colour(new Faker().commerce().color()).build();
        var dto = ProductDetailDTO.builder()
                .uuid(product.getUuid())
                .visible(true)
                .colour(detail.getColour())
                .sizeInventory(si)
                .files(files)
                .build();

        // When
        when(productRepository.findByProductUuid(anyString())).thenReturn(Optional.of(product));
        when(detailRepo.productDetailByColour(anyString())).thenReturn(Optional.of(detail));

        // Then
        productDetailService.create(dto, files);
        verify(this.detailRepo, times(0)).save(any(ProductDetail.class));
    }

}