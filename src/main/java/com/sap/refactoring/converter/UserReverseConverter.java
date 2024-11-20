/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.refactoring.converter;

import com.sap.refactoring.data.UserData;
import com.sap.refactoring.users.User;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link UserData} DTO into a {@link User} entity.
 */
@Component
public class UserReverseConverter implements Converter<UserData, User> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public User convert(@Nonnull final UserData userData) {
        final var user = new User();
        user.setId(userData.getId());
        user.setEmail(userData.getEmail());
        user.setName(userData.getName());
        user.setRoles(List.copyOf(userData.getRoles()));
        return user;
    }
}
