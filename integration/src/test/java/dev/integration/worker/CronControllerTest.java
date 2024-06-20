package dev.integration.worker;

import dev.integration.CustomRunInitScripts;
import dev.integration.AbstractNative;
import dev.integration.MockRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CronControllerTest extends AbstractNative {

    @Test
    void shouldSuccessfullyTestCronJobMethodNativeMode() throws SQLException {
        String cookie = MockRequest.ADMINCOOKIE(testTemplate, PATH);
        assertNotNull(cookie);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.COOKIE, cookie);

        CustomRunInitScripts.insertDummyOrderReservation("integration", "integration");

        final var get = testTemplate.exchange(
                PATH + "api/v1/cron",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Void.class
        );
        assertEquals(HttpStatusCode.valueOf(200), get.getStatusCode());
    }

}
