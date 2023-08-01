package com.emmanuel.sarabrandserver.product.client;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.response.DetailResponse;
import com.emmanuel.sarabrandserver.product.response.ProductResponse;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ClientProductService {
    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final Environment environment;

    public ClientProductService(ProductRepository productRepository, S3Service s3Service, Environment environment) {
        this.productRepository = productRepository;
        this.s3Service = s3Service;
        this.environment = environment;
    }

    /**
     * Method list all Products when a client clicks on shop all. Query method fetchAllProductsClient validates product
     * visibility property is true and inventory is greater than 0.
     * @param page is the number to display a products
     * @param size is the max amount render on a page
     * @return List of ProductResponse
     * */
    public Page<ProductResponse> fetchAll(int page, int size) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = Optional.ofNullable(this.environment.getProperty("aws.bucket")).orElse("");

        return this.productRepository
                .fetchAllProductsClient(PageRequest.of(page, Math.max(size, 30))) //
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, bucket, pojo.getKey());
                    return ProductResponse.builder()
                            .name(pojo.getName())
                            .desc(pojo.getDesc())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                });
    }

    /**
     * Method list all ProductDetails bases on a Product name. A validation is made to make sure product visibility is
     * true and inventory count is greater than zero.
     * @param name is the name of the product
     * @return List of DetailResponse
     * */
    public List<DetailResponse> fetchAll(String name) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        return this.productRepository.fetchDetailClient(name) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getKey().split(","))
                            .map(key -> this.s3Service.getPreSignedUrl(bool, bucket, key))
                            .toList();

                    return DetailResponse.builder()
                            .sku(pojo.getSku())
                            .size(pojo.getSize())
                            .colour(pojo.getColour())
                            .url(urls)
                            .build();
                }) //
                .toList();
    }

    /**
     * Method list all products based on category clicked. It filters validating visibility property is true and
     * inventory is greater than 0.
     * @param name is category name
     * @param page is the number to display a products
     * @param size is the max amount render on a page
     * @return List of ProductResponse
     * */
    public List<ProductResponse> fetchAll(String name, int page, int size) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        return this.productRepository.fetchByCategoryClient(name, PageRequest.of(page, Math.max(size, 30))) //
                .stream() //
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, bucket, pojo.getKey());
                    return ProductResponse.builder()
                            .name(pojo.getName())
                            .desc(pojo.getDesc())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                }) //
                .toList();
    }

}

