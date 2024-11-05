package com.sap.refactoring.users;

import com.sap.refactoring.repository.UserRepository;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * DAO to interact with the {@link UserRepository}. This class includes some basic validation and other logic that would
 * probably be better placed in a service or facade, but for the sake of simplicity these items been left here. All
 * transactions required for writing data are expected to be handled by the repository.
 */
@Component
public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);
    private static final String ERR_USER_NOT_NULL = "The user cannot be null";
    private static final String ERR_EMAIL_MUST_BE_PROVIDED = "The email address must be provided";
    private static final String ERR_NUMBER_OF_ROLES = "At least one user role must be provided";

    private final UserRepository userRepository;

    /**
     * Constructor.
     *
     * @param userRepository The user repository
     */
    @Autowired
    public UserDao(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Saves a user to the database.
     *
     * @param user The user
     * @return The saved user
     */
    public User createUser(@Nonnull final User user) {
        Assert.notNull(user, ERR_USER_NOT_NULL);
        Assert.hasText(user.getEmail(), ERR_EMAIL_MUST_BE_PROVIDED);
        Assert.isTrue(hasRoles(user), ERR_NUMBER_OF_ROLES);
        return userRepository.save(user);
    }

    /**
     * Gets a list of all users from the database, or all users matching the optional {@code name} parameter. No
     * pagination or limits are provided.
     *
     * @param name An optional name filter
     * @return An immutable list of users
     */
    @Nonnull
    public List<User> getUsers(final String name) {
        try {
            if (StringUtils.hasText(name)) {
                return findUsers(name);
            } else {
                return StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();
            }
        } catch (final Exception ex) {
            LOG.error("Failed to get users", ex);
            return List.of();
        }
    }

    /**
     * Gets a user by email.
     *
     * @param email The email
     * @return The user, or null if none is found for that email
     */
    public User getUser(@Nonnull final String email) {
        Assert.hasText(email, ERR_EMAIL_MUST_BE_PROVIDED);
        try {
            return userRepository.findById(email).orElse(null);
        } catch (final Exception ex) {
            LOG.error("Failed to get user with email {}", email, ex);
            return null;
        }
    }

    /**
     * Deletes a user from the database.
     *
     * @param userToDelete A non-null user with an email address
     */
    public void deleteUser(@Nonnull final User userToDelete) {
        Assert.notNull(userToDelete, ERR_USER_NOT_NULL);
        Assert.hasText(userToDelete.getEmail(), ERR_EMAIL_MUST_BE_PROVIDED);

        try {
            userRepository.deleteById(userToDelete.getEmail());
        } catch (final Exception ex) {
            LOG.error("Failed to delete user with email {}", userToDelete.getEmail(), ex);
        }
    }

    /**
     * Updates a user.
     *
     * @param userToUpdate The user to update
     */
    public User updateUser(@Nonnull final User userToUpdate) {
        Assert.notNull(userToUpdate, ERR_USER_NOT_NULL);
        Assert.hasText(userToUpdate.getEmail(), ERR_EMAIL_MUST_BE_PROVIDED);
        Assert.isTrue(hasRoles(userToUpdate), ERR_NUMBER_OF_ROLES);

        try {
            return userRepository.findById(userToUpdate.getEmail()).map(user -> {
                user.setName(userToUpdate.getName());
                user.setRoles(new ArrayList<>(userToUpdate.getRoles()));
                return userRepository.save(user);
            }).orElseThrow();
        } catch (final Exception ex) {
            LOG.error("Failed to update user with email {}", userToUpdate.getEmail(), ex);
            return null;
        }
    }

    /**
     * Finds users by name.
     *
     * @param name The name
     * @return The user, or null if no user is found.
     */
    @Nonnull
    public List<User> findUsers(@Nonnull final String name) {
        Assert.hasText(name, "The name must be provided");

        try {
            return userRepository.findByName(name);
        } catch (final Exception ex) {
            LOG.error("An error occurred finding user with name {}", name, ex);
            return List.of();
        }
    }

    static boolean hasRoles(@Nonnull final User user) {
        return user.getRoles() != null && !user.getRoles().isEmpty();
    }
}
