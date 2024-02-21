package com.sarabrandserver.category.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.projection.ProductPojo;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository repository;
    private final S3Service service;

    /**
     * Returns a list of {@code CategoryResponse}
     * with is_visible marked as true.
     * */
    public List<CategoryResponse> allCategories() {
        var list = this.repository
                .allCategories()
                .stream()
                .filter(CategoryPojo::statusImpl)
                .map(p -> new CategoryResponse(p.getId(), p.getParent(), p.getName(), p.statusImpl()))
                .toList();

        return CustomUtil.createCategoryHierarchy(list);
    }

    /**
     * Asynchronously retrieves a {@link Page} of
     * {@link ProductResponse} objects associated with a
     * specific category.
     *
     * @param currency    The currency in which prices are displayed.
     * @param categoryId  The primary key of a {@link ProductCategory}.
     * @param page        The page number for pagination.
     * @param size        The page size for pagination.
     * @return A {@link CompletableFuture} representing a {@link Page}
     * of {@link ProductResponse}.
     */
    public CompletableFuture<Page<ProductResponse>> allProductsByCategoryId(
            SarreCurrency currency,
            long categoryId,
            int page,
            int size
    ) {
        Page<ProductPojo> dbRes = this.repository
                .allProductsByCategoryIdWhereInStockAndIsVisible(categoryId, currency, PageRequest.of(page, size));

        List<Supplier<ProductResponse>> futures = createTasks(dbRes);

        return CustomUtil.asynchronousTasks(futures)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        dbRes.getPageable(),
                        dbRes.getTotalElements()
                ));
    }

    private List<Supplier<ProductResponse>> createTasks(Page<ProductPojo> dbRes) {
        List<Supplier<ProductResponse>> futures = new ArrayList<>();

        for (ProductPojo p : dbRes) {
            futures.add(() -> {
                var url = service.preSignedUrl(BUCKET, p.getImage());
                return new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCurrency(),
                        url
                );
            });
        }
        return futures;
    }

}
