package ua.ypon.TestAssignment_Java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.ypon.TestAssignment_Java.controllers.UserController;
import ua.ypon.TestAssignment_Java.models.User;
import ua.ypon.TestAssignment_Java.services.UserService;
import ua.ypon.TestAssignment_Java.util.UserNotFoundException;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private User user;
    @Test
    public void testCreateUserWithValidAge() throws Exception {
        when(userService.existsById(anyLong())).thenReturn(false);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"firstName\":\"Name\",\"lastName\":\"LastName\",\"birthDate\":\"2000-01-01\",\"address\":\"Address\",\"phoneNumber\":\"1234567890\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));
    }

    @Test
    public void testCreateUserWithInvalidAge() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"firstName\":\"Name\",\"lastName\":\"LastName\",\"birthDate\":null,\"address\":\"Address\",\"phoneNumber\":\"1234567890\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Birth date is required"));
    }

    @Test
    public void testUpdatePartialUser() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "NewName");
        updates.put("lastName", "NewLastName");
        updates.put("address", "NewAddress");

        User updatedUser = new User(1L, "test@example.com", "NewName", "NewLastName", LocalDate.now(), "NewAddress", "1234567890");

        when(userService.updatePartialUser(anyLong(), any(Map.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"NewName\",\"lastName\":\"NewLastName\",\"address\":\"NewAddress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("NewName"))
                .andExpect(jsonPath("$.lastName").value("NewLastName"))
                .andExpect(jsonPath("$.address").value("NewAddress"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        when(userService.existsById(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSearchUsersByBirthDateRange() throws Exception {
        List<User> users = Arrays.asList(
                new User(1L, "test1@example.com", "TestName1", "TestLastName1", LocalDate.of(2000, 1, 1), "Address1", "1234567891"),
                new User(2L, "test2@example.com", "TestName2", "TestLastName2", LocalDate.of(1995, 5, 5), "Address2", "1234567892")
        );

        when(userService.searchUserByBirth_date(any(LocalDate.class), any(LocalDate.class))).thenReturn(users);

        mockMvc.perform(get("/users/search")
                        .param("startDate", "1990-01-01")
                        .param("endDate", "2000-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("TestName1"))
                .andExpect(jsonPath("$[1].firstName").value("TestName2"));
    }

    @Test
    public void testCreateUserWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid_email\",\"firstName\":\"Name\",\"lastName\":\"LastName\",\"birthDate\":\"2000-01-01\",\"address\":\"Address\",\"phoneNumber\":\"1234567890\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is invalid"));
    }

    @Test
    public void testUpdateNonExistingUser() throws Exception {
        when(userService.updatePartialUser(anyLong(), any(Map.class))).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"NewName\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteNonExistingUser() throws Exception {
        when(userService.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSearchUsersByEmptyResult() throws Exception {
        when(userService.searchUserByBirth_date(any(LocalDate.class), any(LocalDate.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/search")
                        .param("startDate", "2025-02-02")
                        .param("endDate", "2030-12-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
