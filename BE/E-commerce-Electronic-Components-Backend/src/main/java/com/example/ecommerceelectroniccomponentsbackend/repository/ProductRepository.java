package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    // Tìm sản phẩm theo tên danh mục
    @Query("select distinct p from Product p join p.categories c where LOWER(c.name) like lower(concat('%', :categoryName, '%') ) ")
    List<Product> findByCategoryNameContaining(@Param("categoryName") String categoryName);

    // Tìm sản phẩm theo ID danh mục
    @Query("select distinct p from Product p join p.categories c where c.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    // Tìm sản phẩm theo slug danh mục (exact match, case insensitive)
    @Query("select distinct p from Product p join p.categories c where lower(c.slug) = lower(:categorySlug) ")
    List<Product> findByCategorySlug(@Param("categorySlug") String categorySlug);

    // Tìm sản phẩm theo slug danh mục (partial match, case insensitive) - thêm method mới
    @Query("select distinct p from Product p join p.categories c where lower(c.slug) like lower(concat('%', :categorySlug, '%') ) ")
    List<Product> findByCategorySlugContaining(@Param("categorySlug") String categorySlug);

    // Tìm sản phẩm theo đánh giá tối thiểu
    @Query("select p from Product p where p.avgRating >= :minRating")
    Page<Product> findByMinRating(@Param("minRating") Double minRating, Pageable pageable);

    // Tìm sản phẩm theo khoảng giá (qua variants)
    @Query("select distinct p from Product p join p.variants v where v.price between :minPrice and :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    // Tìm sản phẩm có trong kho (qua variants)
    @Query("select distinct p from Product p join p.variants v where v.inStock > 0 and v.isAvailable = true")
    Page<Product> findInStockProducts(Pageable pageable);

    // Tìm sản phẩm có giảm giá (qua variants)
    @Query("select distinct p from Product p join p.variants v where v.discountPrice is not null and v.discountPrice < v.price")
    Page<Product> findProductsWithDiscount(Pageable pageable);

    // Top sản phẩm bán chạy nhất theo sold_quantity
    List<Product> findTop10ByOrderBySoldQuantityDesc();

    // Top sản phẩm đánh giá cao nhất (chỉ lấy sản phẩm có rating > 0)
    @Query("select p from Product p where p.avgRating > 0 order by p.avgRating desc, p.soldQuantity desc")
    List<Product> findTopRatedProducts(Pageable pageable);

    // Tổng số lượng đã bán
    @Query("select coalesce(sum(p.soldQuantity), 0) from Product p")
    long sumTotalSoldQuantity();

    // Rating trung bình tất cả sản phẩm (có rating > 0)
    @Query("select coalesce(avg(p.avgRating), 0) from Product p where p.avgRating > 0")
    double averageRating();

    // Đếm sản phẩm theo khoảng rating
    long countByAvgRatingBetween(double minRating, double maxRating);

    // Đếm sản phẩm chưa có đánh giá
    long countByAvgRating(double avgRating);
}





