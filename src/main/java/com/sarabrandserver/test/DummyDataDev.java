package com.sarabrandserver.test;

import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.collection.entity.ProductCollection;
import com.sarabrandserver.collection.repository.CollectionRepository;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Profile(value = {"dev"})
public class DummyDataDev {

    @Bean
    public CommandLineRunner runner(
            CategoryRepository categoryRepo,
            CollectionRepository collRepo,
            ProductRepo productRepo,
            ProductDetailRepo detailRepo
    ) {
        return args -> {
//            extracted(categoryRepo, collRepo, productRepo, detailRepo);
        };
    }

    private static void extracted(
            CategoryRepository categoryRepo,
            CollectionRepository collRepo,
            ProductRepo productRepo,
            ProductDetailRepo detailRepo
    ) {
        categoryRepo.deleteAll();
        collRepo.deleteAll();
        detailRepo.deleteAll();
        productRepo.deleteAll();

        Set<String> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String s : set) {
            var category = ProductCategory.builder()
                    .uuid(UUID.randomUUID().toString())
                    .categoryName(s)
                    .createAt(new Date())
                    .isVisible(true)
                    .productCategories(new HashSet<>())
                    .build();
            categoryRepo.save(category);
        }

        set.clear();

        for (int i = 0; i < 5; i++) {
            set.add(new Faker().commerce().department());
        }

        for (String s : set) {
            var collection = ProductCollection.builder()
                    .uuid(UUID.randomUUID().toString())
                    .collection(s)
                    .isVisible(true)
                    .createAt(new Date())
                    .modifiedAt(null)
                    .products(new HashSet<>())
                    .build();
            collRepo.save(collection);
        }

    }

}