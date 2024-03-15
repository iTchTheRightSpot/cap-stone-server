package dev.capstone.shipping.controller;

import dev.capstone.AbstractIntegration;
import dev.capstone.shipping.ShippingDto;
import dev.capstone.shipping.ShippingMapper;
import dev.capstone.shipping.entity.ShipSetting;
import dev.capstone.shipping.repository.ShippingRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ShippingControllerTest extends AbstractIntegration {

    @Value("${api.endpoint.baseurl}shipping")
    private String path;

    @Autowired
    private ShippingRepo shippingRepo;

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void create() throws Exception {
        this.mockMvc
                .perform(post("/" + path)
                        .with(csrf())
                        .content(this.objectMapper.writeValueAsString(
                                new ShippingDto(
                                        "Canada",
                                        new BigDecimal("10100"),
                                        new BigDecimal("20.24")
                                )
                        ))
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void update() throws Exception {
        var shipping = shippingRepo
                .save(new ShipSetting("Japan", new BigDecimal("25750"), new BigDecimal("35.55")));

        this.mockMvc
                .perform(put("/" + path)
                        .with(csrf())
                        .content(this.objectMapper.writeValueAsString(
                                new ShippingMapper(
                                        shipping.shipId(),
                                        shipping.country(),
                                        new BigDecimal("10100"),
                                        new BigDecimal("20.24")
                                )
                        ))
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteShipping() throws Exception {
        var shipping = shippingRepo
                .save(new ShipSetting("France", new BigDecimal("25750"), new BigDecimal("35.55")));

        this.mockMvc
                .perform(delete("/" + path + "/" + shipping.shipId())
                        .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void deleteDefaultShouldThrowError() throws Exception {
        // given
        var optional = shippingRepo
                .shippingByCountryElseReturnDefault("default");
        assertFalse(optional.isEmpty());

        // then
        this.mockMvc
                .perform(delete("/" + path + "/" + optional.get().shipId())
                        .with(csrf())
                )
                .andExpect(status().isConflict());
    }

}