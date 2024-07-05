package ua.ypon.TestAssignment_Java.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ypon.TestAssignment_Java.models.User;
import ua.ypon.TestAssignment_Java.repositories.UserRepository;
import ua.ypon.TestAssignment_Java.util.UserNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author ua.ypon 04.07.2024
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));}

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    @Transactional
    public void update(Long id, User updatedUser) {
        updatedUser.setId(id);
    }

    @Transactional
    public User updatePartialUser(Long id, Map<String, Object> updates) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "email" -> existingUser.setEmail((String) value);
                case "firstName" -> existingUser.setFirstName((String) value);
                case "lastName" -> existingUser.setLastName((String) value);
                case "birthDate" -> existingUser.setBirthDate((LocalDate) value);
                case "address" -> existingUser.setAddress((String) value);
                case "phoneNumber" -> existingUser.setPhoneNumber((String) value);
                default -> throw new IllegalArgumentException("Invalid field: " + key);
            }
        });

        return existingUser;
    }


    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> searchUserByBirth_date(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Error: 'From' date should be less than 'To' date.");
        }
        return userRepository.searchUserByBirth_dateBetweenOrderByBirth_date(startDate, endDate);
    }
}
