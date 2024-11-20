/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.refactoring.service;

import com.sap.refactoring.converter.UserDataConverter;
import com.sap.refactoring.converter.UserReverseConverter;
import com.sap.refactoring.data.UserData;
import com.sap.refactoring.users.User;
import com.sap.refactoring.users.UserDao;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


/**
 * Simple user service.
 */
@Service
@Transactional
public class UserService {

    private static final String ERR_EMAIL_IN_USE = "The provided email is already in use";

    private final UserDao userDao;
    private final UserDataConverter userDataPopulator;
    private final UserReverseConverter userReversePopulator;

    /**
     * Constructor.
     *
     * @param userDao The user dao
     * @param userDataPopulator The user populator
     * @param userReversePopulator The user reverse populator
     */
    public UserService(final UserDao userDao, final UserDataConverter userDataPopulator,
            final UserReverseConverter userReversePopulator) {
        this.userDao = userDao;
        this.userDataPopulator = userDataPopulator;
        this.userReversePopulator = userReversePopulator;
    }

    /**
     * Get users for the given name.
     *
     * @param name The name, optional
     * @return A list of users
     */
    public List<UserData> getUsers(final String name) {
        return userDao.getUsers(StringUtils.trim(name))
                .stream()
                .map(userDataPopulator::convert)
                .toList();
    }

    /**
     * Get a user for the given email address.
     *
     * @param id The id, required
     * @return The user, or null if not found
     */
    public UserData getUser(@Nonnull final Long id) {
        Assert.notNull(id, "The email must not be null");
        Assert.isTrue(id > 0L, "The id is out of range");

        return userDao.getUserById(id)
                .map(userDataPopulator::convert)
                .orElse(null);
    }

    public UserData getUserByEmail(@Nonnull final String email) {
        Assert.hasText(email, "The email must not be empty");
        return userDao.getUserByEmail(email)
                .map(userDataPopulator::convert)
                .orElse(null);
    }

    /**
     * Create a user.
     *
     * @param userData The user data, not null
     * @return The created user DTO
     */
    public UserData createUser(@Nonnull final UserData userData) {
        Assert.notNull(userData, "The user data must be provided");
        Assert.hasText(userData.getEmail(), "The email must not be empty");

        if (userDao.getUserByEmail(userData.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException(ERR_EMAIL_IN_USE);
        }

        final var example = userReversePopulator.convert(userData);
        return Optional.of(userDao.createUser(example))
                .map(userDataPopulator::convert)
                .orElse(null);
    }

    /**
     * Updates an existing user.
     *
     * @param userData The user DTO, not null
     * @return The update user DTO
     */
    public UserData updateUser(@Nonnull final UserData userData) {
        final var optUser = userDao.getUserById(userData.getId());
        if (optUser.isEmpty()) {
            return null;
        } else {
            final var optUserByEmail = userDao.getUserByEmail(userData.getEmail());
            if (optUserByEmail.isPresent() && !userData.getId().equals(optUserByEmail.get().getId())) {
                throw new DataIntegrityViolationException(ERR_EMAIL_IN_USE);
            }

            final var updatedUser = userReversePopulator.convert(userData);
            return userDao.updateUser(updatedUser)
                    .map(userDataPopulator::convert)
                    .orElse(null);
        }
    }

    /**
     * Deletes a user by email.
     *
     * @param id The user id
     */
    public void deleteUser(final Long id) {
        userDao.deleteUser(id);
    }
}
