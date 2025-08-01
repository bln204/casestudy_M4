package com.casestudy_m4.service;
import com.casestudy_m4.model.Clothing;
import com.casestudy_m4.repository.IClothingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClothingService implements IClothingService {
    @Autowired
    private IClothingRepository clothingRepository;

    @Override
    public Iterable<Clothing> findAll() {
        return clothingRepository.findAll(Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Clothing> findAllWithPaging(Pageable pageable) {
        return clothingRepository.findAll(pageable);
    }

    @Override
    public Optional<Clothing> findById(Long id) {
        return clothingRepository.findById(id);
    }

    @Override
    public Page<Clothing> searchClothings(Double minPrice, Double maxPrice, String category, String size, Pageable pageable) {
        if (category != null && category.trim().isEmpty()) {
            category = null;
        }
        if (size != null && size.trim().isEmpty()) {
            size = null;
        }
        return clothingRepository.searchClothings(minPrice, maxPrice, category, size, pageable);
    }
}