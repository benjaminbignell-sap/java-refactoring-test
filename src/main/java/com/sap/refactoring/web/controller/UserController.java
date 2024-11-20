package com.sap.refactoring.web.controller;

import com.sap.refactoring.data.UserData;
import com.sap.refactoring.service.UserService;
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
 *     <li>GET /users/id -> Get the user with the given id</li>
 *     <li>POST /users -> Create a new user, pass a {@link UserData} in the body</li>
 *     <li>PUT /users/{id} -> Update user identified by {id}, pass a {@link UserData} in the body</li>
 *     <li>DELETE /user/{id} -> Delete user identified by {id}</li>
 * </ul>
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor.
     *
     * @param userService The user service
     */
    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets a list of users, with an optional filter by name.
     *
     * @param name Optional name filter
     * @return A list of users
     */
    @GetMapping
    public ResponseEntity<List<UserData>> getUsers(@RequestParam(value = "name", required = false) final String name) {
        return ResponseEntity.ok(userService.getUsers(name));
    }

    /**
     * Gets a user by id.
     *
     * @param id The id
     * @return The user, or a 404 if none is found for that email
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserData> getUser(@PathVariable(value = "id") final Long id) {
        final var user = userService.getUser(id);
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
    public ResponseEntity<UserData> addUser(@RequestBody final UserData user) {
        final var createdUser = userService.createUser(user);
        return ResponseEntity
                .created(URI.create("/users/" + user.getEmail()))
                .body(createdUser);
    }

    /**
     * Updates an existing user.
     *
     * @param id       The user id
     * @param userData The user data
     * @return The updated user with a 201, or a 404 if no user exists for that id. If the email in the path and the
     * email in the user payload do not match, a 400 is returned.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserData> updateUser(@PathVariable(name = "id") final Long id,
            @RequestBody final UserData userData) {

        // the id in the path and the id in the payload must match
        if (!id.equals(userData.getId())) {
            throw new IllegalArgumentException("The id in the path and the payload do not match");
        }

        // update the user
        final var updated = userService.updateUser(userData);
        return updated != null
                ? ResponseEntity.status(HttpStatus.ACCEPTED).body(updated)
                : ResponseEntity.notFound().build();
    }

    /**
     * Deletes a user for the given id.
     *
     * @param id The user id
     * @return Returns a 204 in all scenarios; we do not indicate if there was actually any user deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(value = "id") final Long id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
