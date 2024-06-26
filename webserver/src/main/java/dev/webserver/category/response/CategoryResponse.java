package dev.webserver.category.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.category.projection.CategoryPojo;

import java.util.ArrayList;
import java.util.List;

public record CategoryResponse(
        @JsonProperty(value = "category_id")
        long categoryId,
        @JsonProperty(value = "parent_id")
        Long parentId,
        String name,
        boolean visible,
        List<CategoryResponse> children
) {

    public CategoryResponse(String name) {
        this(-1, -1L, name);
    }

    public CategoryResponse(long categoryId, Long parentId, String name, boolean visible) {
        this(categoryId, parentId, name, visible, new ArrayList<>());
    }

    public CategoryResponse(long categoryId, Long parentId, String name) {
        this(categoryId, parentId, name, false);
    }

    public void addToChildren(CategoryResponse child) {
        children.add(child);
    }

    public static CategoryResponse workerList(CategoryPojo p) {
        return new CategoryResponse(p.getId(), p.getParent(), p.getName(), p.statusImpl(), null);
    }

}
