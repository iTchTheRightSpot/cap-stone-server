package com.sarabrandserver.product.service;

import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductImageRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.dto.ProductDetailDTO;
import com.sarabrandserver.product.dto.UpdateProductDetailDTO;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.*;

@Service
@RequiredArgsConstructor
@Setter
public class WorkerProductDetailService {

    @Value(value = "${aws.bucket}") private String BUCKET;
    @Value(value = "${spring.profiles.active}") private String ACTIVEPROFILE;

    private final ProductDetailRepo detailRepo;
    private final ProductSKUService productSKUService;
    private final ProductImageRepo productImageRepo;
    private final ProductRepo productRepo;
    private final CustomUtil customUtil;
    private final HelperService helperService;

    /**
     * Returns a List of ProductDetail based on Product uuid
     *
     * @param uuid is the uuid of the product
     * @return List of DetailResponse
     */
    public List<DetailResponse> fetch(String uuid) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        return this.detailRepo
                .findProductDetailsByProductUuidWorker(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays
                            .stream(pojo.getImage().split(","))
                            .map(key -> this.helperService.preSignedURL(bool, BUCKET, key))
                            .toList();

                    Variant[] variants = this.customUtil
                            .toVariantArray(pojo.getVariants(), WorkerProductDetailService.class);

                    return DetailResponse.builder()
                            .isVisible(pojo.getVisible())
                            .colour(pojo.getColour())
                            .url(urls)
                            .variants(variants)
                            .build();
                })
                .toList();
    }

    /**
     * Create new ProductDetail
     *
     * @param dto of type ProductDetailDTO
     * @throws CustomNotFoundException is thrown if product uuid does not exist
     * @throws DuplicateException      is thrown if product colour exists
     */
    @Transactional
    public void create(ProductDetailDTO dto, MultipartFile[] multipartFiles) {
        var product = this.productRepo
                .findByProductUuid(dto.uuid())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        Optional<ProductDetail> exist = this.detailRepo.productDetailByColour(dto.colour());

        if (exist.isPresent()) {
            // Create new ProductSKU
            var detail = exist.get();
            this.productSKUService.save(dto.sizeInventory(), detail);
            return;
        }

        var date = this.customUtil.toUTC(new Date());

        // Validate MultipartFile[] are all images
        var files = this.helperService.customMultiPartFiles(multipartFiles, new StringBuilder());

        // Save ProductDetail
        var detail = ProductDetail.builder()
                .product(product)
                .colour(dto.colour())
                .createAt(date)
                .isVisible(dto.visible())
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        var saved = this.detailRepo.save(detail);

        // Save ProductSKUs
        this.productSKUService.save(dto.sizeInventory(), saved);

        // Save ProductImages (save to s3)
        this.helperService.productImages(
                detail,
                files,
                this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage"),
                BUCKET
        );
    }

    /**
     * Updates a ProductDetail based on ProductSKU sku
     *
     * @param dto of type DetailDTO
     */
    @Transactional
    public void update(final UpdateProductDetailDTO dto) {
        this.detailRepo.updateProductDetail(
                dto.sku(),
                dto.colour(),
                dto.isVisible(),
                dto.qty(),
                dto.size()
        );
    }

    /**
     * Permanently deletes ProductDetail and its relationship with ProductImages and ProductSKU
     *
     * @param sku is ProductSKU property. ProductSKU has a many to 1 relationship with ProductDetail
     * @throws CustomNotFoundException is thrown when sku does not exist
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public void delete(final String sku) {
        var detail = productDetailByProductSku(sku);

        if (this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage")) {
            var images = this.productImageRepo.imagesByProductDetailID(detail.getProductDetailId());

            List<ObjectIdentifier> keys = images //
                    .stream() //
                    .map(image -> ObjectIdentifier.builder().key(image.getImageKey()).build())
                    .toList();

            if (!keys.isEmpty()) {
                this.helperService.deleteFromS3(keys, BUCKET);
            }
        }

        // Remove detail from Product and Save Product
        this.detailRepo.delete(detail);
    }

    /**
     * Save ProductDetail
     */
    public ProductDetail productDetail(
            final Product product,
            final String colour,
            final boolean visible,
            final Date date
    ) {
        var detail = ProductDetail.builder()
                .product(product)
                .colour(colour)
                .createAt(date)
                .isVisible(visible)
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        return this.detailRepo.save(detail);
    }

    /**
     * Find ProductDetail by sku
     */
    public ProductDetail productDetailByProductSku(final String sku) {
        return this.detailRepo
                .findDetailBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

}