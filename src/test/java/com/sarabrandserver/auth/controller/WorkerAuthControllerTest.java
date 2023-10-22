package com.sarabrandserver.auth.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.user.repository.UserRoleRepository;
import com.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerAuthControllerTest extends AbstractIntegrationTest {
    private final String PRINCIPAL = "SEJU@development.com";
    private final String PASSWORD = "123#-SEJU-Development";
    private final String requestMapping = "/api/v1/worker/auth/";

    @Value(value = "${server.servlet.session.cookie.name}") private String JSESSIONID;

    @Autowired private AuthService authService;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var dto = new RegisterDTO(
                "SEJU",
                "Development",
                PRINCIPAL,
                "",
                "000-000-0000",
                PASSWORD
        );
        this.authService.workerRegister(dto);
    }

    @AfterEach
    void tearDown() {
        this.userRoleRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    /**
     * Method does two things in one. Login and Register. To register, worker has to have a role WORKER
     */
    @Test
    @Order(1)
    void register() throws Exception {
        // Login
        MvcResult login = this.MOCKMVC
                .perform(post(requestMapping + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDTO(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        // Register
        var dto = new RegisterDTO(
                "James",
                "james@james.com",
                "james@james.com",
                "james development",
                "0000000000",
                "A;D@#$13245eifdkj"
        );

        this.MOCKMVC
                .perform(post(requestMapping + "register")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(dto))
                        .cookie(login.getResponse().getCookie(JSESSIONID))
                )
                .andExpect(status().isCreated());
    }

    /**
     * Simulates registering an existing worker
     */
    @Test
    @Order(2)
    void register_with_existing_credentials() throws Exception {
        // Login
        MvcResult login = this.MOCKMVC
                .perform(post(requestMapping + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDTO(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        var dto = new RegisterDTO(
                "SEJU",
                "Development",
                PRINCIPAL,
                "",
                "00-000-0000",
                PASSWORD
        );

        this.MOCKMVC
                .perform(post(requestMapping + "register")
                        .contentType(APPLICATION_JSON)
                        .with(csrf())
                        .content(this.MAPPER.writeValueAsString(dto))
                        .cookie(login.getResponse().getCookie(JSESSIONID))
                )
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateException));
    }

    @Test
    @Order(3)
    void login_wrong_password() throws Exception {
        String payload = this.MAPPER
                .writeValueAsString(new LoginDTO(PRINCIPAL, "fFeubfrom@#$%^124234"));
        this.MOCKMVC
                .perform(post(requestMapping + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(payload)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    /**
     * Validates cookie has been clear. But cookie will still be valid if it due to jwt being stateless
     */
    @Test
    @Order(4)
    void logout() throws Exception {
        // Login
        MvcResult login = this.MOCKMVC
                .perform(post(requestMapping + "login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.MAPPER.writeValueAsString(new LoginDTO(PRINCIPAL, PASSWORD)))
                )
                .andExpect(status().isOk())
                .andReturn();

        // Jwt Cookie
        Cookie cookie = login.getResponse().getCookie(JSESSIONID);
        assertNotNull(cookie);

        // Logout
        MvcResult logout = this.MOCKMVC
                .perform(post("/api/v1/logout").cookie(cookie).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        cookie = logout.getResponse().getCookie(JSESSIONID); // This should be empty

        // Access protected route with invalid cookie
        this.MOCKMVC
                .perform(get("/test/worker").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Full authentication is required to access this resource")
                );
    }

}