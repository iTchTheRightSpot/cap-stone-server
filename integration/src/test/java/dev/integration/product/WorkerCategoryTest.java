package dev.integration.product;

import dev.integration.MainTest;
import dev.webserver.category.dto.CategoryDTO;
import dev.webserver.category.dto.UpdateCategoryDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
class WorkerCategoryTest extends MainTest {

    @BeforeAll
    static void before() {
        assertNotNull(COOKIE);
    }

    @Test
    void shouldSuccessfullyRetrieveACategory() {
        testClient.get()
                .uri("/api/v1/worker/category")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldSuccessfullyRetrieveProductsBaseOnCategory() {
        testClient.get()
                .uri("/api/v1/worker/category/products?category_id=1")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldSuccessfullyCreateACategory() {
        testClient.post()
                .uri("/api/v1/worker/category")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters
                        .fromValue(new CategoryDTO("worker-cat", true, null)))
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void shouldSuccessfullyUpdateACategory() {
        testClient.put()
                .uri("/api/v1/worker/category")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        new UpdateCategoryDTO(1L, null, "frank", false)))
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void shouldSuccessfullyDeleteACategory() {
        testClient.delete()
                .uri("/api/v1/worker/category/1")
                .cookie("JSESSIONID", COOKIE.getValue())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
