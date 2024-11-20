/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.refactoring.data;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Error DTO.
 */
@Getter
@Setter
public class Error {
    private Date timestamp;
    private String message;
}
