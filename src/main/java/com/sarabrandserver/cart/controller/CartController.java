package com.sarabrandserver.cart.controller;


import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "api/v1/client/cart")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_CLIENT')")
public class CartController {

    private final CartService cartService;

    @ResponseStatus(OK)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    void create(@Valid @RequestBody CartDTO dto) {
        cartService.create(dto);
    }

}