package com.casestudy_m4.service;

import com.casestudy_m4.model.Clothing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IClothingService {
    Iterable<Clothing> findAll();
    Page<Clothing> findAllWithPaging(Pageable pageable);
    Optional<Clothing> findById(Long id);
    Page<Clothing> searchClothings(Double minPrice, Double maxPrice, String category, String size, Pageable pageable);
}
