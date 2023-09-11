package com.emmanuel.sarabrandserver.product.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SizeInventoryDTO {

    @JsonProperty(required = true, value = "qty")
    @NotNull(message = "Please enter or choose product quantity")
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull(message = "Please enter or choose product size")
    @NotEmpty(message = "Please enter or choose product size")
    private String size;

}
