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
 * Converts a {@link User} entity into a {@link UserData} DTO.
 */
@Component
public class UserDataConverter implements Converter<User, UserData> {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public UserData convert(@Nonnull final User user) {
        final var target = new UserData();
        target.setId(user.getId());
        target.setEmail(user.getEmail());
        target.setName(user.getName());
        target.setRoles(List.copyOf(user.getRoles()));
        return target;
    }
}
