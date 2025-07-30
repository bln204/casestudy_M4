package com.casestudy_m4.service;



import com.casestudy_m4.model.Clothing;
import com.casestudy_m4.repository.IClothingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClothingService {
    @Autowired
    private IClothingRepository clothingRepository;

    public List<Clothing> searchClothings(Double minPrice, Double maxPrice, String category, String size) {
        // Xử lý chuỗi rỗng hoặc null
        if (category != null && category.trim().isEmpty()) {
            category = null;
        }
        if (size != null && size.trim().isEmpty()) {
            size = null;
        }
        return clothingRepository.searchClothings(minPrice, maxPrice, category, size);
    }


    public Clothing getClothingById(Long id) {
        return clothingRepository.findById(id).orElse(null);
    }

    public List<Clothing> getAllClothings() {
        return clothingRepository.findAll();
    }
}