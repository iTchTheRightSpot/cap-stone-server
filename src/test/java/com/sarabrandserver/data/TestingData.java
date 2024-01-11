package com.sarabrandserver.data;

import com.github.javafaker.Faker;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.product.dto.*;
import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import static com.sarabrandserver.enumeration.RoleEnum.CLIENT;
import static com.sarabrandserver.enumeration.RoleEnum.WORKER;

public class TestingData {

    public static SarreBrandUser client() {
        var client = SarreBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .build();
        client.setClientRole(Set.of(new ClientRole(CLIENT, client)));
        return client;
    }

    public static SarreBrandUser worker() {
        var client = SarreBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .build();
        client.setClientRole(
                Set.of(new ClientRole(CLIENT, client), new ClientRole(WORKER, client))
        );
        return client;
    }

    @NotNull
    public static SizeInventoryDTO[] sizeInventoryDTOArray(int size) {
        SizeInventoryDTO[] dto = new SizeInventoryDTO[size];

        for (int i = 0; i < size; i++) {
            dto[i] = new SizeInventoryDTO(new Faker().number().randomDigitNotZero(), "tall");
        }
        return dto;
    }

    /**
     * Converts all files from uploads directory into a MockMultipartFile
     * */
    @NotNull
    public static MockMultipartFile[] files() {
        return Arrays.stream(new Path[]{Paths.get("src/test/resources/uploads/benzema.JPG")})
                .map(path -> {
                    String contentType;
                    byte[] content;
                    try {
                        contentType = Files.probeContentType(path);
                        content = Files.readAllBytes(path);
                    } catch (IOException ignored) {
                        contentType = "text/plain";
                        content = new byte[3];
                    }

                    return new MockMultipartFile(
                            "files",
                            path.getFileName().toString(),
                            contentType,
                            content
                    );
                })
                .toArray(MockMultipartFile[]::new);
    }

    @NotNull
    public static CreateProductDTO createProductDTO(long categoryId, SizeInventoryDTO[] dtos) {
        return productDTO(
                categoryId,
                new Faker().commerce().productName(),
                dtos,
                new Faker().commerce().color()
        );
    }

    @NotNull
    public static CreateProductDTO createProductDTO(
            String productName,
            long categoryId,
            SizeInventoryDTO[] dtos
    ) {
        return productDTO(categoryId, productName, dtos, new Faker().commerce().color());
    }

    @NotNull
    public static CreateProductDTO productDTO(
            long categoryId,
            String productName,
            SizeInventoryDTO[] dtos,
            String colour
    ) {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().number().numberBetween(10000, 700000)), "NGN"),
        };

        return new CreateProductDTO(
                categoryId,
                productName,
                new Faker().lorem().fixedString(1000),
                arr,
                true,
                dtos,
                colour
        );
    }

    @NotNull
    public static ProductDetailDTO productDetailDTO(String productID, String colour, SizeInventoryDTO[] dtos) {
        return new ProductDetailDTO(productID, false, colour, dtos);
    }

    @NotNull
    public static UpdateProductDTO updateProductDTO(
            String productID,
            String productName,
            String category,
            long categoryId
    ) {
        return new UpdateProductDTO(
                productID,
                productName,
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                category,
                categoryId
        );
    }

    @NotNull
    public static void dummyProducts(ProductCategory cat, int num, WorkerProductService service) {
        var images = TestingData.files();

        for (int i = 0; i < num; i++) {
            var data = TestingData
                    .productDTO(
                            cat.getCategoryId(),
                            new Faker().commerce().productName() + " " + i,
                            new SizeInventoryDTO[]{
                                    new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "medium"),
                                    new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "small"),
                                    new SizeInventoryDTO(new Faker().number().numberBetween(1, 40), "large")
                            },
                            new Faker().commerce().color() + " " + i
                    );

            service.create(data, images);
        }
    }

}
