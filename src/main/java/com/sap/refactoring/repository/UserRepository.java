package com.sap.refactoring.repository;

import com.sap.refactoring.users.User;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A {@link CrudRepository} for {@link User} entities.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(@Nonnull final String email);

    /**
     * Find users by their name.
     *
     * @param name The name, must not be null or blank
     * @return A non-null list of users
     */
    @Nonnull
    List<User> findByName(@Nonnull final String name);
}
