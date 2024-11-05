package com.sap.refactoring.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import com.sap.refactoring.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Unit test to ensure that {@link UserDao} provides adequate validations, and makes the appropriate calls to the
 * repository. Moved into the same package as the class it's testing.
 */
@SpringBootTest
class UserDaoUnitTest {

    @Autowired
    UserDao userDao;

    @Autowired
    UserRepository userRepository;

    @Configuration
    static class Config {

        @Bean
        public UserDao userDao() {
            return new UserDao(userRepository());
        }

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }

    @BeforeEach
    public void setUp() {
        Mockito.reset(userRepository);
    }

    @Test
    void createUserTest() {
        // ensure assertions work for parameter validation
        Try.call(() -> userDao.createUser(null)).ifSuccess(t -> fail("A null user should throw an assertion"));
        Try.call(() -> userDao.createUser(new User()))
                .ifSuccess(t -> fail("A user without email address should throw an assertion"));
        Try.call(() -> userDao.createUser(createUser("John", List.of())))
                .ifSuccess(t -> fail("A user without roles should throw an assertion"));

        // create a user
        final var user = createUser("fake", List.of("admin", "customerservice"));
        userDao.createUser(user);
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    void deleteUserTest() {
        // ensure assertions work for parameter validation
        Try.call(() -> doDelete(null)).ifSuccess(t -> fail("A null user should throw an assertion"));
        Try.call(() -> doDelete(new User()))
                .ifSuccess(t -> fail("A user without email address should throw an assertion"));

        // delete a user
        final var user = createUser("fake", List.of());
        userDao.deleteUser(user);
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(user.getEmail());
    }

    @Test
    void getUsersTest() {
        // find all users
        Mockito.when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        var users = userDao.getUsers(null);
        assertNotNull(users);
        assertEquals(2, users.size());
        Mockito.verify(userRepository, Mockito.times(1)).findAll();

        // find users by name
        Mockito.when(userRepository.findByName("bob")).thenReturn(List.of(createUser("bob", List.of())));
        users = userDao.getUsers("bob");
        assertNotNull(users);
        assertEquals(1, users.size());
        Mockito.verify(userRepository, Mockito.times(1)).findByName("bob");
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    void getUserTest() {
        // ensure assertions work for parameter validation
        Try.call(() -> userDao.getUser(null)).ifSuccess(t -> fail("A null email should throw an assertion"));
        Try.call(() -> userDao.getUser("")).ifSuccess(t -> fail("A blank email should throw an assertion"));

        // get user by id
        userDao.getUser("bob@mail.com"); // return not important
        Mockito.verify(userRepository, Mockito.times(1)).findById("bob@mail.com");
    }

    @Test
    void findUsersTest() {
        // ensure assertions work for parameter validation
        Try.call(() -> userDao.findUsers(null)).ifSuccess(t -> fail("A null name should throw an assertion"));
        Try.call(() -> userDao.findUsers("")).ifSuccess(t -> fail("A null name should throw an assertion"));

        // find users by name
        userDao.findUsers("bob");
        Mockito.verify(userRepository, Mockito.times(1)).findByName("bob");
    }

    @Test
    void updateUserTest() {
        // ensure assertions work for parameter validation
        Try.call(() -> userDao.updateUser(null)).ifSuccess(t -> fail("A null user should throw an assertion"));
        Try.call(() -> userDao.updateUser(new User()))
                .ifSuccess(t -> fail("A user without email address should throw an assertion"));
        Try.call(() -> userDao.updateUser(createUser("John", List.of())))
                .ifSuccess(t -> fail("A user without roles should throw an assertion"));

        // update a user to have a different name and list of roles
        final var originalUser = createUser("bob", List.of("role1", "role2"));
        originalUser.setName("John");

        final var user = createUser("bob", List.of("role1", "role2", "role3"));
        Mockito.when(userRepository.findById(user.getEmail())).thenReturn(java.util.Optional.of(originalUser));
        Mockito.when(userRepository.save(originalUser)).thenReturn(user);

        final var updatedUser = userDao.updateUser(user);
        assertNotNull(updatedUser);
        assertEquals(updatedUser, user);
        Mockito.verify(userRepository, Mockito.times(1)).findById(user.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).save(user);

        // attempt to update a user that doesn't exist
        final var invalidUser = createUser("notfound", List.of("role1", "role2"));
        final var notUpdatedUser = userDao.updateUser(invalidUser);
        assertNull(notUpdatedUser);
        Mockito.verify(userRepository, Mockito.times(1)).findById(invalidUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(0)).save(invalidUser);
    }


    Void doDelete(final User user) {
        userDao.deleteUser(user);
        return null;
    }

    User createUser(final String name, final List<String> roles) {
        final var user = new User();
        user.setName(name);
        user.setEmail(name + "@integration.com");
        user.setRoles(roles);
        return user;
    }
}