/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.refactoring.data;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * User DTO.
 */
@Getter
@Setter
@EqualsAndHashCode
public class UserData {
    private Long id;
    private String email;
    private String name;
    private List<String> roles;
}
