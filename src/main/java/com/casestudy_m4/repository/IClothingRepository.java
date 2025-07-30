package com.casestudy_m4.repository;

import com.casestudy_m4.model.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IClothingRepository extends JpaRepository<Clothing, Long> {
    @Query("SELECT c FROM Clothing c WHERE " +
            "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
            "(:category IS NULL OR c.category.name = :category) AND " +
            "(:size IS NULL OR c.size = :size)")
    List<Clothing> searchClothings(@Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("category") String category,
                                   @Param("size") String size);
}