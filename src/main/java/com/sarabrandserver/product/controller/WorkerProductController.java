package com.sarabrandserver.product.controller;

import com.sarabrandserver.product.service.WorkerProductService;
import com.sarabrandserver.product.dto.CreateProductDTO;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.product.dto.UpdateProductDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("api/v1/worker/product")
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
@RequiredArgsConstructor
public class WorkerProductController {

    private final WorkerProductService workerProductService;

    /**
     * Method fetches a list of ProductResponse.
     *
     * @param page is the UI page number
     * @param size is the amount in the list
     * @return ResponseEntity
     */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<ProductResponse> allProducts(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "30") Integer size
    ) {
        return this.workerProductService.fetchAll(page, Math.min(size, 30));
    }

    /**
     * Create a new Product
     */
    @ResponseStatus(CREATED)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public void create(
            @Valid @RequestPart CreateProductDTO dto,
            @NotNull @RequestPart MultipartFile[] files
    ) {
        this.workerProductService.create(dto, files);
    }

    /**
     * Update a Product
     *
     * @param dto of type UpdateProductDTO
     */
    @ResponseStatus(NO_CONTENT)
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public void update(@Valid @RequestBody UpdateProductDTO dto) {
        this.workerProductService.update(dto);
    }

    /**
     * Method permanently deletes a Product
     *
     * @param uuid is the Product uuid
     */
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    public void delete(@NotNull @RequestParam(value = "id") String uuid) {
        this.workerProductService.delete(uuid.trim());
    }

}