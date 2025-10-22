package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.User;
import com.ong.empuje.comunitario.consumer.repository.UserRepository;
import com.ong.empuje.comunitario.consumer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Integer id) {
        logger.debug("Finding User with id: {}", id);
        try {
            return userRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", id, e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        logger.debug("Finding all Users");
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Users", e);
            throw new RuntimeException("Failed to find users: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public User createUser(User user) {
        logger.debug("Creating User: {}", user);
        try {
            validateUser(user);
            User savedUser = userRepository.save(user);
            logger.info("Created User with id: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating User: {}", user, e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        logger.debug("Updating User: {}", user);
        try {
            validateUser(user);
            Optional<User> existingUser = userRepository.findById(user.getId());
            if (existingUser.isEmpty()) {
                logger.error("User not found for id: {}", user.getId());
                throw new RuntimeException("User not found");
            }
            User updatedUser = userRepository.save(user);
            logger.info("Updated User with id: {}", updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            logger.error("Error updating User: {}", user, e);
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteUser(Integer id) {
        logger.debug("Deleting User with id: {}", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                logger.error("User not found for id: {}", id);
                throw new RuntimeException("User not found");
            }
            userRepository.deleteById(id);
            logger.info("Deleted User with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting User with id: {}", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    private void validateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        // Add additional validation as needed, e.g., checking for required fields like username or email
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> save(User user) {
        logger.debug("Saving User with id: {}", user);
        try {
            return Optional.of(userRepository.save(user));
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", user.getId(), e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }
}