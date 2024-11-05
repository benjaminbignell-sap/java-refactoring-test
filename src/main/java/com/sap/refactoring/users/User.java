package com.sap.refactoring.users;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The user entity. The email is the unique identifier.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode
public class User {

    @Id
    @Column(unique = true)
    private String email;
    private String name;
    @ElementCollection
    private List<String> roles;
}
