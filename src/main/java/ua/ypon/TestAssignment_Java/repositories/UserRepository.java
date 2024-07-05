package ua.ypon.TestAssignment_Java.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ua.ypon.TestAssignment_Java.models.User;

import java.time.LocalDate;
import java.util.List;

/**
 * @author ua.ypon 04.07.2024
 */
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> searchUserByBirth_dateBetweenOrderByBirth_date(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    boolean existsById(Long id);
}
