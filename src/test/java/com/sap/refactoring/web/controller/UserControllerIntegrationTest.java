package com.sap.refactoring.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refactoring.data.UserData;
import com.sap.refactoring.service.UserService;
import com.sap.refactoring.users.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Integration test for the {@link UserController}. Moved from the original "UserIntegrationTest" into the same package
 * as the class it's testing. The "UserResourceUnitTest" didn't appear to do anything useful, it was removed.
 */
@WebMvcTest
class UserControllerIntegrationTest {

    @MockBean
    UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        Mockito.reset(userService);
        Mockito.when(userService.createUser(Mockito.any(UserData.class))).thenAnswer(ans -> ans.getArgument(0));
    }

    @Test
    void createUserTest() throws Exception {
        // create a user and valid that it was returned
        final var integration = createUser("initial");
        final var result = createNewUser(integration)
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/users/" + integration.getEmail()))
                .andReturn();
        final var returnedUser = parsePayload(result.getResponse().getContentAsString());
        assertEquals(integration, returnedUser);
    }

    @Test
    void updateUserTest() throws Exception {
        // update the user's name and validate that it was returned
        final var updated = createUser("initial");
        updated.setName("updated");
        Mockito.when(userService.updateUser(updated)).thenReturn(updated);

        var result = mockMvc.perform(put("/users/" + updated.getId())
                        .contentType("application/json")
                        .content(buildPayload(updated))
                ).andExpect(status().isAccepted())
                .andReturn();

        final var returnedUser = parsePayload(result.getResponse().getContentAsString());
        assertEquals(updated, returnedUser);

        // attempt to update a user where the id in the path doesn't match the payload
        mockMvc.perform(put("/users/2")
                .contentType("application/json")
                .content(buildPayload(updated))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void updateUserTestNotFound() throws Exception {
        // create a user
        createNewUser(createUser("initial"));

        // attempt to update a user that doesn't exist
        final var updated = createUser("notvalid");
        Mockito.when(userService.updateUser(updated)).thenReturn(null);
        mockMvc.perform(put("/users/" + updated.getId())
                .contentType("application/json")
                .content(buildPayload(updated))
        ).andExpect(status().isNotFound());

        // attempt to update a user that doesn't exist
        final var updated2 = createUser("notvalid2");
        Mockito.when(userService.updateUser(updated2)).thenReturn(null);
        mockMvc.perform(put("/users/" + (updated2.getId() + 10L))
                .contentType("application/json")
                .content(buildPayload(updated))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getUsers() throws Exception {
        // create some useres
        final var user1 = createUser("user1");
        final var user2 = createUser("user2");
        final var user3 = createUser("user3");
        createNewUser(createUser("user1"));
        createNewUser(createUser("user2"));
        createNewUser(createUser("user3"));

        Mockito.when(userService.getUsers(null)).thenReturn(List.of(user1, user2, user3));
        Mockito.when(userService.getUsers("user1")).thenReturn(List.of(user1));

        // get all users
        var result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();
        var users = List.of(mapper.readValue(result.getResponse().getContentAsString(), User[].class));
        assertEquals(3, users.size());

        // get user by name, with a matching result
        result = mockMvc.perform(get("/users?name=user1"))
                .andExpect(status().isOk())
                .andReturn();
        users = List.of(mapper.readValue(result.getResponse().getContentAsString(), User[].class));
        assertEquals(1, users.size());

        // get user by name, no matching result
        result = mockMvc.perform(get("/users?name=user777"))
                .andExpect(status().isOk())
                .andReturn();
        users = List.of(mapper.readValue(result.getResponse().getContentAsString(), User[].class));
        assertTrue(users.isEmpty());
    }

    private ResultActions createNewUser(final UserData user) throws Exception {
        return mockMvc.perform(post("/users")
                .contentType("application/json")
                .content(buildPayload(user))
        );
    }

    private String buildPayload(final UserData user) {
        try {
            return mapper.writeValueAsString(user);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private UserData parsePayload(final String value) {
        try {
            return mapper.readValue(value, UserData.class);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private UserData createUser(final String name) {
        final var user = new UserData();
        user.setId(1L);
        user.setName(name);
        user.setEmail(name + "@integration.com");
        user.setRoles(List.of("role1"));
        return user;
    }
}
