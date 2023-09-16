package com.emmanuel.sarabrandserver.collection.service;

import com.emmanuel.sarabrandserver.category.dto.UpdateCollectionDTO;
import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.response.CollectionResponse;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class WorkerCollectionService {
    private final CollectionRepository collectionRepository;
    private final CustomUtil customUtil;

    public WorkerCollectionService(CollectionRepository collectionRepository, CustomUtil customUtil) {
        this.collectionRepository = collectionRepository;
        this.customUtil = customUtil;
    }

    /** Returns a list of CollectionResponse. */
    public List<CollectionResponse> fetchAllCategories() {
        return this.collectionRepository
                .fetchAllCollection() //
                .stream() //
                .map(pojo -> CollectionResponse.builder()
                        .id(pojo.getUuid())
                        .collection(pojo.getCollection())
                        .created(pojo.getCreated().getTime())
                        .modified(pojo.getModified() == null ? 0L : pojo.getModified().getTime())
                        .visible(pojo.getVisible())
                        .build()
                )
                .toList();
    }

    public Page<ProductResponse> allProductsByCollection(String id, int page, int size) {
        return this.collectionRepository
                .allProductsByCollection(id, PageRequest.of(page, size))
                .map(pojo -> ProductResponse.builder()
                        .id(pojo.getUuid())
                        .name(pojo.getName())
                        .price(pojo.getPrice())
                        .currency(pojo.getCurrency())
                        .imageUrl(pojo.getKey())
                        .build()
                );
    }

    /**
     * Creates a ProductCollection object
     * @param dto of type CollectionDTO
     * @throws DuplicateException if collection name exists
     * */
    public void create(CollectionDTO dto) {
        if (this.collectionRepository.findByName(dto.getName().trim()).isPresent()) {
            throw new DuplicateException(dto.getName() + " exists");
        }

        var date = this.customUtil.toUTC(new Date()).orElseGet(Date::new);
        var collection = ProductCollection.builder()
                .uuid(UUID.randomUUID().toString())
                .collection(dto.getName().trim())
                .createAt(date)
                .modifiedAt(null)
                .isVisible(dto.getVisible())
                .products(new HashSet<>())
                .build();

        this.collectionRepository.save(collection);
    }

    /**
     * Method is responsible for updating a ProductCollection based on uuid.
     * @param dto of type UpdateCollectionDTO
     * @throws DuplicateException when dto.name exists, and it is not associated to uuid
     * */
    @Transactional
    public void update(UpdateCollectionDTO dto) {
        boolean bool = this.collectionRepository
                .duplicateCategoryForUpdate(dto.getId().trim(), dto.getName().trim()) > 0;

        if (bool) {
            throw new DuplicateException(dto.getName() + " is a duplicate");
        }

        var date = this.customUtil.toUTC(new Date()).orElseGet(Date::new);

        this.collectionRepository.update(date, dto.getName(), dto.getVisible(), dto.getId().trim());
    }

    // Called in WorkerProductService
    public ProductCollection findByName(String name) {
        return this.collectionRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    public ProductCollection findByUuid(String uuid) {
        return this.collectionRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException("ProductCollection does not exist"));
    }

}
