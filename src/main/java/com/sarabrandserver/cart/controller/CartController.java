package com.sarabrandserver.cart.controller;

import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.cart.service.CartService;
import com.sarabrandserver.enumeration.SarreCurrency;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<CartResponse> cartItems(
            @RequestParam(name = "currency", defaultValue = "ngn") String currency,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        SarreCurrency s = SarreCurrency.valueOf(currency.toUpperCase());
        return this.cartService.cartItems(s, req, res);
    }

    @ResponseStatus(CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void create(@Valid @RequestBody CartDTO dto, HttpServletRequest req) {
        cartService.create(dto, req);
    }

    @ResponseStatus(OK)
    @DeleteMapping
    public void deleteItem(@NotNull @RequestParam(name = "sku") String sku, HttpServletRequest req) {
        this.cartService.deleteFromCart(req, sku);
    }

}
