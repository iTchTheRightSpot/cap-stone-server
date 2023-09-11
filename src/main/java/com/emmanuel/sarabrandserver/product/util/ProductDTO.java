package com.emmanuel.sarabrandserver.product.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductDTO implements Serializable {

    @JsonProperty(value = "id")
    @NotNull(message = "cannot be empty")
    private String uuid;

    @JsonProperty(value = "name")
    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String name;

    @Size(max = 400, message = "Max of 255")
    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String desc;

    @NotNull(message = "cannot be empty")
    private BigDecimal price;

    @NotNull(message = "cannot be empty")
    private String category;

    @NotNull(message = "cannot be empty")
    private String collection;

}
