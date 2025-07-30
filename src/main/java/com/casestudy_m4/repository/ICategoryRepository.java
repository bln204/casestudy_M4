package com.casestudy_m4.repository;

import com.casestudy_m4.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<Category, Long> {
}