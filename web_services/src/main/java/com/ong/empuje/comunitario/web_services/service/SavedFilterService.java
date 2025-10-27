package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.SavedFilter;
import com.ong.empuje.comunitario.web_services.model.User;
import java.util.List;
import java.util.Optional;

public interface SavedFilterService {
    List<SavedFilter> findByCategoryId(Integer categoryId);
    List<SavedFilter> findByUserId(Integer userId);
    List<SavedFilter> findByCategory(Category category);
    List<SavedFilter> findByUser(User user);
    Optional<SavedFilter> findByUserIdAndName(Integer userId, String name);
    Optional<SavedFilter> findByIdAndUserId(Integer id, Integer userId);
    List<SavedFilter> findByUserUsername(String username);
    Optional<SavedFilter> findByIdAndUserUsername(Integer id, String username);
    List<SavedFilter> findByIsDeletedFalse();
    Optional<SavedFilter> findById(Integer id);
    SavedFilter save(SavedFilter filter);
}