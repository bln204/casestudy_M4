package com.casestudy_m4.repository;

import com.casestudy_m4.model.Clothing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClothingRepository extends PagingAndSortingRepository<Clothing, Long> {
    @Query("SELECT c FROM Clothing c WHERE " +
            "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
            "(:category IS NULL OR (c.category IS NOT NULL AND c.category.name = :category)) AND " +
            "(:size IS NULL OR c.size = :size)")
    Page<Clothing> searchClothings(@Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("category") String category,
                                   @Param("size") String size,
                                   Pageable pageable);

    Optional<Clothing> findById(Long id);
}