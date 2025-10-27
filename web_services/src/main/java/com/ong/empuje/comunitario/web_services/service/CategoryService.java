package com.ong.empuje.comunitario.web_services.service;

import java.util.List;

import com.ong.empuje.comunitario.web_services.model.Category;

import java.util.Optional;

public interface CategoryService {
    Optional<Category> findByName(String name);
    List<Category> findAll();
    Optional<Category> findById(Integer id);
}