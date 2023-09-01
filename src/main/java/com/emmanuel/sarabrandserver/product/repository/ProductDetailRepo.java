package com.emmanuel.sarabrandserver.product.repository;

import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.projection.DetailPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductDetailRepo extends JpaRepository<ProductDetail, Long> {

    /**
     * Update a ProductDetail and ProductSku using native MySQL query as you can update multiple
     * tables in jpql without writing a lot of code
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE product_sku s
            INNER JOIN product_detail d ON d.detail_id = s.detail_id
            SET d.is_visible = :visible, s.inventory = :qty, s.size = :s
            WHERE s.sku = :sku
            """,
            nativeQuery = true
    )
    void updateProductDetail(
            @Param(value = "sku") String sku,
            @Param(value = "visible") boolean visible,
            @Param(value = "qty") int qty,
            @Param(value = "s") String size
    );

    /**
     * Query retrieves all ProductDetails associated to a Product by its uuid.
     * It filters and maps using Spring Data Projection.
     * The magic is since ProductDetail has a 1 to many relationship with
     * ProductImage and ProductSKU, we are getting all ProductImages keys (comma separated).
     * These keys we use to retrieve pre-assigned urls from s3.
     * Finally, for ProductSku we retrieve an array of distinct custom objects.
     * NOTE: this method is similar to findProductDetailsByProductUuidWorker only it
     * filters by ProductDetail being visible
     */
    @Query(value = """
            SELECT
            d.is_visible AS visible,
            d.colour AS colour,
            GROUP_CONCAT(DISTINCT i.image_key) AS image,
            CONCAT('[',
                GROUP_CONCAT(DISTINCT JSON_OBJECT('sku', s.sku, 'inventory', s.inventory, 'size', s.size)),
            ']') AS variants
            FROM product_detail d
            INNER JOIN product_image i ON d.detail_id = i.detail_id
            INNER JOIN product p ON d.product_id = p.product_id
            INNER JOIN product_sku s ON d.detail_id = s.detail_id
            WHERE p.uuid = :uuid AND d.is_visible = true
            GROUP BY d.colour
            """, nativeQuery = true)
    List<DetailPojo> fetchProductDetailByUUIDClient(@Param(value = "uuid") String uuid);

    @Query(value = """
            SELECT COUNT(p.uuid)
            FROM Product p
            INNER JOIN ProductDetail det ON p.productId = det.product.productId
            WHERE p.uuid = :uuid AND det.colour = :colour
            """)
    int colourExist(@Param(value = "uuid") String uuid, @Param(value = "colour") String colour);

    @Query(value = """
            SELECT
            d.is_visible AS visible,
            d.colour AS colour,
            GROUP_CONCAT(DISTINCT i.image_key) AS image,
            CONCAT('[',
                GROUP_CONCAT(DISTINCT JSON_OBJECT('sku', s.sku, 'inventory', s.inventory, 'size', s.size)),
            ']') AS variants
            FROM product_detail d
            INNER JOIN product_image i ON d.detail_id = i.detail_id
            INNER JOIN product p ON d.product_id = p.product_id
            INNER JOIN product_sku s ON d.detail_id = s.detail_id
            WHERE p.uuid = :uuid
            GROUP BY d.is_visible, d.colour
            """, nativeQuery = true)
    List<DetailPojo> findProductDetailsByProductUuidWorker(@Param(value = "uuid") String uuid);

}
