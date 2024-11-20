/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.refactoring.converter;

import jakarta.annotation.Nonnull;

/**
 * A simple interface for the conversion of one type to another.
 *
 * @param <T> The type of argument provided to the converter
 * @param <R> The return type from the converter
 */
public interface Converter<T, R> {

    /**
     * Converts an object.
     *
     * @param source The source object, not null
     * @return The converted object
     */
    @Nonnull R convert(@Nonnull final T source);
}
