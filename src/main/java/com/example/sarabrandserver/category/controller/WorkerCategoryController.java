package com.example.sarabrandserver.category.controller;

import com.example.sarabrandserver.category.dto.CategoryDTO;
import com.example.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.example.sarabrandserver.category.service.WorkerCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/worker/category")
//@PreAuthorize(value = "hasAnyAuthority('WORKER')")
public class WorkerCategoryController {
    private final WorkerCategoryService workerCategoryService;

    public WorkerCategoryController(WorkerCategoryService workerCategoryService) {
        this.workerCategoryService = workerCategoryService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> allCategories() {
        return new ResponseEntity<>(this.workerCategoryService.fetchAll(), HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryDTO dto) {
        return this.workerCategoryService.create(dto);
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateCategoryDTO dto) {
        return this.workerCategoryService.update(dto);
    }

    @DeleteMapping(path = "/{category_id}")
    public ResponseEntity<?> delete(@PathVariable(value = "category_id") String id) {
        return this.workerCategoryService.delete(id);
    }

}
