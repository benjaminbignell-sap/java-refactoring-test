package com.sap.refactoring.web.controller;

import com.sap.refactoring.users.User;
import com.sap.refactoring.users.UserDao;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the /users API.
 * <ul><strong>Operations:</strong>
 *     <li>GET /users -> Get all users</li>
 *     <li>GET /users?name=value -> Get all users where name equals {@code value}</li>
 *     <li>GET /users/{email} -> Get the user with the given email address</li>
 *     <li>POST /users -> Create a new user, pass a {@link User} in the body</li>
 *     <li>PUT /users/{email} -> Update user identified by {email}, pass a {@link User} in the body</li>
 *     <li>DELETE /user/{email} -> Delete user identified by {email}</li>
 * </ul>
 * Ideally, this controller should be returning DTOs instead of the User entity. But for this simple example, it's
 * probably acceptable. (Also, we don't have an internal identifier like a Commerce PK that should probably stay hidden
 * from the consumers.)
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserDao userDao;

    /**
     * Constructor.
     *
     * @param userDao The user DAO
     */
    @Autowired
    public UserController(final UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Gets a list of users, with an optional filter by name.
     *
     * @param name Optional name filter
     * @return A list of users
     */
    @GetMapping
    public ResponseEntity<List<User>> getUsers(@RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(userDao.getUsers(name));
    }

    /**
     * Gets a user by email.
     *
     * @param email The email
     * @return The user, or a 404 if none is found for that email
     */
    @GetMapping("/{email}")
    public ResponseEntity<User> getUser(@PathVariable(value = "email") final String email) {
        final var user = userDao.getUser(email);
        return user != null
                ? ResponseEntity.ok(user)
                : ResponseEntity.notFound().build();
    }

    /**
     * Creates a new user.
     *
     * @param user The user to create
     * @return The new user, or a 409 if a user with that email already exists
     */
    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody final User user) {
        /*
        I'm not sure how I feel about this ... it seems off somehow. Maybe the validation should be done in the DAO
        (or in the non-existent UserService). The big issue is that the createUser operation could happen many times
        (it's transactional in the repository, so that's OK) but the "last" request would win and set its values.

        My intent was to never call createUser() (and by extension, the repository's save() method) for an existing
        user. I could put @Transactional here on the controller method, but that's the wrong place, and it doesn't
        necessarily guarantee anything.
        */
        final var existingUser = userDao.getUser(user.getEmail());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        final var createdUser = userDao.createUser(user);
        return ResponseEntity
                .created(URI.create("/users/" + user.getEmail()))
                .body(createdUser);
    }

    /**
     * Updates an existing user.
     *
     * @param email The user email
     * @param user  The user data
     * @return The updated user with a 201, or a 404 if no user exists for that email. If the email in the path and the
     * email in the user payload do not match, a 400 is returned.
     */
    @PutMapping("/{email}")
    public ResponseEntity<User> updateUser(@PathVariable(name = "email") final String email,
            @RequestBody final User user) {

        if (!email.equals(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        final var updated = userDao.updateUser(user);
        return updated != null
                ? ResponseEntity.status(HttpStatus.ACCEPTED).body(updated)
                : ResponseEntity.notFound().build();
    }

    /**
     * Deletes a user for the given email.
     *
     * @param email The email address
     * @return Returns a 204 in all scenarios; we do not indicate if there was actually any user deletion
     */
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable(value = "email") final String email) {

        final var userToDelete = new User();
        userToDelete.setEmail(email);
        userDao.deleteUser(userToDelete);
        return ResponseEntity.noContent().build();
    }
}
