package dev.integration;

import com.github.javafaker.Faker;
import dev.webserver.auth.dto.LoginDto;
import dev.webserver.cart.response.CartResponse;
import dev.webserver.product.dto.CreateProductDTO;
import dev.webserver.product.dto.PriceCurrencyDto;
import dev.webserver.product.dto.SizeInventoryDTO;
import dev.webserver.product.dto.UpdateProductDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.math.BigDecimal;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestData {

    @NotNull
    public static SizeInventoryDTO[] sizeInventoryDTOArray(int size) {
        SizeInventoryDTO[] dto = new SizeInventoryDTO[size];
        for (int i = 0; i < size; i++) {
            dto[i] = new SizeInventoryDTO(new Faker().number().randomDigitNotZero() + 1, "tall " + i);
        }
        return dto;
    }

    /**
     * Just like creating a {@link MockMultipartFile} for {@link MockMvc}, this achieves the same thing
     * but for {@link WebTestClient}.
     *
     * @param dto application/json to send the server.
     * @return a {@link MultiValueMap} where the key is the parameter the server accepts.
     * @see <a href="https://github.com/spring-projects/spring-framework/issues/20666">github issue</a>
     * */
    @NotNull
    public static MultiValueMap<String, Object> mockMultiPart(String dto) {
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();

        // create file
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        multipart.add("files", new HttpEntity<>(resources(), headers));

        // create dto
        HttpHeaders metadataHeaders = new HttpHeaders();
        metadataHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        multipart.add("dto", new HttpEntity<>(dto, metadataHeaders));

        return multipart;
    }

    @NotNull
    private static Resource[] resources() {
        Path path = Paths.get("src/test/resources/uploads/");

        assertTrue(Files.exists(path));

        File dir = new File(path.toUri());
        assertNotNull(dir);

        File[] files = dir.listFiles();
        assertNotNull(files);

        Resource[] resources = new FileSystemResource[files.length];

        for (int i = 0; i < files.length; i++) {
            resources[i] = new FileSystemResource(files[i]);
        }

        return resources;
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
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().number().numberBetween(10000, 700000)), "NGN"),
        };

        return new CreateProductDTO(
                categoryId,
                productName,
                new Faker().lorem().fixedString(1000),
                new Faker().number().randomDouble(5, 0, 50),
                arr,
                true,
                dtos,
                colour
        );
    }

    public static String CARTCOOKIE(TestRestTemplate restTemplate, String PATH) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var get = restTemplate.exchange(
                PATH + "api/v1/cart",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<CartResponse>>() {}
        );

        var cookies = get.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return cookies.stream()
                .filter(cookie -> cookie.startsWith("CARTCOOKIE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cart cookie is empty"));
    }

    public static String ADMINCOOKIE(TestRestTemplate restTemplate, String PATH) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var post = restTemplate.postForEntity(
                PATH + "api/v1/worker/auth/login",
                new HttpEntity<>(new LoginDto("admin@admin.com", "password123"), headers),
                Void.class
        );

        var cookies = post.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies == null || cookies.isEmpty())
            throw new RuntimeException("admin cookie is empty");

        return cookies.stream()
                .filter(cookie -> cookie.startsWith("JSESSIONID"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("admin cookie is empty"));
    }

    @NotNull
    public static UpdateProductDTO updateProductDTO(
            String productID,
            String productName,
            long categoryId
    ) {
        return new UpdateProductDTO(
                productID,
                productName,
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                categoryId,
                new Faker().number().randomDouble(5, 100, 100)
        );
    }

    @NotNull
    public static ResponseCookie toCookie(String cookie) {
        return HttpCookie.parse(cookie)
                .stream()
                .findFirst()
                .map(httpCookie -> ResponseCookie
                        .from(httpCookie.getName(), httpCookie.getValue())
                        .domain(httpCookie.getDomain())
                        .path(httpCookie.getPath())
                        .maxAge(httpCookie.getMaxAge())
                        .httpOnly(httpCookie.getSecure())
                        .secure(httpCookie.getSecure())
                        .sameSite("lax")
                        .build()
                )
                .orElseThrow();
    }

}
