package ua.ypon.TestAssignment_Java.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.ypon.TestAssignment_Java.models.User;
import ua.ypon.TestAssignment_Java.services.UserService;
import ua.ypon.TestAssignment_Java.util.UserNotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * @author ua.ypon 04.07.2024
 */
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    @Value("${app.minAgeToRegister}")
    private int minAgeToRegister;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        // Перевірка валідності електронної адреси
        if (!isValidEmailAddress(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid");
        }
        LocalDate birth_date = user.getBirthDate();
        if(birth_date == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Birth date is required");
        }
        Date date = Date.from(birth_date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int yearOfBirth = cal.get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int age = currentYear - yearOfBirth;

        if (age < minAgeToRegister) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User should be at least" + minAgeToRegister + " years old.");
        }
        return ResponseEntity.ok("User created successfully");
    }

    private boolean isValidEmailAddress(String email) {
        //перевірка на основі наявності '@' у адресі
        return email != null && email.contains("@");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updatePartialUser(@Valid @RequestBody Map<String, Object> updates, @PathVariable Long id) {
        try {
            User updatedUser = userService.updatePartialUser(id, updates);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e){
                return ResponseEntity.badRequest().build();
            }
        }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        Optional<User> optionalUser = Optional.ofNullable(userService.findUserById(id));
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        userService.update(id, user);
        return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if(!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUserByBirthDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<User> users = userService.searchUserByBirth_date(startDate, endDate);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
