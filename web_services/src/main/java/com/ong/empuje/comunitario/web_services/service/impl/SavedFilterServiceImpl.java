package com.ong.empuje.comunitario.web_services.service.impl;

import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.SavedFilter;
import com.ong.empuje.comunitario.web_services.model.User;
import com.ong.empuje.comunitario.web_services.repository.SavedFilterRepository;
import com.ong.empuje.comunitario.web_services.service.SavedFilterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SavedFilterServiceImpl implements SavedFilterService {

    @Autowired
    private SavedFilterRepository savedFilterRepository;

    @Override
    public List<SavedFilter> findByCategoryId(Integer categoryId) {
        return savedFilterRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<SavedFilter> findByUserId(Integer userId) {
        return savedFilterRepository.findByUserId(userId);
    }

    @Override
    public List<SavedFilter> findByCategory(Category category) {
        return savedFilterRepository.findByCategory(category);
    }

    @Override
    public List<SavedFilter> findByUser(User user) {
        return savedFilterRepository.findByUser(user);
    }

    @Override
    public Optional<SavedFilter> findByUserIdAndName(Integer userId, String name) {
        return savedFilterRepository.findByUserIdAndName(userId, name);
    }

    @Override
    public Optional<SavedFilter> findByIdAndUserId(Integer id, Integer userId) {
        return savedFilterRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public List<SavedFilter> findByUserUsername(String username) {
        return savedFilterRepository.findByUserUsername(username);
    }

    @Override
    public Optional<SavedFilter> findByIdAndUserUsername(Integer id, String username) {
        return savedFilterRepository.findByIdAndUserUsername(id, username);
    }

    @Override
    public List<SavedFilter> findByIsDeletedFalse() {
        return savedFilterRepository.findByIsDeletedFalse();
    }

    @Override
    public Optional<SavedFilter> findById(Integer id) {
        return savedFilterRepository.findById(id);
    }

    @Override
    public SavedFilter save(SavedFilter filter) {
        return savedFilterRepository.save(filter);
    }   
}