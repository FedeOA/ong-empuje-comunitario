package com.ong.empuje.comunitario.web_services.repository;

import com.ong.empuje.comunitario.web_services.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
}