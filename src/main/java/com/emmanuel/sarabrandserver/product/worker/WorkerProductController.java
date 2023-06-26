package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.product.dto.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.dto.DetailDTO;
import com.emmanuel.sarabrandserver.product.dto.ProductDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/worker/product")
//@PreAuthorize(value = "hasAnyAuthority('WORKER')")
public class WorkerProductController {
    private final WorkerProductService workerProductService;

    public WorkerProductController(WorkerProductService workerProductService) {
        this.workerProductService = workerProductService;
    }

    /**
     * Method fetches a list of ProductResponse.
     * @param page is the UI page number
     * @param size is the amount in the list
     * @return ResponseEntity of HttpStatus and ProductResponse
     * */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "15") Integer size
    ) {
        return new ResponseEntity<>(this.workerProductService.fetchAll(page, size), HttpStatus.OK);
    }

    /**
     * Method returns a list of DetailResponse.
     * @param name is the name of the product
     * @param page is amount of size based on the page
     * @param size is the amount in the list adn
     * @return ResponseEntity of type HttpStatus and DetailResponse
     * */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> fetchAll(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "15") Integer size
    ) {
        return new ResponseEntity<>(this.workerProductService.fetchAll(name, page, size), HttpStatus.OK);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> create(
            @Valid @ModelAttribute CreateProductDTO dto,
            @RequestParam(name = "files") MultipartFile[] files
    ) {
        return workerProductService.create(dto, files);
    }

    /**
     * Update a Product
     * @param dto of type ProductDTO
     * @return ResponseEntity of type HttpStatus
     * */
    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductDTO dto) {
        return this.workerProductService.updateProduct(dto);
    }

    /**
     * Update a ProductDetail
     * @param dto of type DetailDTO
     * @return ResponseEntity of type HttpStatus
     * */
    @PutMapping(path = "/detail", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProductDetail(@Valid @RequestBody DetailDTO dto) {
        return this.workerProductService.updateProductDetail(dto);
    }

    /**
     * Method permanently deletes a Product
     * @param id is the Product id
     * @return ResponseEntity of type HttpStatus
     * */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteProduct(@NotNull @PathVariable(value = "id") Long id) {
        return this.workerProductService.deleteProduct(id);
    }

    /**
     * Method permanently deletes a ProductDetail
     * @param name is the Product name
     * @param sku is a unique String for each ProductDetail
     * @return ResponseEntity of type HttpStatus
     * */
    @DeleteMapping(path = "/detail/{name}/{sku}")
    public ResponseEntity<?> deleteProductDetail(
       @NotNull @PathVariable(value = "name") String name,
       @NotNull @PathVariable(value = "sku") String sku
    ) {
        return this.workerProductService.deleteProductDetail(name, sku);
    }

}
